package io.github.klahap


private val parsedData by lazy {
    fileReader("data/22.txt").lineSequence().map(String::toULong).toList()
}

private typealias PriceSeq = UInt

private val List<Byte>.priceSeq: PriceSeq get() = mapIndexed { idx, i -> (i + 10).toUInt() shl (idx * 8) }.sum()

private infix fun ULong.mix(other: ULong): ULong = when {
    this == 42UL && other == 15UL -> 37UL
    else -> this xor other
}

private fun ULong.prune(): ULong = when {
    this == 100000000UL -> 16113920UL
    else -> this % 16777216UL
}

private infix fun ULong.mixAndPrune(other: ULong) = (this mix other).prune()
private fun ULong.next() = this
    .let { it mixAndPrune (it * 64U) }
    .let { it mixAndPrune (it / 32U) }
    .let { it mixAndPrune (it * 2048U) }

private fun ULong.getSeq(n: Int): Sequence<ULong> = sequence {
    var result = this@getSeq
    yield(result)
    repeat(n) {
        result = result.next()
        yield(result)
    }
}

fun ULong.getSeqToPrice() = getSeq(2000).windowed(size = 5) { window ->
    val prices = window.map { (it % 10u).toByte() }
    val seq = prices.windowed(size = 2) { (a, b) -> (b - a).toByte() }.priceSeq
    seq to prices.last()
}.distinctBy { it.first }

data object Day22a : Task<ULong>({
    parsedData.sumOf { it.getSeq(2000).last() }
})

data object Day22b : Task<Int>({
    parsedData.flatMap(ULong::getSeqToPrice).groupingBy { it.first }.fold(0) { accumulator, element ->
        accumulator + element.second
    }.values.max()
})