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

    val first get() = data.first
    val second get() = data.second
}

@JvmInline
private value class Pages(val data: List<Int>) {
    val midElement get() = data[data.size / 2]
}

private fun Pages.invalidRules(validRules: Set<Rule>) = data.asSequence().flatMapIndexed { i1, p1 ->
    data.asSequence().take(i1).map { p0 -> Rule(p1, p0) }
}.filter { it in validRules }.toSet()

private fun Pages.isOrdered(rules: Set<Rule>) = invalidRules(rules).isEmpty()

private fun Pages.order(rules: Set<Rule>): Pages {
    val result = this.data.toMutableList()
    while (true) {
        val invalidRules = Pages(result).invalidRules(rules)
        if (invalidRules.isEmpty()) break
        invalidRules.forEach { result.swapElements(it.first, it.second) }
    }
    return Pages(result.toList())
}

object Day05a : Task<Int>({
    val (rules, pagesRow) = parsedData
    pagesRow.asSequence()
        .filter { it.isOrdered(rules) }
        .sumOf { it.midElement }
})

object Day05b : Task<Int>({
    val (rules, pagesRow) = parsedData
    pagesRow.asSequence()
        .filter { !it.isOrdered(rules) }
        .sumOf { it.order(rules).midElement }

})

fun main() {
    Day05a.execute()
    Day05b.execute()
}
