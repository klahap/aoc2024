package io.github.klahap

import kotlinx.io.readString

private val rawData by lazy { fileReader("data/03.txt").readString() }

private fun String.compute() = "mul\\((\\d{1,3}),(\\d{1,3})\\)".toRegex().findAll(this)
    .sumOf { it.groupValues[1].toInt() * it.groupValues[2].toInt() }

object Day03a : Task<Int>({
    rawData.compute()
})

object Day03b : Task<Int>({
    rawData.split("do()")
        .sumOf { it.substringBefore("don't()").compute() }
})