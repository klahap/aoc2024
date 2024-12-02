package io.github.klahap

fun main() = fileReader("02.txt").lineSequence()
    .filter { line ->
        val diffs = line
            .split("\\s+".toRegex())
            .map(String::toInt)
            .windowed(2) { (a, b) -> b - a }
        val isValidIncreasing = diffs.all { it in +1..+3 }
        val isValidDecreasing = diffs.all { it in -3..-1 }
        isValidIncreasing || isValidDecreasing
    }.count().let(::println)
