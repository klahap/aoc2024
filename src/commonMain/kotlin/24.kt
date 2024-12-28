package io.github.klahap


private val instructionRegex = Regex("(?<in1>\\w{3}) (?<op>\\w{2,3}) (?<in2>\\w{3}) -> (?<out>\\w{3})")

private val parsedData by lazy {
    val (inputRaw, instructionsRaw) = fileReader("data/24.txt").readText().split("\n\n")
    val input = inputRaw.lineSequence().associate {
        val (name, value) = it.split(": ")
        name.toInstructionVal() to (value.single() == '1')
    }
    val instructions = instructionsRaw.lineSequence().map { row ->
        instructionRegex.matchEntire(row)!!.groups.let {
            Instruction(
                in1 = it["in1"]!!.value.toInstructionVal(),
                in2 = it["in2"]!!.value.toInstructionVal(),
                out = it["out"]!!.value.toInstructionVal(),
                operation = when (it["op"]!!.value) {
                    "AND" -> Boolean::and
                    "OR" -> Boolean::or
                    "XOR" -> Boolean::xor
                    else -> error("Invalid operation")
                }
            )
        }
    }.toList()
    input to instructions
}

private typealias InstructionVal = UInt

private fun Boolean.toInstructionVal(): InstructionVal = if (this) 1u else 0u
private fun String.toInstructionVal(): InstructionVal =
    toList().reversed().mapIndexed { idx, value -> value.code.toUInt() shl ((idx + 1) * 8) }.sum()

private val InstructionVal.isOutput get() = (this shr (3 * 8)).toInt().toChar() == 'z'
private val InstructionVal.name
    get() = (3 downTo 1).map { (this shr (it * 8)).toUByte().toInt().toChar() }.joinToString("")

private data class Instruction(
    val operation: Boolean.(Boolean) -> Boolean,
    var in1: InstructionVal,
    var in2: InstructionVal,
    val out: InstructionVal,
) {
    val allValuesKnown get() = (in1 <= 1u) && (in2 <= 1u)

    fun compute(): Boolean {
        if (!allValuesKnown) error("All values must be known")
        return (in1 == 1u).operation(in2 == 1u)
    }
}

private fun List<Instruction>.update(name: InstructionVal, value: Boolean) {
    forEach {
        if (it.in1 == name) it.in1 = value.toInstructionVal()
        if (it.in2 == name) it.in2 = value.toInstructionVal()
    }
}

data object Day24a : Task<ULong>({
    val (input, instructionsInput) = parsedData
    var instructions = instructionsInput.toMutableList().toList()
    val output = mutableMapOf<InstructionVal, Boolean>()
    input.forEach { (name, value) -> instructions.update(name, value) }
    while (instructions.isNotEmpty()) {
        val (known, unknown) = instructions.partition { it.allValuesKnown }
        known.forEach {
            val name = it.out
            val value = it.compute()
            unknown.update(name, value)
            if (name.isOutput) output[name] = value
        }
        instructions = unknown
    }
    val result = output.map { it.key.name to it.value }.sortedBy { it.first }
        .mapIndexed { index, (_, value) -> if (value) (1UL shl index) else 0UL }
        .sum()
    result
})

data object Day24b : Task<Int>({
    -1
})