package io.github.klahap

private val matrix by lazy { fileReader("data/04.txt").lineSequence().toList().toCharMatrix() }

data object Day04a : Task<Int>({
    matrix.run {
        sequence {
            yieldAll(rows)
            yieldAll(cols)
            yieldAll(diagsX)
            yieldAll(diagsY)
        }
    }
        .map { it.concatToString() }
        .sumOf { it.count("XMAS", withOverlapping = false) + it.count("SAMX", withOverlapping = false) }
})

data object Day04b : Task<Int>({
    matrix.run {
        indices().count { (i, j) ->
            if (getOrZero(i, j) != 'A') return@count false
            val (x1, x2) = getOrZero(i - 1, j - 1) to getOrZero(i + 1, j + 1)
            if ((x1 != 'M' || x2 != 'S') && (x1 != 'S' || x2 != 'M')) return@count false
            val (y1, y2) = getOrZero(i - 1, j + 1) to getOrZero(i + 1, j - 1)
            if ((y1 != 'M' || y2 != 'S') && (y1 != 'S' || y2 != 'M')) return@count false
            true
        }
    }
})