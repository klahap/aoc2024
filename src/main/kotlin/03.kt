package io.github.klahap

private val rawData get() = fileReader("03.txt").readText()

private fun String.compute() = "mul\\((\\d{1,3}),(\\d{1,3})\\)".toRegex().findAll(this)
    .sumOf { it.groupValues[1].toInt() * it.groupValues[2].toInt() }

private fun task1() = rawData.compute()

private fun task2() = rawData.split("do()")
    .sumOf { it.substringBefore("don't()").compute() }

fun main() {
    task1().let(::println)
    task2().let(::println)
}