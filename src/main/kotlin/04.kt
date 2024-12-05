package io.github.klahap

private val matrix by lazy { fileReader("04.txt").readText().toCharMatrix() }

object Day04a : Task<Int>({
    matrix.run {
        sequence {
            yieldAll(rows)
            yieldAll(cols)
            yieldAll(diagsX)
            yieldAll(diagsY)
        }
    }
        .map { it.concat() }
        .sumOf { it.count("XMAS") + it.count("SAMX") }
})

object Day04b : Task<Int>({
    matrix.run {
        indices().count { (i, j) ->
            val d1 = (-1..1).map { getOrZero(i + it, j + it) }.concat()
            val d2 = (-1..1).map { getOrZero(i + it, j - it) }.concat()
            (d1 == "MAS" || d1 == "SAM") && ((d2 == "MAS" || d2 == "SAM"))
        }
    }
})

fun main() {
    Day04a.execute()
    Day04b.execute()
}