package io.github.klahap

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.runBlocking
import kotlinx.io.Source
import kotlinx.io.buffered
import kotlinx.io.files.Path
import kotlinx.io.files.SystemFileSystem
import kotlinx.io.readLine
import kotlinx.io.readString
import kotlin.jvm.JvmInline

@JvmInline
value class TaskName(private val name: String) {
    override fun toString(): String = name
}

sealed class Task<T : Any>(val block: () -> T) {
    fun execute(silent: Boolean = false): T = block().also {
        if (!silent)
            println("$taskName: ${it.toString().padStart(10, ' ')}")
    }

    val taskName get() = TaskName(this::class.simpleName!!)
}

sealed class AsyncTask<T : Any>(block: suspend CoroutineScope.() -> T) :
    Task<T>({ runBlocking(Dispatchers.Default, block) })

fun fileReader(name: String) = SystemFileSystem.source(Path(name)).buffered()
fun Source.lineSequence(): Sequence<String> = generateSequence { readLine() }
fun Source.readText() = readString()

fun <T> List<T>.allCombinations(): Sequence<Pair<T, T>> =
    asSequence().flatMapIndexed { idx, x -> asSequence().drop(idx + 1).map { y -> x to y } }

sealed interface Pos2D<T> {
    val x: T
    val y: T
}

data class IntPos2D(override val x: Int, override val y: Int) : Pos2D<Int> {
    fun rotate90() = IntPos2D(y, -x)
    operator fun plus(other: Pos2D<Int>) = IntPos2D(x + other.x, y + other.y)
    operator fun minus(other: Pos2D<Int>) = IntPos2D(x - other.x, y - other.y)
    operator fun times(other: Int) = IntPos2D(x * other, y * other)
}

data class LongPos2D(override val x: Long, override val y: Long) : Pos2D<Long> {
    fun rotate90() = LongPos2D(y, -x)
    operator fun plus(other: Pos2D<Long>) = LongPos2D(x + other.x, y + other.y)
    operator fun minus(other: Pos2D<Long>) = LongPos2D(x - other.x, y - other.y)
    operator fun times(other: Int) = LongPos2D(x * other, y * other)
}

sealed interface Matrix<T : Any, Vec : Any> {
    val zero: T
    val n: Int
    val m: Int
    val iInc: Int
    val jInc: Int
    fun generateVec(size: Int, block: (Int) -> T): Vec

    fun isValidPosition(i: Int, j: Int): Boolean =
        (i in 0..<n && j in 0..<m)

    fun isValidPosition(pos: IntPos2D): Boolean =
        (pos.x in 0..<n && pos.y in 0..<m)

    fun posToIndex(pos: IntPos2D) = pos.x * iInc + pos.y * jInc
    fun indexToPos(x: Int): IntPos2D = when {
        iInc == 1 -> IntPos2D(x / jInc, x % jInc)
        jInc == 1 -> IntPos2D(x / iInc, x % iInc)
        else -> throw NotImplementedError() // TODO
    }

    operator fun get(index: Int): T
    operator fun get(i: Int, j: Int): T = get(i * iInc + j * jInc)
    fun getOrNull(i: Int, j: Int): T? = when {
        isValidPosition(i, j) -> get(i * iInc + j * jInc)
        else -> null
    }

    fun getOrZero(i: Int, j: Int): T = when {
        isValidPosition(i, j) -> get(i * iInc + j * jInc)
        else -> zero
    }

    operator fun get(pos: IntPos2D): T = get(pos.x * iInc + pos.y * jInc)
    fun getOrNull(pos: IntPos2D): T? = getOrNull(pos.x, pos.y)
    fun getOrZero(pos: IntPos2D): T = getOrZero(pos.x, pos.y)

    operator fun set(index: Int, value: T)
    operator fun set(i: Int, j: Int, value: T) = set(i * iInc + j * jInc, value)
    operator fun set(pos: IntPos2D, value: T) = set(pos.x * iInc + pos.y * jInc, value)
    fun setOrNull(pos: IntPos2D, value: T) = when {
        isValidPosition(pos.x, pos.y) -> set(pos.x * iInc + pos.y * jInc, value)
        else -> null
    }

    fun row(i: Int): Vec = generateVec(m) { get(i, it) }
    fun col(j: Int): Vec = generateVec(n) { get(it, j) }
    fun diagX(k: Int): Vec = generateVec(m) { getOrZero(k + it, it) }
    fun diagY(k: Int): Vec = generateVec(m) { getOrZero(k - it, it) }

    fun indices(): Sequence<IntPos2D> =
        (0..<n).asSequence().flatMap { i -> (0..<m).asSequence().map { j -> IntPos2D(i, j) } }

