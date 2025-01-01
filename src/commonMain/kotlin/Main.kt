package io.github.klahap

fun Task<*>.profile(n: Int = 100) {
    execute(silent = true)
    val duration = kotlin.time.measureTime {
        repeat(n) { execute(silent = true) }
    }.let { it / n }.toString().padStart(13, ' ')
    println("$taskName: $duration")
}

fun main() {
    println()
    Day01a.profile()
    Day01b.profile()
    Day02a.profile()
    Day02b.profile()
    Day03a.profile()
    Day03b.profile()
    Day04a.profile()
    Day04b.profile()
    Day05a.profile()
    Day05b.profile()
    Day06a.profile()
    Day06b.profile()
    Day07a.profile()
    Day07b.profile()
    Day08a.profile()
    Day08b.profile()
    Day09a.profile()
    Day09b.profile()
    Day10a.profile()
    Day10b.profile()
    Day11a.profile()
    Day11b.profile()
    Day12a.profile()
    Day12b.profile()
    Day13a.profile()
    Day13b.profile()
    Day14a.profile()
    Day14b.profile()
    Day15a.profile()
    Day15b.profile()
    Day16a.profile(n = 10) // slow :(
    Day16b.profile(n = 10) // slow :(
    Day17a.profile()
    Day17b.profile()
    Day18a.profile()
    Day18b.profile()
    Day19a.profile()
    Day19b.profile()
    Day20a.profile()
    Day20b.profile(n = 5) // slow :(
    Day21a.profile()
    Day21b.profile()
    Day22a.profile()
    Day22b.profile(n = 5) // slow :(
    Day23a.profile()
    Day23b.profile()
    Day24a.profile()
    Day24b.profile(n = 5) // slow :(
    Day25a.profile()
    println()
}