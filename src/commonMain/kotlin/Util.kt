package io.github.klahap

import io.github.klahap.Direction.*
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
import kotlin.math.absoluteValue

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

val Int.abs get() = absoluteValue

fun <T> List<T>.allCombinations(): Sequence<Pair<T, T>> =
    asSequence().flatMapIndexed { idx, x -> asSequence().drop(idx + 1).map { y -> x to y } }

fun <T, R> Pair<Collection<T>, Collection<R>>.allCombinations(): Sequence<Pair<T, R>> =
    first.asSequence().flatMap { x -> second.asSequence().map { y -> x to y } }

sealed interface Pos2D<T> {
    val x: T
    val y: T
}

data class IntPos2D(override val x: Int, override val y: Int) : Pos2D<Int> {
    fun rotate90() = IntPos2D(y, -x)
    operator fun plus(other: Pos2D<Int>) = IntPos2D(x + other.x, y + other.y)
    operator fun minus(other: Pos2D<Int>) = IntPos2D(x - other.x, y - other.y)
    operator fun times(other: Int) = IntPos2D(x * other, y * other)
    val norm1 get() = x.absoluteValue + y.absoluteValue
}

data class LongPos2D(override val x: Long, override val y: Long) : Pos2D<Long> {
    fun rotate90() = LongPos2D(y, -x)
    operator fun plus(other: Pos2D<Long>) = LongPos2D(x + other.x, y + other.y)
    operator fun minus(other: Pos2D<Long>) = LongPos2D(x - other.x, y - other.y)
    operator fun times(other: Int) = LongPos2D(x * other, y * other)
}

sealed interface Matrix<T : Any> {
    val n: Int
    val m: Int
    val iInc: Int
    val jInc: Int

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

    operator fun get(pos: IntPos2D): T = get(pos.x * iInc + pos.y * jInc)
    fun getOrNull(pos: IntPos2D): T? = getOrNull(pos.x, pos.y)

    operator fun set(index: Int, value: T)
    operator fun set(i: Int, j: Int, value: T) = set(i * iInc + j * jInc, value)
    operator fun set(pos: IntPos2D, value: T) = set(pos.x * iInc + pos.y * jInc, value)
    fun setOrNull(pos: IntPos2D, value: T) = when {
        isValidPosition(pos.x, pos.y) -> set(pos.x * iInc + pos.y * jInc, value)
        else -> null
    }

    fun indices(): Sequence<IntPos2D> =
        (0..<n).asSequence().flatMap { i -> (0..<m).asSequence().map { j -> IntPos2D(i, j) } }

    fun values(): Sequence<Pair<IntPos2D, T>> =
        indices().map { it to get(it) }

}

sealed interface ZeroMatrix<T : Any> : Matrix<T> {
    val zero: T

    fun getOrZero(i: Int, j: Int): T = when {
        isValidPosition(i, j) -> get(i * iInc + j * jInc)
        else -> zero
    }

    fun getOrZero(pos: IntPos2D): T = getOrZero(pos.x, pos.y)
}

sealed interface VectorMatrix<T : Any, Vec : Any> : ZeroMatrix<T> {
    fun generateVec(size: Int, block: (Int) -> T): Vec

