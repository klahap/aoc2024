package io.github.klahap

import kotlin.math.absoluteValue

fun main() = fileReader("01.txt").lineSequence()
    .map {
        val e1 = it.substringBefore(' ').toInt()
        val e2 = it.substringAfterLast(' ').toInt()
        e1 to e2
    }
    .toList()
    .let { data ->
        val column1 = data.map { it.first }.sorted()
        val column2 = data.map { it.second }.sorted()
        column1.zip(column2)
    }
    .sumOf { (e1, e2) -> (e2 - e1).absoluteValue }
    .let(::println)
