package io.github.klahap

private val parsedData: Pair<Set<Rule>, List<Pages>> by lazy {
    fileReader("05.txt").lineSequence()
        .filter(String::isNotBlank)
        .partition { it.contains("|") }
        .let { (rules, pages) ->
            val parsedRules = rules.map {
                Rule(
                    it.substringBefore('|').toInt(),
                    it.substringAfter('|').toInt(),
                )
            }.toSet()
            val parsedPages = pages.map { Pages(it.split(',').map(String::toInt)) }
            parsedRules to parsedPages
        }
}

@JvmInline
private value class Rule(val data: Pair<Int, Int>) {
    constructor(first: Int, second: Int) : this(first to second)
}

@JvmInline
private value class Pages(val data: List<Int>) {
    val midElement get() = data[data.size / 2]
}

private fun Set<Rule>.toComparator() = Comparator<Int> { p0, p1 ->
    if (contains(Rule(p1, p0))) 1
    else -1
}

private fun Pages.sortedByRules(rules: Set<Rule>) = Pages(data.sortedWith(rules.toComparator()))


object Day05a : Task<Int>({
    val (rules, pagesRow) = parsedData
    pagesRow.asSequence()
        .filter { it.sortedByRules(rules) == it }
        .sumOf { it.midElement }
})

object Day05b : Task<Int>({
    val (rules, pagesRow) = parsedData
    pagesRow.asSequence()
        .mapNotNull { it.sortedByRules(rules).takeIf { sorted -> sorted != it } }
        .sumOf { it.midElement }
})

fun main() {
    Day05a.execute()
    Day05b.execute()
}
