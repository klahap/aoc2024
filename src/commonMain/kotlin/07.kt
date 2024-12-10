package io.github.klahap

private val parsedData by lazy {
    fileReader("data/07.txt").lineSequence().map {
        val value = it.substringBefore(": ").toLong()
        val numbers = it.substringAfter(": ").split(' ').reversed().map(String::toLong).toLongArray()
        value to numbers
    }.toList()
}

private fun Long.removeSuffix(suffix: Long) = toString().removeSuffix(suffix.toString()).toLong()
private fun Long.unJoinPossible(x: Long) = this != x && toString().endsWith(x.toString())

private fun Long.check(numbers: LongArrayView, withJoins: Boolean): Boolean {
    val x = numbers.first()
    return when {
        this <= 0L -> false
        numbers.size == 1 -> this == x
        this % x == 0L && div(x).check(numbers.dropFirst(), withJoins) -> true
        withJoins && unJoinPossible(x) && removeSuffix(x).check(numbers.dropFirst(), withJoins) -> true
        minus(x).check(numbers.dropFirst(), withJoins) -> true
        else -> false
    }
}

private fun List<Pair<Long, LongArray>>.compute(withJoins: Boolean) =
    filter { (result, numbers) -> result.check(LongArrayView(numbers), withJoins = withJoins) }
        .sumOf { (result, _) -> result }

data object Day07a : Task<Long>({ parsedData.compute(withJoins = false) })
data object Day07b : Task<Long>({ parsedData.compute(withJoins = true) })