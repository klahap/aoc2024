package io.github.klahap

import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.runBlocking

private val parsedData
    get() = fileReader("06.txt").lineSequence().map { line ->
        line.map { c ->
            when (c) {
                '^' -> State.VISIT_UP.mask
                '#' -> State.BLOCK.mask
                '.' -> State.NONE.mask
                else -> throw IllegalArgumentException()
            }
        }
    }.toList().toMatrix()

private enum class State(val value: Int) {
    NONE(-1),
    VISIT_UP(0),
    VISIT_DOWN(1),
    VISIT_RIGHT(2),
    VISIT_LEFT(3),
    BLOCK(4);

    val mask = (1 shl value).toUByte()
}

private fun UByte.hasState(state: State) = (this and state.mask) != State.NONE.mask
private fun UByte.isBlock() = hasState(State.BLOCK)
private fun UByte.hasNoState() = this == State.NONE.mask
private fun UByte.addState(state: State) = this or state.mask

private enum class Direction(
    val vec: IntPos2D,
    val state: State,
) {
    UP(IntPos2D(-1, 0), State.VISIT_UP),
    RIGHT(UP.vec.rotate90(), State.VISIT_RIGHT),
    DOWN(RIGHT.vec.rotate90(), State.VISIT_DOWN),
    LEFT(DOWN.vec.rotate90(), State.VISIT_LEFT);

    fun rotate90() = when (this) {
        UP -> RIGHT
        RIGHT -> DOWN
        DOWN -> LEFT
        LEFT -> UP
    }
}

private enum class StopReason {
    OUT_OF_BOUNDS,
    LOOP,
}

private inline fun UByteMatrix.walk(
    pos: IntPos2D,
    direction: Direction,
    stopCondition: (UByte, IntPos2D, Direction) -> StopReason?,
): StopReason {
    var pos = pos
    var direction = direction
    while (true) {
        val b = getOrNull(pos) ?: return StopReason.OUT_OF_BOUNDS
        stopCondition(b, pos, direction)?.let { return it }
        when {
            b.isBlock() -> {
                pos = pos - direction.vec
                direction = direction.rotate90()
            }

            else -> set(pos, b.addState(direction.state))
        }
        pos = pos + direction.vec
    }
}


object Day06a : Task<Int>({
    val matrix = parsedData
    var result = 1
    matrix.walk(
        pos = matrix.find(State.VISIT_UP.mask)!!,
        direction = Direction.UP,
    ) { b, _, _ ->
        if (b.hasNoState()) result++
        null
    }
    result
})

private data class CheckIsLoopJob(
    val matrix: UByteMatrix,
    val pos: IntPos2D,
    val direction: Direction,
) {
    fun execute(): Boolean {
        val stopReason = matrix.walk(
            pos = pos + direction.vec,
            direction = direction,
        ) { value, _, direction ->
            if (value.hasState(direction.state)) StopReason.LOOP else null
        }
        return stopReason == StopReason.LOOP
    }
}

@OptIn(DelicateCoroutinesApi::class)
object Day06b : Task<Int>({
    val matrix = parsedData
    var result = MutableStateFlow(0)

    runBlocking(Dispatchers.Default) {
        val jobs = Channel<CheckIsLoopJob>(capacity = 0)
        launchWorker(20) {
            for (job in jobs)
                if (job.execute())
                    result.update { it + 1 }
        }

        matrix.walk(
            pos = matrix.find(State.VISIT_UP.mask)!!,
            direction = Direction.UP,
        ) { value, pos, direction ->
            if (value.hasNoState()) {
                val job = CheckIsLoopJob(
                    matrix = matrix.copy().apply { set(pos, State.BLOCK.mask) },
                    pos = pos - direction.vec,
                    direction = direction.rotate90(),
                )
                jobs.send(job)
            }
            null
        }
        jobs.close()
    }
    result.value
})

fun main() {
    Day06a.execute()
    Day06b.execute()
}
