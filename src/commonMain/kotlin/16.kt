package io.github.klahap

private val parsedData by lazy {
    fileReader("data/16.txt").lineSequence().toList().toCharMatrix()
}

private val Matrix<*>.startPos get() = IntPos2D(n - 2, 1)
private val Matrix<*>.endPos get() = IntPos2D(1, m - 2)

private data class Job(
    val pos: IntPos2D,
    val direction: Direction,
    val cost: Int,
)

private typealias Costs = MTuple4<Int, Int, Int, Int>

private fun Costs.get(d: Direction) = get(d.ordinal)
private fun Costs.set(d: Direction, value: Int) = set(d.ordinal, value)

private const val COST_FORWARD = 1
private const val COST_ROTATE = 1000

private class MazeRun(
    private val maze: CharMatrix,
) {
    var minCost: Int = Int.MAX_VALUE
        private set
    private val costs: GenericMatrix<Costs> = maze.toGenericMatrix {
        MTuple4(Int.MAX_VALUE, Int.MAX_VALUE, Int.MAX_VALUE, Int.MAX_VALUE)
    }

    fun run() {
        val jobs = ArrayDeque<Job>().apply { add(Job(pos = maze.startPos, direction = Direction.RIGHT, cost = 0)) }
        while (jobs.isNotEmpty()) {
            val job = jobs.removeLast()
            if (job.cost > minCost) continue
            val char = maze[job.pos]
            if (char == '#') continue
            val posCosts = costs[job.pos]
            listOf(
                job.direction to job.cost,
                job.direction.rotate90() to job.cost + COST_ROTATE,
                job.direction.rotate90Reverse() to job.cost + COST_ROTATE,
            ).forEach { (d, c) ->
                val prevCosts = posCosts.get(d)
                when {
                    prevCosts <= c -> return@forEach
                    prevCosts > c -> posCosts.set(d, c)
                }
                if (char == 'E') {
                    if (c < minCost) minCost = c
                    return@forEach
                }
                if (maze[job.pos + d.vec] == '#') return@forEach
                jobs.add(Job(pos = job.pos + d.vec, direction = d, cost = c + COST_FORWARD))
            }
        }
    }

    fun countValidTiles(): Int {
        val jobs = ArrayDeque<Job>().apply {
            costs[maze.endPos]
                .let { allEndCosts -> Direction.entries.filter { allEndCosts.get(it) == minCost } }
                .map { Job(pos = maze.endPos, direction = it, cost = minCost) }
                .forEach(this::add)
        }
        var result = 0
        while (jobs.isNotEmpty()) {
            val job = jobs.removeLast()
            when (val c = maze[job.pos]) {
                'O', '#' -> continue
                'E', '.', 'S' -> {
                    maze[job.pos] = 'O'
                    result++
                    if (c == 'S') continue
                }

                else -> error("Unexpected maze value")
            }

            listOf(
                job.direction to COST_FORWARD,
                job.direction.rotate90() to COST_ROTATE + COST_FORWARD,
                job.direction.rotate90Reverse() to COST_ROTATE + COST_FORWARD,
            )
                .map { (d, c) -> Job(pos = job.pos - d.vec, direction = d, cost = job.cost - c) }
                .filter { costs[it.pos].get(it.direction) == it.cost }
                .forEach(jobs::add)
        }
        return result
    }
}

data object Day16a : Task<Int>({
    MazeRun(parsedData.copy()).also(MazeRun::run).minCost
})

data object Day16b : Task<Int>({
    MazeRun(parsedData.copy()).also(MazeRun::run).countValidTiles()
})