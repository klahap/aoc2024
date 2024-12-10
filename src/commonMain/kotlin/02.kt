package io.github.klahap

import kotlin.collections.component1
import kotlin.collections.component2


private val parsedData by lazy {
    fileReader("data/02.txt").lineSequence()
        .map { it.split("\\s+".toRegex()).map(String::toInt) }
        .toList()
}

private fun List<Int>.isValid(): Boolean {
    val diffs = windowed(2) { (a, b) -> b - a }
    val isValidIncreasing = diffs.all { it in +1..3 }
    val isValidDecreasing = diffs.all { it in -3..-1 }
    return isValidIncreasing || isValidDecreasing
}

data object Day02a : Task<Int>({
    parsedData.count { it.isValid() }
})

data object Day02b : Task<Int>({
    parsedData.count { line ->
        sequence {
            yield(line)
            for (i in 0..<(line.size))
                yield(line.toMutableList().apply { removeAt(i) })
        }.any { it.isValid() }
    }
})
