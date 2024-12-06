package io.github.klahap

import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.consumeAsFlow
import kotlinx.coroutines.flow.count

private val parsedData by lazy {
    fileReader("data/06.txt").lineSequence().map { line ->
        line.map { c ->
            when (c) {
                '^' -> State.VISIT_UP.mask
                '#' -> State.BLOCK.mask
                '.' -> State.NONE.mask
                else -> throw IllegalArgumentException()
            }
        }
    }.toList().toMatrix()
}

private enum class State(shift: Int) {
    NONE(-1),
    VISIT_UP(0),
    VISIT_DOWN(1),
    VISIT_RIGHT(2),
    VISIT_LEFT(3),
    BLOCK(4);

    val mask = (1 shl shift).toUByte()
}

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

private enum class StopReason { LOOP, OUT_OF_BOUNDS }

private fun UByte.hasState(state: State) = (this and state.mask) != State.NONE.mask
private fun UByte.isBlock() = hasState(State.BLOCK)
private fun UByte.hasNoState() = this == State.NONE.mask
private fun UByte.addState(state: State) = this or state.mask

private inline fun UByteMatrix.walk(
    pos: IntPos2D = find(State.VISIT_UP.mask)!!,
    direction: Direction = Direction.UP,
    stopCondition: UByteMatrix.(UByte, IntPos2D, Direction) -> StopReason?,
): StopReason {
    var pos = pos
    var direction = direction
    while (true) {
        val b = getOrNull(pos) ?: return StopReason.OUT_OF_BOUNDS
        stopCondition(b, pos, direction)?.let { return it }
        when {
            b.isBlock() -> {
                pos -= direction.vec
                direction = direction.rotate90()
            }

            else -> set(pos, b.addState(direction.state))
        }
        pos += direction.vec
    }
}

private fun UByteMatrix.isLoop(pos: IntPos2D, direction: Direction): Boolean {
    val stopReason = walk(pos = pos, direction = direction) { value, _, direction ->
        if (value.hasState(direction.state)) StopReason.LOOP else null
    }
    return stopReason == StopReason.LOOP
}

object Day06a : Task<Int>({
    var result = 1
    parsedData.copy().walk { b, _, _ ->
        if (b.hasNoState()) result++
        null
    }
    result
})

object Day06b : AsyncTask<Int>({
    val jobs = Channel<() -> Boolean>(capacity = Channel.UNLIMITED)
    val worker = launchWorker(nofWorkers = 100) {
        jobs.consumeAsFlow().count { job -> job() }
    }
    parsedData.copy().walk { value, pos, direction ->
        if (value.hasNoState()) {
            val searchMatrix = copy().apply { set(pos, State.BLOCK.mask) }
            jobs.send {
                searchMatrix.isLoop(
                    pos = pos - direction.vec + direction.rotate90().vec,
                    direction = direction.rotate90(),
                )
            }
        }
        null
    }
    jobs.close()
    worker.awaitAll().sum()
})