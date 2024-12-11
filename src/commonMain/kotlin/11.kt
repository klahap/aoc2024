package io.github.klahap

import kotlinx.io.readString

private val parsedData by lazy {
    fileReader("data/11.txt").readString().split(' ').map(String::toLong)
}

private fun Map<Long, Long>.blink() = entries.flatMap { (stone, count) ->
    if (stone == 0L) return@flatMap listOf(1L to count)
    val stoneStr = stone.toString()
    if (stoneStr.length % 2 == 0)
        listOf(
            stoneStr.take(stoneStr.length / 2).toLong() to count,
            stoneStr.drop(stoneStr.length / 2).toLong() to count,
        )
    else
        listOf((stone * 2024) to count)
}.groupingBy { it.first }.fold(0L) { acc, (_, count) -> acc + count }

data object Day11a : Task<Long>({
    var state = parsedData.associateWith { 1L }
    repeat(25) { state = state.blink() }
    state.values.sum()
})

data object Day11b : Task<Long>({
    var state = parsedData.associateWith { 1L }
    repeat(75) { state = state.blink() }
    state.values.sum()
})