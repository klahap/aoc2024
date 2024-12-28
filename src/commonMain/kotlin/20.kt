package io.github.klahap

private val parsedData by lazy {
    fileReader("data/20.txt").lineSequence().toList().toCharMatrix()
}

private fun CharMatrix.toCostMatrix() = toUIntMatrix {
    when (it) {
        '#' -> UInt.MAX_VALUE
        '.', 'S' -> UInt.MIN_VALUE
        'E' -> 1u
        else -> error("Invalid input")
    }
}.apply {
    var pos = find(1u)!!
    for (i in 2u..UInt.MAX_VALUE) {
        pos += Direction.entries.firstOrNull { get(pos + it.vec) == UInt.MIN_VALUE }?.vec ?: break
        set(pos, i)
    }
}

private fun CharMatrix.count(minSave: UInt, maxSize: UInt): Int {
    return toCostMatrix().run {
        values()
            .filter { (_, cost) -> cost in minSave..<UInt.MAX_VALUE }
            .flatMap { (pos, cost) ->
                (2u..maxSize).asSequence().flatMap { cheatSize ->
                    getNorm1CircleCoords(cheatSize.toInt()).mapNotNull { v ->
                        val cost2 = getOrNull(pos + v)?.takeIf { it != UInt.MAX_VALUE } ?: return@mapNotNull null
                        if (cost2 + cheatSize >= cost) return@mapNotNull null
                        cost - (cost2 + cheatSize)
                    }
                }
            }
    }.count { it >= minSave }
}

data object Day20a : Task<Int>({
    parsedData.count(minSave = 100u, maxSize = 2u)
})

data object Day20b : Task<Int>({
    parsedData.count(minSave = 100u, maxSize = 20u)
})