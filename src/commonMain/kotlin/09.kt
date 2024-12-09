package io.github.klahap

import kotlinx.io.readString

private val parsedData: IntArray by lazy {
    fileReader("data/09.txt").readString()
        .map(Char::toString).map(String::toInt).toIntArray()
}

private typealias Block = ULong

private val Block.index get() = ((this shr 32) and 0x00000000FFFFFFFFUL).toInt()
private val Block.value get() = ((this shr 4) and 0x000000000FFFFFFFUL).toInt()
private val Block.size get() = (this and 0x0FUL).toInt()
private fun block(index: Int, size: Int, value: Int) =
    (index.toULong() shl 32) or (value.toULong() shl 4) or size.toULong()

private fun Block.sum() = (0 until size).sumOf { (it + index) * value.toLong() }

object Day09a : Task<Long>({
    val data = parsedData
    val length = (0..<data.size step 2).sumOf { data[it] }
    val backwardSequence = (data.size - 1 downTo 0 step 2).asSequence().flatMap { index ->
        (0..<data[index]).map { index / 2 }
    }.iterator()
    sequence {
        var i = 0
        while (true) {
            (0..<data[i]).forEach { yield(i / 2) }
            i++
            (0..<data[i]).forEach { yield(backwardSequence.next()) }
            i++
        }
    }.take(length).withIndex().sumOf { (it.index * it.value).toLong() }
})

object Day09b : Task<Long>({
    val data = run {
        var index = 0
        parsedData.mapIndexed { idx, size ->
            block(index = index, size = size, value = if (idx % 2 == 0) idx / 2 else 0)
                .also { index += size }
        }.toTypedArray()
    }
    val latestInsert = mutableMapOf<Int, Int>()
    (data.size - 1 downTo 0 step 2).asSequence().map lastBlockMap@{ idxLastBlock ->
        val lastBlock = data[idxLastBlock]
        if (lastBlock.size == 0) return@lastBlockMap lastBlock
        val startSearchId = (1..lastBlock.size).maxOf { latestInsert[it] ?: 1 }
        (startSearchId..<idxLastBlock step 2).asSequence().mapNotNull { idxEmptyBlock ->
            val emptyBlock = data[idxEmptyBlock]
            if (emptyBlock.size < lastBlock.size) return@mapNotNull null
            latestInsert[lastBlock.size] = idxEmptyBlock
            val n = lastBlock.size
            data[idxEmptyBlock] = block(
                index = emptyBlock.index + n, size = emptyBlock.size - n, value = emptyBlock.value
            )
            block(index = emptyBlock.index, size = n, value = lastBlock.value)
        }.firstOrNull() ?: run {
            latestInsert[lastBlock.size] = idxLastBlock
            lastBlock
        }
    }.sumOf { block -> block.sum() }
})