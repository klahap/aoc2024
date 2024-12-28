package io.github.klahap

private val parsedData by lazy {
    fileReader("data/01.txt").lineSequence().toList()
        .map { it.split(' ').mapNotNull(String::toIntOrNull).toPair() }
}

data object Day01a : Task<Int>({
    parsedData.run {
        val column1 = map { it.first }.sorted()
        val column2 = map { it.second }.sorted()
        column1.zip(column2)
    }.sumOf { (e1, e2) -> (e2 - e1).abs }
})

data object Day01b : Task<Int>({
    parsedData.run {
        val column1 = map { it.first }.groupingBy { it }.eachCount()
        val column2 = map { it.second }.groupingBy { it }.eachCount()
        column1.entries.sumOf { (x, count1) ->
            val count2 = column2[x] ?: 0
            x * count1 * count2
        }
    }
})