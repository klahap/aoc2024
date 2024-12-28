package io.github.klahap

private val parsedData: Pair<Set<Rule>, List<Pages>> by lazy {
    fileReader("data/05.txt").lineSequence()
        .filter(String::isNotBlank)
        .partition { it.contains("|") }
        .let { (rules, pages) ->
            val parsedRules = rules.map { it.split('|').map(String::toInt).toPair() }.toSet()
            val parsedPages = pages.map { it.split(',').map(String::toInt) }
            parsedRules to parsedPages
        }
}

private typealias Rule = Pair<Int, Int>
private typealias Pages = List<Int>

private val Pages.midElement get() = this[size / 2]

private fun Pages.sortedByRules(rules: Set<Rule>): Pages = sortedWith(rules.toComparator())
private fun Set<Rule>.toComparator() = Comparator<Int> { p0, p1 ->
    if (contains(Rule(p1, p0))) 1
    else -1
}

data object Day05a : Task<Int>({
    val (rules, pagesRow) = parsedData
    pagesRow.asSequence()
        .filter { it.sortedByRules(rules) == it }
        .sumOf { it.midElement }
})

data object Day05b : Task<Int>({
    val (rules, pagesRow) = parsedData
    pagesRow.asSequence()
        .mapNotNull { it.sortedByRules(rules).takeIf { sorted -> sorted != it } }
        .sumOf { it.midElement }
})