package io.github.klahap

private val parsedData by lazy {
    val raw = fileReader("data/19.txt").lineSequence().toList()
    raw.first().split(", ") to raw.drop(2)
}

private class TowelsSolver(
    val towels: List<String>,
    val cacheCount: MutableMap<String, ULong> = mutableMapOf(),
)

private fun TowelsSolver.validSubStrings(band: String): Sequence<String> =
    towels.asSequence().filter(band::startsWith).map(band::removePrefix)

private fun TowelsSolver.countCombinations(band: String): ULong = when {
    band.isEmpty() -> 1uL
    else -> cacheCount.getOrPut(band) { validSubStrings(band).sumOf(::countCombinations) }
}

private fun TowelsSolver.existsCombinations(band: String): Boolean = when {
    band.isEmpty() -> true
    else -> validSubStrings(band).any(::existsCombinations)
}

data object Day19a : Task<Int>({
    val (towels, bands) = parsedData
    TowelsSolver(towels).run { bands.count(::existsCombinations) }
})

data object Day19b : Task<ULong>({
    val (towels, bands) = parsedData
    TowelsSolver(towels).run { bands.sumOf(::countCombinations) }
})
