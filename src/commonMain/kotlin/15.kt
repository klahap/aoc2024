package io.github.klahap

private val parsedData by lazy {
    val (matrixRaw, movementsRaw) = fileReader("data/15.txt").readText().split("\n\n")
    val matrix = matrixRaw.split('\n').toCharMatrix()
    val movements = movementsRaw.replace("\n", "").map { Move.entries.first { m -> m.char == it } }
    matrix to movements.toTypedArray()
}

private enum class Move(val char: Char, val vec: IntPos2D) {
    UP('^', IntPos2D(-1, 0)),
    DOWN('v', IntPos2D(1, 0)),
    LEFT('<', IntPos2D(0, -1)),
    RIGHT('>', IntPos2D(0, 1)),
}

private fun Char.flipBoxSide() = if (this == '[') ']' else '['
private fun CharMatrix.computeResult(char: Char) = values()
    .sumOf { (pos, c) -> if (c == char) (pos.x * 100 + pos.y).toLong() else 0L }

private fun CharMatrix.canMoveBoxesHorizontal(pos: IntPos2D, move: Move): Int? {
    for (i in 2..Int.MAX_VALUE step 2)
        return when (this[pos + (move.vec * i)]) {
            '.' -> i
            '#' -> null
            else -> continue
        }
    error("endless loop")
}

private fun CharMatrix.moveBoxesHorizontal(pos: IntPos2D, move: Move, steps: Int) {
    val firstBoxSide = this[pos]
    this[pos] = '.'
    for (i in 1..steps)
        this[pos + (move.vec * i)] = if (i % 2 == 1) firstBoxSide else firstBoxSide.flipBoxSide()
}

private fun CharMatrix.canMoveBoxesVertical(pos: IntPos2D, move: Move): Boolean {
    val side = get(pos)
    val posOther = pos + (if (side == '[') Move.RIGHT.vec else Move.LEFT.vec)
    val newPos = pos + move.vec to posOther + move.vec
    val newPosChar = get(newPos.first) to get(newPos.second)
    if (newPosChar.first == '#' || newPosChar.second == '#') return false
    if (newPosChar.first == '[' || newPosChar.first == ']')
        if (!canMoveBoxesVertical(pos = newPos.first, move = move)) return false
    if (newPosChar.second == side)
        if (!canMoveBoxesVertical(pos = newPos.second, move = move)) return false
    return true
}

private fun CharMatrix.moveBoxesVertical(pos: IntPos2D, move: Move) {
    val side = get(pos)
    val posOther = pos + (if (side == '[') Move.RIGHT.vec else Move.LEFT.vec)
    val newPos = pos + move.vec to posOther + move.vec
    val newPosChar = get(newPos.first) to get(newPos.second)
    if (newPosChar.first == '[' || newPosChar.first == ']')
        moveBoxesVertical(pos = newPos.first, move = move)
    if (newPosChar.second == side)
        moveBoxesVertical(pos = newPos.second, move = move)
    set(pos, '.')
    set(posOther, '.')
    set(newPos.first, side)
    set(newPos.second, side.flipBoxSide())
}

private fun CharMatrix.moveBoxes(pos: IntPos2D, move: Move) = when (move) {
    Move.UP, Move.DOWN -> canMoveBoxesVertical(pos, move).also { if (it) moveBoxesVertical(pos, move) }
    Move.LEFT, Move.RIGHT -> canMoveBoxesHorizontal(pos, move)?.also { moveBoxesHorizontal(pos, move, it) } != null
}

data object Day15a : Task<Long>({
    val (matrixOld, movements) = parsedData
    val matrix = matrixOld.copy()
    var pos = matrix.find('@')!!
    matrix[pos] = '.'
    for (move in movements) {
        val newPos = pos + move.vec
        when (matrix[newPos]) {
            '.' -> pos = newPos
            '#' -> Unit
            'O' -> {
                var p = newPos
                while (true) {
                    p += move.vec
                    when (matrix[p]) {
                        '#' -> break
                        'O' -> continue
                        '.' -> {
                            matrix[p] = 'O'
                            matrix[newPos] = '.'
                            pos = newPos
                            break
                        }
                    }
                }
            }
        }
    }
    matrix.computeResult('O')
})

data object Day15b : Task<Long>({
    val (matrixOld, movements) = parsedData
    var pos = matrixOld.find('@')!!.let { IntPos2D(it.x, it.y * 2) }
    val matrix = matrixOld.rows.map { row ->
        row.joinToString("") {
            when (it) {
                '#' -> "##"
                'O' -> "[]"
                '.', '@' -> ".."
                else -> error("unexpected char")
            }
        }
    }.toList().toCharMatrix()

    for (move in movements) {
        val newPos = pos + move.vec
        when (matrix[newPos]) {
            '.' -> pos = newPos
            '#' -> Unit
            '[', ']' -> {
                if (matrix.moveBoxes(newPos, move))
                    pos = newPos
            }
        }
    }
    matrix.computeResult('[')
})
