package io.github.klahap

import kotlin.time.Duration
import kotlin.time.Duration.Companion.microseconds
import kotlin.time.Duration.Companion.seconds
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
        Day06a -> 5131
        Day06b -> 1784
    }

fun main() {
    val tasks = Task::class.sealedSubclasses
        .mapNotNull { it.objectInstance }
        .sortedBy { it::class.simpleName }

    tasks.forEach {
        val actual = it.execute(silent = true)
        val expected = it.expectedResult
        if (actual != expected)
            throw AssertionError("error in ${it.taskName}: $actual != $expected")
    }

    val n = 100
    mutableMapOf<Task<*>, Duration>().also { executionTimes ->
        (0..<n).forEach {
            tasks.forEach { task ->
                val d = measureTime { task.execute(silent = true) }
                executionTimes.compute(task) { _, p1 -> (p1 ?: 0.seconds) + d }
            }
        }
    }
        .mapValues { it.value / n }
        .entries.sortedBy { it.key.taskName.name }
        .onEach { (key, value) ->
            val prettyResult = key.expectedResult.toString().padStart(10, ' ')
            val prettyDuration = value.toString().padStart(13, ' ')
            println("${key.taskName}: $prettyResult in $prettyDuration")
        }.sumOf { it.value.inWholeMicroseconds }.microseconds
        .let { "execution duration: $it" }
}