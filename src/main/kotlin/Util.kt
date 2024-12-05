package io.github.klahap

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

fun fileReader(name: String) = Thread.currentThread().contextClassLoader
    .getResourceAsStream(name)!!.bufferedReader()

sealed interface Matrix<T : Any, Vec : Any> {
    val zero: T
    val n: Int
    val m: Int
    val iInc: Int
    val jInc: Int
    fun generateVec(size: Int, block: (Int) -> T): Vec

    fun getOrNull(i: Int, j: Int): T?
    fun getOrZero(i: Int, j: Int): T = getOrNull(i = i, j = j) ?: zero
    fun get(i: Int, j: Int): T = getOrNull(i = i, j = j) ?: throw IndexOutOfBoundsException()

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
    override fun generateVec(size: Int, block: (Int) -> Char) = CharArray(size, block)

    override fun getOrNull(i: Int, j: Int): Char? {
        return if (i !in 0..<n || j !in 0..<m)
            null
        else
            data[i * iInc + j * jInc]
    }
}

fun String.toCharMatrix(): CharMatrix {
    val rows = this.split('\n')
    val nofCols = rows.map { it.length }.distinct().singleOrNull()
        ?: throw IllegalArgumentException("different column sizes")
    return CharMatrix(
        data = rows.joinToString(separator = "").toCharArray(),
        zero = ' ',
        n = rows.size,
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
