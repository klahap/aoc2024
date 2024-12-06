package io.github.klahap

import kotlin.math.absoluteValue

private val parsedData by lazy {
    fileReader("data/01.txt").lineSequence()
        .map {
            val e1 = it.substringBefore(' ').toInt()
            val e2 = it.substringAfterLast(' ').toInt()
            e1 to e2
        }
        .toList()
}

object Day01a : Task<Int>({
    parsedData.let { data ->
        val column1 = data.map { it.first }.sorted()
        val column2 = data.map { it.second }.sorted()
        column1.zip(column2)
    }.sumOf { (e1, e2) -> (e2 - e1).absoluteValue }
})

object Day01b : Task<Int>({
    parsedData.let { data ->
        val column1 = data.map { it.first }.groupingBy { it }.eachCount()
        val column2 = data.map { it.second }.groupingBy { it }.eachCount()
        column1.entries.sumOf { (x, count1) ->
            val count2 = column2[x] ?: 0
            x * count1 * count2
        }
    }
})