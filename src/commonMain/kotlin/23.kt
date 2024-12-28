package io.github.klahap

private val parsedData by lazy {
    fileReader("data/23.txt").lineSequence()
        .map { it.split("-").map(String::toComputer).sorted().toPair() }
        .toSet().groupBy({ it.first }, { it.second }).mapValues { it.value.toSet() }
}

private typealias Computer = UShort

private val Computer.isChief get() = (this.toUInt() shr 8) == 0u
private val Computer.name
    get() = (1 downTo 0).map { (this.toUInt() shr (it * 8)).toUByte().toInt() }
        .joinToString("") { if (it == 0) "t" else it.toChar().toString() }

private fun String.toComputer(): Computer =
    reversed().mapIndexed { idx, c -> if (c == 't') 0u else (c.code.toUInt() shl (idx * 8)) }.sum().toUShort()

private class MaxGroupSolver(
    val connectionMap: Map<Computer, Set<Computer>>,
) {
    val cache = mutableMapOf<Set<Computer>, Set<Computer>>()
    fun solve() = connectionMap.maxGroup()

    private fun Set<Computer>.maxGroup(): Set<Computer> = when {
        isEmpty() -> emptySet()
        else -> cache.getOrPut(this) {
            associateWith {
                connectionMap[it]?.intersect(this) ?: emptySet()
            }.maxGroup()
        }
    }

    private fun Map<Computer, Set<Computer>>.maxGroup(): Set<Computer> = when {
        isEmpty() -> emptySet()
        size == 1 -> keys + values.single()
        else -> map { (key, value) -> setOf(key) + value.maxGroup() }.maxBy { it.size }
    }
}

data object Day23a : Task<Int>({
    val connectionMap = parsedData
    connectionMap.filterKeys(Computer::isChief).values.sumOf { n2All ->
        n2All.sumOf { n2 -> connectionMap[n2]?.count { n3 -> n2All.contains(n3) } ?: 0 }
    }
})

data object Day23b : Task<String>({
    MaxGroupSolver(parsedData).solve()
        .map(Computer::name).sorted()
        .joinToString(",")
})