package io.github.klahap

import kotlinx.io.readString

private val parsedData by lazy { fileReader("data/11.txt").readString().split(' ').map(String::toLong) }

private fun List<Long>.compute(steps: Int): Long = generateSequence(associateWith { 1L }) { state ->
    state.entries.flatMap { (stone, count) ->
        if (stone == 0L) return@flatMap listOf(1L to count)
        val nofStoneDigits = stone.countDigitsBase10()
        if (nofStoneDigits % 2 == 1) return@flatMap listOf((stone * 2024) to count)
        val divider = (nofStoneDigits / 2).pow10()
        listOf(
            (stone / divider) to count,
            (stone % divider) to count,
        )
    }.groupingBy { it.first }.fold(0L) { acc, (_, count) -> acc + count }
}.take(steps + 1).last().values.sum()

data object Day11a : Task<Long>({ parsedData.compute(25) })
data object Day11b : Task<Long>({ parsedData.compute(75) })