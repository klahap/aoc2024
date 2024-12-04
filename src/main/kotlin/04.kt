package io.github.klahap

private val matrix get() = fileReader("04.txt").readText().toCharMatrix()

private fun task1() = matrix.run {
    sequence {
        yieldAll(rows)
        yieldAll(cols)
        yieldAll(diagsX)
        yieldAll(diagsY)
    }
        .map { it.concat() }
        .sumOf { it.count("XMAS") + it.count("SAMX") }
}

private fun task2() = matrix.run {
    indices().count { (i, j) ->
        val d1 = (-1..1).map { getOrZero(i + it, j + it) }.concat()
        val d2 = (-1..1).map { getOrZero(i + it, j - it) }.concat()
        (d1 == "MAS" || d1 == "SAM") && ((d2 == "MAS" || d2 == "SAM"))
    }
}

fun main() {
    task1().let(::println)
    task2().let(::println)
}