    fun values(): Sequence<Pair<IntPos2D, T>> =
        indices().map { it to get(it) }

    val rows get() = (0..<n).asSequence().map { row(it) }
    val cols get() = (0..<m).asSequence().map { col(it) }
    val diagsX get() = (-m + 1..<n).asSequence().map { diagX(it) }
    val diagsY get() = (0..<n + m - 1).asSequence().map { diagY(it) }
}

class CharMatrix(
    val data: CharArray,
    override val zero: Char,
    override val n: Int, // #Rows
    override val m: Int, // #Columns
    override val iInc: Int, // iterator steps row
    override val jInc: Int, // iterator steps column
) : Matrix<Char, CharArray> {
    fun copy() = CharMatrix(data = data.copyOf(), zero = zero, n = n, m = m, iInc = iInc, jInc = jInc)

    fun slice(i: Int, j: Int, n: Int, m: Int) = CharMatrix(
        data = CharArray(n * m) { get(i + it % m, j + it / m) },
        zero = zero,
        n = n, m = m,
        iInc = m, jInc = 1,
    )

    override fun generateVec(size: Int, block: (Int) -> Char) = CharArray(size, block)
    override operator fun get(index: Int) = data[index]
    override operator fun set(index: Int, value: Char) = run { data[index] = value }

    fun find(c: Char) = data.indexOf(c).takeIf { it != -1 }?.let { indexToPos(it) }

    fun toString(separator: String) =
        rows.joinToString("\n") { row -> row.joinToString(separator) { it.toString() } }
}

class UByteMatrix(
    private val data: UByteArray,
    override val zero: UByte,
    override val n: Int, // #Rows
    override val m: Int, // #Columns
    override val iInc: Int, // iterator steps row
    override val jInc: Int, // iterator steps column
) : Matrix<UByte, UByteArray> {
    fun copy() = UByteMatrix(data = data.copyOf(), zero = zero, n = n, m = m, iInc = iInc, jInc = jInc)

    fun slice(i: Int, j: Int, n: Int, m: Int) = UByteMatrix(
        data = UByteArray(n * m) { get(i + it % m, j + it / m) },
        zero = zero,
        n = n, m = m,
        iInc = m, jInc = 1,
    )

    override fun generateVec(size: Int, block: (Int) -> UByte) = UByteArray(size, block)
    override operator fun get(index: Int) = data[index]
    override operator fun set(index: Int, value: UByte) = run { data[index] = value }

    fun find(c: UByte) = data.indexOf(c).takeIf { it != -1 }?.let { indexToPos(it) }
}

class LongArrayView(
    val data: LongArray,
    private val offset: Int = 0,
) {
    val size = data.size - offset
    fun first() = data[offset]
    fun dropFirst() = LongArrayView(data, offset + 1)
}

fun List<String>.toCharMatrix(): CharMatrix {
    val nofCols = map { it.length }.distinct().singleOrNull()
        ?: throw IllegalArgumentException("different column sizes")
    return CharMatrix(
        data = joinToString(separator = "").toCharArray(),
        zero = ' ',
        n = size,
        m = nofCols,
        iInc = nofCols,
        jInc = 1,
    )
}

fun List<List<UByte>>.toMatrix(): UByteMatrix {
    val nofCols = map { it.size }.distinct().singleOrNull()
        ?: throw IllegalArgumentException("different column sizes")
    return UByteMatrix(
        data = flatten().toUByteArray(),
        zero = 0.toUByte(),
        n = size,
        m = nofCols,
        iInc = nofCols,
        jInc = 1,
    )
}

fun String.count(pattern: String, withOverlapping: Boolean): Int {
    if (pattern.isEmpty()) throw IllegalArgumentException("pattern must not be empty")
    var pos = 0
    var found = 0
    while (pos <= length) {
        val p = indexOf(pattern, startIndex = pos)
        if (p == -1) break
        found++
        pos = p + if (withOverlapping) pattern.length else 1
    }
    return found
}

fun <T> CoroutineScope.launchWorker(
    nofWorkers: Int,
    block: suspend CoroutineScope.(Int) -> T,
): List<Deferred<T>> = (0..<nofWorkers).map { async { block(it) } }

private val thresholds = generateSequence(10L) { it * 10L }.take(18).toList().toLongArray()
fun Int.pow10() = thresholds[this - 1]
fun Long.countDigitsBase10(): Int {
    var low = 0
    var high = thresholds.size - 1
    while (low < high) {
        val mid = (low + high) / 2
        if (this < thresholds[mid]) high = mid else low = mid + 1
    }
    return low + 1
}
