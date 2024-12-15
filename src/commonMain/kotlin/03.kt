package io.github.klahap

private val rawData by lazy { fileReader("data/03.txt").readText() }

private fun String.compute() = "mul\\((\\d{1,3}),(\\d{1,3})\\)".toRegex().findAll(this)
    .sumOf { it.groupValues[1].toInt() * it.groupValues[2].toInt() }

data object Day03a : Task<Int>({
    rawData.compute()
})

data object Day03b : Task<Int>({
    rawData.split("do()")
        .sumOf { it.substringBefore("don't()").compute() }
})