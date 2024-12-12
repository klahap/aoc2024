package io.github.klahap

private val parsedData by lazy { fileReader("data/12.txt").lineSequence().toList().toCharMatrix() }

private data class Region(
    val area: Int = 0,
    val perimeter: Int = 0,
) {
    val price get() = area * perimeter
    operator fun plus(other: Region) = Region(area = area + other.area, perimeter = perimeter + other.perimeter)
}

private fun Sequence<Region>.sum() = fold(Region()) { a, b -> a + b }

private val offsets = arrayOf(IntPos2D(1, 0), IntPos2D(0, 1), IntPos2D(-1, 0), IntPos2D(0, -1))

private fun CharMatrix.getRegionsWithNeighbours(pos: IntPos2D, origin: Char): Sequence<Region> = sequence {
    when (getOrNull(pos)) {
        origin.lowercaseChar() -> Unit
        origin -> {
            set(pos, origin.lowercaseChar())
            yield(Region(area = 1))
            for (offset in offsets)
                yieldAll(getRegionsWithNeighbours(pos + offset, origin = origin))
        }

        else -> yield(Region(perimeter = 1))
    }
}

private fun CharMatrix.getRegionsWithNeighbours2(pos: IntPos2D, origin: Char): Sequence<Region> = sequence {
    if (getOrNull(pos) != origin) return@sequence
    set(pos, origin.lowercaseChar())
    fun isNeighbour(offset: IntPos2D) = getOrNull(pos + offset)?.uppercaseChar() != origin
    val corners = offsets.count { offset ->
        val a = isNeighbour(offset)
        val b = isNeighbour(offset.rotate90())
        val c = isNeighbour(offset + offset.rotate90()) // to check if it's an inner corner
        (a && b) || (!a && !b && c)
    }
    yield(Region(area = 1, perimeter = corners))
    for (offset in offsets)
        yieldAll(getRegionsWithNeighbours2(pos + offset, origin = origin))
}

data object Day12a : Task<Int>({
    parsedData.copy().run {
        values().map { (pos, c) ->
            if (c.isLowerCase()) return@map Region()
            getRegionsWithNeighbours(pos, c).sum()
        }.sumOf(Region::price)
    }
})

data object Day12b : Task<Int>({
    parsedData.copy().run {
        values().map { (pos, c) ->
            if (c.isLowerCase()) return@map Region()
            getRegionsWithNeighbours2(pos, c).sum()
        }.sumOf(Region::price)
    }
})