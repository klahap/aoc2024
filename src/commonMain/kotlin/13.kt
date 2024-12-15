package io.github.klahap

private val coordRegex = ".*?X[+=](\\d+), Y[+=](\\d+)".toRegex()
private val parsedData by lazy {
    fileReader("data/13.txt").readText().split("\n\n").map { machineStr ->
        val (a, b, prize) = machineStr.split("\n").map { line ->
            coordRegex.matchEntire(line)!!.let { LongPos2D(it.groupValues[1].toLong(), it.groupValues[2].toLong()) }
        }
        Machine(a = a, b = b, prize = prize)
    }
}

private data class Machine(
    val a: LongPos2D,
    val b: LongPos2D,
    val prize: LongPos2D,
) {
    fun computeTokens(): Long {
        val d = a.x * b.y - a.y * b.x // Cramer's rule
        val dx = prize.x * b.y - prize.y * b.x
        val dy = a.x * prize.y - a.y * prize.x
        return if (dx % d != 0L || dy % d != 0L) 0 else (dx / d) * 3 + (dy / d)
    }
}

data object Day13a : Task<Long>({
    parsedData.sumOf { it.computeTokens() }
})

data object Day13b : Task<Long>({
    val offset = LongPos2D(10000000000000L, 10000000000000L)
    parsedData.sumOf { it.copy(prize = it.prize + offset).computeTokens() }
})