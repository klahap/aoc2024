package io.github.klahap

private val parsedData by lazy {
    fileReader("data/18.txt").lineSequence()
        .map { row ->
            val (a, b) = row.split(',').map(String::toInt)
            IntPos2D(b, a)
        }.toList()
}

private const val n = 70
private const val nofBytes = 1024

private fun getMemory() = (0..<n + 1).map { ".".repeat(n + 1) }.toCharMatrix().also { matrix ->
    parsedData.take(nofBytes).forEach { matrix[it] = '#' }
}

private data class Step18(
    val pos: IntPos2D,
    val cost: Int,
) : Comparable<Step18> {
    override fun compareTo(other: Step18) = cost.compareTo(other.cost)
}

private class Clusters {
    var nextGroupId = 0
    val posToGroupId = mutableMapOf<IntPos2D, Int>()
    val groupIdToPos = mutableMapOf<Int, MutableSet<IntPos2D>>()

    fun isInSameGroup(a: IntPos2D, b: IntPos2D): Boolean {
        return (posToGroupId[a] ?: return false) == posToGroupId[b]
    }

    fun add(pos: IntPos2D) {
        val groups = posToGroupId.filterKeys { (it - pos).norm1 == 1 }.values.toSet()
        when (groups.size) {
            0 -> {
                val groupId = nextGroupId++
                posToGroupId[pos] = groupId
                groupIdToPos[groupId] = mutableSetOf(pos)
            }

            1 -> {
                val groupId = groups.single()
                posToGroupId[pos] = groupId
                groupIdToPos[groupId]!!.add(pos)
            }

            else -> {
                val groupId = groups.first()
                val allPos = groups.flatMap { groupIdToPos.remove(it)!! } + listOf(pos)
                allPos.forEach { posToGroupId[it] = groupId }
                groupIdToPos[groupId] = allPos.toMutableSet()
            }
        }
    }
}

data object Day18a : Task<Int>({
    val memory = getMemory()
    val startPos = IntPos2D(0, 0)
    val endPos = IntPos2D(memory.n - 1, memory.m - 1)
    val queue = SortedMutableListDescending<Step18>()
    queue.add(Step18(pos = endPos, cost = 0))
    memory[endPos] = '#'
    run {
        while (queue.isNotEmpty()) {
            val step = queue.removeLast()
            if (step.pos == startPos) return@run step.cost
            memory[step.pos] = '#'
            Direction.entries
                .map { step.pos + it.vec }
                .filter { memory.getOrNull(it) == '.' }
                .forEach {
                    memory[it] = '#'
                    queue.add(Step18(pos = it, cost = step.cost + 1))
                }
        }
        error("nof path found")
    }
})

data object Day18b : Task<String>({
    val allCorrupted = parsedData
    val clusters = Clusters().apply {
        val allPos = (0..n).flatMap { x -> (0..n).map { y -> IntPos2D(x, y) } }.toSet()
        (allPos - allCorrupted.toSet()).forEach { add(it) }
    }
    allCorrupted.reversed().first {
        clusters.add(it)
        clusters.isInSameGroup(IntPos2D(0, 0), IntPos2D(n, n))
    }.run { "$y,$x" }
})
