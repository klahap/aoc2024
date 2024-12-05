package io.github.klahap

import kotlin.time.Duration.Companion.microseconds
import kotlin.time.measureTime

private val Task<*>.expectedResult
    get() = when (this) {
        Day01a -> 1603498
        Day01b -> 25574739
        Day02a -> 341
        Day02b -> 404
        Day03a -> 178538786
        Day03b -> 102467299
        Day04a -> 2517
        Day04b -> 1960
        Day05a -> 6384
        Day05b -> 5353
    }

fun main() {
    val tasks = Task::class.sealedSubclasses
        .mapNotNull { it.objectInstance }
        .sortedBy { it::class.simpleName }

    tasks.forEach {
        val actual = it.execute()
        val expected = it.expectedResult
        if (actual != expected)
            throw AssertionError("$actual != $expected")
    }

    val n = 100
    val executionDuration = (0..<n).sumOf {
        measureTime {
            tasks.forEach { it.execute(silent = true) }
        }.inWholeMicroseconds
    }.let { it / n }.microseconds
    println("execution duration: $executionDuration")
}