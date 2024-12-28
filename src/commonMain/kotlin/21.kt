package io.github.klahap

private val parsedData by lazy {
    fileReader("data/21.txt").lineSequence().toList()
}

private sealed interface PadButton {
    val pos: IntPos2D

    fun isValidWay(way: List<KeyPadButton>): Boolean {
        var p = pos
        for (w in way) {
            p += w.diff
            when (this) {
                is NumPadButton -> if (p == NumPadButton.EMPTY.pos) return false
                is KeyPadButton -> if (p == KeyPadButton.EMPTY.pos) return false
            }
        }
        return true
    }
}

private enum class NumPadButton(override val pos: IntPos2D, val char: Char) : PadButton {
    N7(IntPos2D(0, 0), '7'),
    N8(IntPos2D(1, 0), '8'),
    N9(IntPos2D(2, 0), '9'),
    N4(IntPos2D(0, 1), '4'),
    N5(IntPos2D(1, 1), '5'),
    N6(IntPos2D(2, 1), '6'),
    N1(IntPos2D(0, 2), '1'),
    N2(IntPos2D(1, 2), '2'),
    N3(IntPos2D(2, 2), '3'),
    EMPTY(IntPos2D(0, 3), ' '),
    N0(IntPos2D(1, 3), '0'),
    ENTER(IntPos2D(2, 3), 'A');
}

private enum class KeyPadButton(override val pos: IntPos2D, val diff: IntPos2D) : PadButton {
    EMPTY(pos = IntPos2D(0, 0), diff = IntPos2D(0, 0)),
    UP(pos = IntPos2D(1, 0), diff = IntPos2D(0, -1)),
    ENTER(pos = IntPos2D(2, 0), diff = IntPos2D(0, 0)),
    LEFT(pos = IntPos2D(0, 1), diff = IntPos2D(-1, 0)),
    DOWN(pos = IntPos2D(1, 1), diff = IntPos2D(0, 1)),
    RIGHT(pos = IntPos2D(2, 1), diff = IntPos2D(1, 0));
}

private fun IntPos2D.way(): List<KeyPadButton> {
    val keyH = if (x < 0) KeyPadButton.LEFT else KeyPadButton.RIGHT
    val keyV = if (y < 0) KeyPadButton.UP else KeyPadButton.DOWN
    val keys = List(x.abs) { keyH } + List(y.abs) { keyV }
    return keys
}

private fun Char.toNumPadButton() = NumPadButton.entries.first { it.char == this }
private fun <P : PadButton> P.wayTo(dst: P) = (dst.pos - pos).way()
private fun <P : PadButton> P.allWaysTo(dst: P): Set<List<KeyPadButton>> =
    wayTo(dst).permutations().distinct().filter(::isValidWay).toSet()

private class KRobot(
    val previous: KRobot?,
    val cache: MutableMap<Pair<KeyPadButton, KeyPadButton>, ULong> = mutableMapOf(),
)

private fun KRobot.getCost(way: List<NumPadButton>): ULong {
    var pSrc = NumPadButton.ENTER
    var costs = 0uL
    for (w in way) {
        costs += pSrc.allWaysTo(w).minOf(this::getWayCost)
        pSrc = w
    }
    return costs
}

private fun KRobot.getWayCost(way: List<KeyPadButton>): ULong {
    val previousRobot = previous ?: return (way.size + 1).toULong()
    var pSrc = KeyPadButton.ENTER
    var costs = 0uL
    for (w in way) {
        costs += previousRobot.getPathCost(pSrc, w)
        pSrc = w
    }
    return costs + previousRobot.getPathCost(pSrc, KeyPadButton.ENTER)
}

private fun KRobot.getPathCost(src: KeyPadButton, dst: KeyPadButton): ULong {
    if (src == dst) return 1uL
    return cache.getOrPut(src to dst) {
        src.allWaysTo(dst).minOf(::getWayCost)
    }
}

private fun List<String>.compute(nofRobots: Int): ULong {
    val robots = run {
        var robot = KRobot(previous = null)
        repeat(nofRobots) { robot = KRobot(previous = robot) }
        robot
    }
    return sumOf { number ->
        val cost = robots.getCost(number.map(Char::toNumPadButton))
        val code = number.substringBefore('A').toULong()
        cost * code
    }
}

data object Day21a : Task<ULong>({
    parsedData.compute(nofRobots = 2)
})

data object Day21b : Task<ULong>({
    parsedData.compute(nofRobots = 25)
})