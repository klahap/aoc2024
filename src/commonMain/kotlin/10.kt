package io.github.klahap


private val parsedData by lazy {
    fileReader("data/10.txt").lineSequence().toList().toCharMatrix()
}

private fun CharArray.findAll(c: Char) = indices.filter { get(it) == c }
private fun CharMatrix.getNeighbors(pos: IntPos2D, c: Char): List<Int> = listOf(
    pos + IntPos2D(1, 0),
    pos + IntPos2D(0, 1),
    pos - IntPos2D(1, 0),
    pos - IntPos2D(0, 1),
).filter { getOrNull(it) == c }.map { posToIndex(it) }

private fun <T> CharMatrix.stepTo(start: Map<Int, T>, c: Char): Map<Int, List<T>> =
    start.entries.flatMap { (pos, solutions) ->
        getNeighbors(indexToPos(pos), c).map { it to solutions }
    }.groupBy({ it.first }, { it.second })

data object Day10a : Task<Int>({
    parsedData.run {
        var state = data.findAll('9').associateWith { setOf(it) }
        for (c in '8' downTo '0')
            state = stepTo(state, c).mapValues { it.value.flatten().toSet() }
        state.values.sumOf { it.size }
    }
})

data object Day10b : Task<Int>({
    parsedData.run {
        var state = data.findAll('9').associateWith { 1 }
        for (c in '8' downTo '0')
            state = stepTo(state, c).mapValues { it.value.sum() }
        state.values.sum()
    }
})