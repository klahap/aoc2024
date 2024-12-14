package io.github.klahap

private val regex = Regex("^p=(\\d+),(\\d+) v=(-?\\d+),(-?\\d+)\$")
private val parsedData by lazy {
    fileReader("data/14.txt").lineSequence().map { line ->
        val (x, y, vx, vy) = regex.matchEntire(line)!!.groupValues.drop(1).map(String::toInt)
        Robot(position = IntPos2D(x, y), velocity = IntPos2D(vx, vy))
    }.toList()
}
private const val width = 101
private const val height = 103

private data class Robot(val position: IntPos2D, val velocity: IntPos2D)

private fun Robot.move(sec: Int) = (position + (velocity * sec))
    .let { IntPos2D(it.x % width, it.y % height) }
    .let { IntPos2D(if (it.x < 0) width + it.x else it.x, if (it.y < 0) height + it.y else it.y) }

data object Day14a : Task<Long>({
    parsedData.map { it.move(100) }
        .fold(longArrayOf(0, 0, 0, 0)) { acc, pos ->
            val idx = when {
                pos.x < width / 2 -> 0
                pos.x > width / 2 -> 1
                else -> return@fold acc
            }
            val idy = when {
                pos.y < height / 2 -> 0
                pos.y > height / 2 -> 1
                else -> return@fold acc
            }
            acc.also { it[idx + idy * 2] += 1L }
        }.fold(1, Long::times)
})

data object Day14b : Task<Int>({
    (1..Int.MAX_VALUE).first { i ->
        val currentPoints = LinkedHashSet<IntPos2D>(parsedData.size)
        for (robot in parsedData)
            if (!currentPoints.add(robot.move(i)))
                return@first false
        true
    }
})