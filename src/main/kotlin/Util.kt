package io.github.klahap

fun fileReader(name: String) = Thread.currentThread().contextClassLoader
    .getResourceAsStream(name)!!.bufferedReader()

class Matrix<T : Any>(
    private val data: List<T>,
    val zero: T,
    val n: Int, // #Rows
    val m: Int, // #Columns
    val iInc: Int, // iterator steps row
    val jInc: Int, // iterator steps column
) {
    fun getOrNull(i: Int, j: Int): T? {
        return if (i !in 0..<n || j !in 0..<m)
            null
        else
            data[i * iInc + j * jInc]
    }

    fun getOrZero(i: Int, j: Int): T = getOrNull(i = i, j = j) ?: zero
    fun get(i: Int, j: Int): T = getOrNull(i = i, j = j) ?: throw IndexOutOfBoundsException()
    fun row(i: Int): List<T> = List(m) { get(i, it) }
    fun col(j: Int): List<T> = List(n) { get(it, j) }
    fun diagX(k: Int): List<T> = List(m) { getOrZero(k + it, it) }
    fun diagY(k: Int): List<T> = List(m) { getOrZero(k - it, it) }

    fun indices(): Sequence<Pair<Int, Int>> =
        (0..<n).asSequence().flatMap { i -> (0..<n).asSequence().map { j -> i to j } }

    fun values(): Sequence<Pair<Pair<Int, Int>, T>> =
        indices().map { it to get(it.first, it.second) }

    val rows get() = (0..<n).asSequence().map { row(it) }
    val cols get() = (0..<m).asSequence().map { col(it) }
    val diagsX get() = (-m + 1..<n).asSequence().map { diagX(it) }
    val diagsY get() = (0..<n + m - 1).asSequence().map { diagY(it) }
}

fun String.toCharMatrix(): Matrix<Char> {
    val rows = this.split('\n')
    val nofCols = rows.map { it.length }.distinct().singleOrNull()
        ?: throw IllegalArgumentException("different column sizes")
    return Matrix<Char>(
        data = rows.joinToString(separator = "").toList(),
        zero = ' ',
        n = rows.size,
        m = nofCols,
        iInc = nofCols,
        jInc = 1,
    )
}

fun Iterable<Char>.concat() = this.joinToString(separator = "")

fun String.count(pattern: String): Int = split(pattern).size - 1