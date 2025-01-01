package io.github.klahap

private val instructionRegex = Regex("(?<in1>\\w{3}) (?<op>\\w{2,3}) (?<in2>\\w{3}) -> (?<out>\\w{3})")
private val parsedData by lazy {
    val (inputRaw, instructionsRaw) = fileReader("data/24.txt").readText().split("\n\n")
    val input = inputRaw.lineSequence().associate {
        val (name, value) = it.split(": ")
        name to (value.single() == '1')
    }
    val instructions = instructionsRaw.lineSequence().map { row ->
        instructionRegex.matchEntire(row)!!.groups.let {
            Instruction(
                in1 = it["in1"]!!.value,
                in2 = it["in2"]!!.value,
                out = it["out"]!!.value,
                operation = Instruction.Operation.valueOf(it["op"]!!.value),
            )
        }
    }.toSet()
    input to instructions
}

private val String.isFinalOutput get() = get(0) == 'z'
private val String.isFirstInput get() = get(0) == 'x' || get(0) == 'y'

private data class InstructionInput(var in1: Boolean? = null, var in2: Boolean? = null) {
    val validInputOrNull: Pair<Boolean, Boolean>? get() = in1?.let { it1 -> in2?.let { it2 -> it1 to it2 } }
}

private data class Instruction(
    val operation: Operation,
    val in1: String,
    val in2: String,
    val out: String,
) {
    enum class Operation(val function: Boolean.(Boolean) -> Boolean) {
        AND(Boolean::and),
        OR(Boolean::or),
        XOR(Boolean::xor),
    }

    fun compute(input: Pair<Boolean, Boolean>): Boolean = operation.function.invoke(input.first, input.second)
}

private fun List<Pair<Instruction, InstructionInput>>.update(updates: Map<String, Boolean>) {
    forEach { (inst, input) ->
        updates[inst.in1]?.let { input.in1 = it }
        updates[inst.in2]?.let { input.in2 = it }
    }
}

private fun Map<String, Boolean>.getNumber(c: Char) = entries.filter { it.key[0] == c }
    .sumOf { (key, value) -> if (value) (1UL shl key.substring(1).toInt()) else 0UL }

private fun execute(input: Map<String, Boolean>, instructions: Set<Instruction>): ULong {
    var instructionsWithValues = instructions.map { it to InstructionInput() }
    val output = mutableMapOf<String, Boolean>()
    instructionsWithValues.update(input)
    while (instructionsWithValues.isNotEmpty()) {
        val updates = mutableMapOf<String, Boolean>()
        instructionsWithValues = instructionsWithValues.filter { (inst, input) ->
            val validInput = input.validInputOrNull ?: return@filter true
            updates[inst.out] = inst.compute(validInput)
            false
        }
        instructionsWithValues.update(updates)
        output += updates.filterKeys { it.isFinalOutput }
    }
    return output.getNumber('z')
}

private fun Set<Instruction>.getPrevInstructions(instruction: Instruction): List<Instruction> {
    return listOf(instruction) +
            filter { it.out == instruction.in1 }.flatMap { getPrevInstructions(it) } +
            filter { it.out == instruction.in2 }.flatMap { getPrevInstructions(it) }
}

private fun Set<Instruction>.getNextInstructions(input: String): List<Instruction> =
    filter { it.in1 == input || it.in2 == input }.flatMap { listOf(it) + getNextInstructions(it.out) }

private fun keyZ(n: Int) = "z${n.toString().padStart(2, '0')}"
private fun keyX(n: Int) = "x${n.toString().padStart(2, '0')}"
private fun keyY(n: Int) = "y${n.toString().padStart(2, '0')}"

data object Day24a : Task<ULong>({
    val (input, instructions) = parsedData
    execute(input = input, instructions = instructions)
})

data object Day24b : Task<String>({
    val (_, instructions) = parsedData
    val outputDependencies = instructions
        .associate { it.out to instructions.getPrevInstructions(it).toSet() }
    val inputDependencies = instructions.flatMap { listOf(it.in1, it.in2) }
        .associateWith { instructions.getNextInstructions(it).toSet() }
    val direct = (0..44).associateWith {
        outputDependencies["z${it.toString().padStart(2, '0')}"]!!.intersect(
            inputDependencies["x${it.toString().padStart(2, '0')}"]!! +
                    inputDependencies["y${it.toString().padStart(2, '0')}"]!!
        )
    }
    val carries = (0..44).associateWith {
        outputDependencies[keyZ(it + 1)]!!
            .intersect(inputDependencies[keyX(it)]!! + inputDependencies[keyY(it)]!!)
    }
    val directCarryIntersect = (1..44).associateWith {
        direct[it]!!.intersect(carries[it]!!)
    }
    val errorsType1 = directCarryIntersect.entries
        .filter { it.value.singleOrNull()?.operation == Instruction.Operation.XOR }
        .associate { it.key to it.value.single() }
        .filter {
            inputDependencies[it.value.out]!!.intersect(outputDependencies[keyZ(it.key)]!!)
                .singleOrNull()?.operation != Instruction.Operation.XOR
        }.map { (idx, inst) ->
            val expectedInst = instructions
                .single { it.operation == Instruction.Operation.XOR && (it.in1 == inst.out || it.in2 == inst.out) }
            expectedInst.out to keyZ(idx)
        }
    val errorsType2 = directCarryIntersect
        .filter { it.value.size == 1 && it.value.single().operation != Instruction.Operation.XOR }
        .map { (idx, _) ->
            val inputs = setOf(keyX(idx), keyY(idx))
            val (instA, instB) = instructions.filter { it.in1 in inputs && it.in2 in inputs }
            instA.out to instB.out
        }
    val errorsType3 = directCarryIntersect
        .filter { it.value.isEmpty() }
        .mapNotNull { (idx, _) ->
            val inputs = setOf(keyX(idx), keyY(idx))
            val inInst = instructions.filter { it.in1 in inputs && it.in2 in inputs }
            if (inInst.none { it.out == keyZ(idx) }) return@mapNotNull null
            val out = inInst.singleOrNull { it.operation == Instruction.Operation.XOR }?.out ?: return@mapNotNull null
            val wrongOut = instructions.singleOrNull {
                it.operation == Instruction.Operation.XOR && (it.in1 == out || it.in2 == out)
            }?.out ?: return@mapNotNull null
            wrongOut to keyZ(idx)
        }
    (errorsType1 + errorsType2 + errorsType3).flatMap { it.toList() }.sorted().joinToString(",")
})