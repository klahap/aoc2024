package io.github.klahap

private typealias Region = ULong

private val parsedData by lazy { fileReader("data/12.txt").lineSequence().toList().toCharMatrix() }
private val offsets = arrayOf(IntPos2D(1, 0), IntPos2D(0, 1), IntPos2D(-1, 0), IntPos2D(0, -1))

private fun region(area: UInt = 0u, perimeter: UInt = 0u): Region = (area.toULong() shl 32) + perimeter
private val Region.area: UInt get() = (this shr 32).toUInt()
private val Region.perimeter: UInt get() = ((this shl 32) shr 32).toUInt()
private val Region.price get() = area * perimeter


private fun CharMatrix.getRegionsWithNeighbours(pos: IntPos2D, origin: Char): Region {
    val c = getOrNull(pos)
    if (c == origin.lowercaseChar()) return region()
    if (c != origin) return region(perimeter = 1u)
    set(pos, origin.lowercaseChar())
    val neighbourResult = offsets.sumOf { getRegionsWithNeighbours(pos + it, origin = origin) }
    return region(area = 1u) + neighbourResult
}

private fun CharMatrix.getRegionsWithNeighbours2(pos: IntPos2D, origin: Char): Region {
    if (getOrNull(pos) != origin) return region()
    set(pos, origin.lowercaseChar())
    fun isNeighbour(offset: IntPos2D) = getOrNull(pos + offset)?.uppercaseChar() != origin
    val corners = offsets.count { offset ->
        val a = isNeighbour(offset)
        val b = isNeighbour(offset.rotate90())
        val c = isNeighbour(offset + offset.rotate90()) // to check if it's an inner corner
        (a && b) || (!a && !b && c)
    }
    val neighbourResult = offsets.sumOf { getRegionsWithNeighbours2(pos + it, origin = origin) }
    return region(area = 1u, perimeter = corners.toUInt()) + neighbourResult
}

data object Day12a : Task<Int>({
    parsedData.copy().run {
        values().sumOf { (pos, c) ->
            if (c.isLowerCase()) return@sumOf 0u
            getRegionsWithNeighbours(pos, c).price
        }.toInt()
    }
})

data object Day12b : Task<Int>({
    parsedData.copy().run {
        values().sumOf { (pos, c) ->
            if (c.isLowerCase()) return@sumOf 0u
            getRegionsWithNeighbours2(pos, c).price
        }.toInt()
    }
})