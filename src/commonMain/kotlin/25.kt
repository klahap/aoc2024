package io.github.klahap

private val parsedData by lazy {
    fileReader("data/25.txt").readText().split("\n\n").asSequence()
        .map(String::toCharMatrix)
        .map { m ->
            val isLock = m[0, 0] == '#'
            isLock to m.cols.map { col -> (col.count { it == '#' } - 1).toUByte() }.toList().toUByteArray()
        }
        .partition { it.first }.toList().map { it.map { x -> x.second }.toSet() }.toPair()
}

data object Day25a : Task<Int>({
    parsedData.allCombinations().count { (a, b) -> a.zip(b).all { (x, y) -> x + y <= 5u } }
})