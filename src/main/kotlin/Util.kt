package io.github.klahap

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.runBlocking

@JvmInline
value class TaskName(val name: String) {
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

fun fileReader(name: String) = Thread.currentThread().contextClassLoader
    .getResourceAsStream(name)!!.bufferedReader()

sealed interface Pos2D<T> {
    val x: T
    val y: T
}

data class IntPos2D(override val x: Int, override val y: Int) : Pos2D<Int> {
    fun rotate90() = IntPos2D(y, -x)
    operator fun plus(other: Pos2D<Int>) = IntPos2D(x + other.x, y + other.y)
    operator fun minus(other: Pos2D<Int>) = IntPos2D(x - other.x, y - other.y)
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

    fun indexToPos(x: Int): IntPos2D = when {
        x !in 0..<m * n -> throw IndexOutOfBoundsException()
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

    fun row(i: Int): Vec = generateVec(m) { get(i, it) }
    fun col(j: Int): Vec = generateVec(n) { get(it, j) }
    fun diagX(k: Int): Vec = generateVec(m) { getOrZero(k + it, it) }
    fun diagY(k: Int): Vec = generateVec(m) { getOrZero(k - it, it) }

    fun indices(): Sequence<Pair<Int, Int>> =
        (0..<n).asSequence().flatMap { i -> (0..<n).asSequence().map { j -> i to j } }

    fun values(): Sequence<Pair<Pair<Int, Int>, T>> =
        indices().map { it to get(it.first, it.second) }

    val rows get() = (0..<n).asSequence().map { row(it) }
    val cols get() = (0..<m).asSequence().map { col(it) }
    val diagsX get() = (-m + 1..<n).asSequence().map { diagX(it) }
    val diagsY get() = (0..<n + m - 1).asSequence().map { diagY(it) }
}

class CharMatrix(
    private val data: CharArray,
    override val zero: Char,
    override val n: Int, // #Rows
    override val m: Int, // #Columns
    override val iInc: Int, // iterator steps row
    override val jInc: Int, // iterator steps column
) : Matrix<Char, CharArray> {
    fun copy() = CharMatrix(data = data.clone(), zero = zero, n = n, m = m, iInc = iInc, jInc = jInc)

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
