package io.github.klahap

import kotlin.time.measureTime


private fun Task<*>.profile() {
    val n = 100
    execute(silent = true)
    val duration = measureTime {
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
    println()
}