    fun row(i: Int): Vec = generateVec(m) { get(i, it) }
    fun col(j: Int): Vec = generateVec(n) { get(it, j) }
    fun diagX(k: Int): Vec = generateVec(m) { getOrZero(k + it, it) }
    fun diagY(k: Int): Vec = generateVec(m) { getOrZero(k - it, it) }

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
) : VectorMatrix<Char, CharArray> {
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
) : VectorMatrix<UByte, UByteArray> {
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

class UIntMatrix(
    val data: UIntArray,
    override val zero: UInt,
    override val n: Int, // #Rows
    override val m: Int, // #Columns
    override val iInc: Int, // iterator steps row
    override val jInc: Int, // iterator steps column
) : VectorMatrix<UInt, UIntArray> {
    override fun generateVec(size: Int, block: (Int) -> UInt) = UIntArray(size, block)
    override operator fun get(index: Int) = data[index]
    override operator fun set(index: Int, value: UInt) = run { data[index] = value }
    fun find(c: UInt) = data.indexOf(c).takeIf { it != -1 }?.let { indexToPos(it) }
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

class GenericMatrix<T : Any>(
    val data: Array<T>,
    override val n: Int, // #Rows
    override val m: Int, // #Columns
    override val iInc: Int, // iterator steps row
    override val jInc: Int, // iterator steps column
) : Matrix<T> {
    override operator fun get(index: Int) = data[index]
    override operator fun set(index: Int, value: T) = run { data[index] = value }
}

fun CharMatrix.toUIntMatrix(block: (Char) -> UInt) = UIntMatrix(
    data = UIntArray(data.size) { block(this[it]) },
    zero = 0u,
    n = n, m = m,
    iInc = iInc, jInc = jInc,
)

inline fun <reified T : Any> CharMatrix.toGenericMatrix(block: (Char) -> T) = GenericMatrix(
    data = Array(data.size) { block(this[it]) },
    n = n, m = m,
    iInc = iInc, jInc = jInc,
)

fun String.toCharMatrix() = split('\n').toCharMatrix()

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

enum class Direction(
    val vec: IntPos2D,
) {
    UP(IntPos2D(-1, 0)),
    RIGHT(UP.vec.rotate90()),
    DOWN(RIGHT.vec.rotate90()),
    LEFT(DOWN.vec.rotate90());

    fun rotate90() = when (this) {
        UP -> RIGHT
        RIGHT -> DOWN
        DOWN -> LEFT
        LEFT -> UP
    }

    fun rotate90Reverse() = when (this) {
        UP -> LEFT
        RIGHT -> UP
        DOWN -> RIGHT
        LEFT -> DOWN
    }
}

fun Char.toDirection(): Direction = when (this) {
    '^' -> UP
    'v' -> DOWN
    '<' -> LEFT
    '>' -> RIGHT
    else -> error("unknown direction '$this'")
}

fun <T> List<T>.toPair(): Pair<T, T> {
    if (size != 2) error("unexpected size: $size")
    return first() to last()
}

fun <T> List<T>.permutations(): Sequence<List<T>> =
    if (size <= 1) sequenceOf(this)
    else sequence {
        for (i in indices) {
            val element = this@permutations[i]
            val subList = this@permutations.take(i) + this@permutations.drop(i + 1)
            yieldAll(subList.permutations().map { listOf(element) + it })
        }
    }

class SortedMutableListDescending<T : Comparable<T>> {
    private val data: MutableList<T> = mutableListOf()
    val size: Int get() = data.size
    override fun toString(): String = data.toString()

    fun removeLast() = data.removeLast()
    fun isEmpty() = data.isEmpty()
    fun isNotEmpty() = data.isNotEmpty()

    fun add(element: T) {
        var i0 = 0
        var i1 = data.size
        while (i0 != i1) {
            val im = (i1 + i0) / 2
            val c = data[im].compareTo(element)
            when {
                c < 0 -> i1 = im
                c > 0 -> i0 = im + 1
                c == 0 -> {
                    i0 = im; break
                }
            }
        }
        data.add(i0, element)
    }
}

fun getNorm1CircleCoords(r: Int) = sequence {
    (-r..r).forEach { x ->
        val yAbs = r - x.absoluteValue
        yield(IntPos2D(x, yAbs))
        if (yAbs > 0) yield(IntPos2D(x, -yAbs))
    }
}

data class Tuple2<T0, T1>(val t0: T0, val t1: T1)
data class Tuple3<T0, T1, T2>(val t0: T0, val t1: T1, val t2: T2)
data class Tuple4<T0, T1, T2, T3>(val t0: T0, val t1: T1, val t2: T2, val t3: T3)
data class Tuple5<T0, T1, T2, T3, T4>(val t0: T0, val t1: T1, val t2: T2, val t3: T3, val t4: T4)
data class Tuple6<T0, T1, T2, T3, T4, T5>(val t0: T0, val t1: T1, val t2: T2, val t3: T3, val t4: T4, val t5: T5)
data class MTuple2<T0, T1>(var t0: T0, var t1: T1)
data class MTuple3<T0, T1, T2>(var t0: T0, var t1: T1, var t2: T2)
data class MTuple4<T0, T1, T2, T3>(var t0: T0, var t1: T1, var t2: T2, var t3: T3)
data class MTuple5<T0, T1, T2, T3, T4>(var t0: T0, var t1: T1, var t2: T2, var t3: T3, var t4: T4)
data class MTuple6<T0, T1, T2, T3, T4, T5>(var t0: T0, var t1: T1, var t2: T2, var t3: T3, var t4: T4, var t5: T5)

fun <T> Tuple2<T, T>.toList(): List<T> = listOf(t0, t1)
fun <T> Tuple3<T, T, T>.toList(): List<T> = listOf(t0, t1, t2)
fun <T> Tuple4<T, T, T, T>.toList(): List<T> = listOf(t0, t1, t2, t3)
fun <T> Tuple5<T, T, T, T, T>.toList(): List<T> = listOf(t0, t1, t2, t3, t4)
fun <T> Tuple6<T, T, T, T, T, T>.toList(): List<T> = listOf(t0, t1, t2, t3, t4, t5)
fun <T> MTuple2<T, T>.toList(): List<T> = listOf(t0, t1)
fun <T> MTuple3<T, T, T>.toList(): List<T> = listOf(t0, t1, t2)
fun <T> MTuple4<T, T, T, T>.toList(): List<T> = listOf(t0, t1, t2, t3)
fun <T> MTuple5<T, T, T, T, T>.toList(): List<T> = listOf(t0, t1, t2, t3, t4)
fun <T> MTuple6<T, T, T, T, T, T>.toList(): List<T> = listOf(t0, t1, t2, t3, t4, t5)

fun <T> MTuple4<T, T, T, T>.get(idx: Int): T = when (idx) {
    0 -> t0; 1 -> t1; 2 -> t2; 3 -> t3
    else -> throw IndexOutOfBoundsException()
}

fun <T> MTuple4<T, T, T, T>.set(idx: Int, value: T) = when (idx) {
    0 -> t0 = value; 1 -> t1 = value; 2 -> t2 = value; 3 -> t3 = value
    else -> throw IndexOutOfBoundsException()
}