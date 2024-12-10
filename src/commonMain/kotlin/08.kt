package io.github.klahap

import kotlin.math.min
import kotlin.math.max

private val parsedData by lazy { fileReader("data/08.txt").lineSequence().toList().toCharMatrix() }

private fun CharMatrix.count(anyGridPos: Boolean): Int = data.iterator().withIndex().asSequence()
    .filter { it.value != '.' }
    .groupBy({ it.value }, { it.index })
    .values.asSequence()
    .flatMap { indices ->
        indices.allCombinations().flatMap { (i1, i2) ->
            val (p1, p2) = indexToPos(i1) to indexToPos(i2)
            val direction = p2 - p1
            val maxStepsBackwards = min(
                max(p1.x / direction.x, (n - p1.x - 1) / -direction.x),
                max(p1.y / direction.y, (m - p1.y - 1) / -direction.y),
            )
            val maxStepsForwards = min(
                max(p1.x / -direction.x, (n - p1.x - 1) / direction.x),
                max(p1.y / -direction.y, (m - p1.y - 1) / direction.y),
            )
            if (anyGridPos)
                -maxStepsBackwards..maxStepsForwards
            else {
                listOfNotNull(
                    (-1).takeIf { maxStepsBackwards >= 1 },
                    2.takeIf { maxStepsForwards >= 2 },
                )
            }.map { posToIndex(p1 + (direction * it)) }
        }
    }
    .distinct().count()

data object Day08a : Task<Int>({ parsedData.count(anyGridPos = false) })
data object Day08b : Task<Int>({ parsedData.count(anyGridPos = true) })