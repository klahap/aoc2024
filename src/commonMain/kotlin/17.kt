package io.github.klahap

private val parsedData by lazy {
    val (ra, rb, rc, _, code) = fileReader("data/17.txt").lineSequence().toList()
    val register = Register(
        a = ra.substringAfterLast(' ').toULong(),
        b = rb.substringAfterLast(' ').toULong(),
        c = rc.substringAfterLast(' ').toULong(),
    )
    val program = code.substringAfterLast(' ').split(',').map(String::toInt).chunked(2)
        .map { (inst, lit) -> Inst.entries[inst] to lit }
    register to program
}

data class Register(
    var a: ULong,
    var b: ULong = 0uL,
    var c: ULong = 0uL,
)

private enum class Inst(val block: InstPointer.() -> Unit) {
    ADV({ register.a = register.a shr combo.toInt(); p++ }),
    BXL({ register.b = register.b xor literal; p++ }),
    BST({ register.b = combo % 8u; p++ }),
    JNZ({ if (register.a == 0UL) p++ else p = literal.toInt() / 2 }),
    BXC({ register.b = register.b xor register.c; p++ }),
    OUT({ output.add((combo % 8u).toInt()); p++ }),
    BDV({ register.b = register.a shr combo.toInt(); p++ }),
    CDV({ register.c = register.a shr combo.toInt(); p++ }),
}

private data class InstPointer(
    val register: Register,
    val program: List<Pair<Inst, Int>>,
) {
    var p: Int = 0
    val output: MutableList<Int> = mutableListOf()
    val opcode get() = program[p].first
    val literal get() = program[p].second.toULong()
    val combo
        get() = when (val l = literal) {
            0uL, 1uL, 2uL, 3uL -> l
            4uL -> register.a
            5uL -> register.b
            6uL -> register.c
            else -> error("Invalid combo operand")
        }

    fun run(): List<Int> {
        while (p < program.size)
            opcode.block(this)
        return output.toList()
    }
}

private fun List<Pair<Inst, Int>>.execute(registerA: ULong) =
    InstPointer(register = Register(registerA), program = this).run()

data object Day17a : Task<String>({
    val (register, code) = parsedData
    code.execute(register.a).joinToString(",", transform = Int::toString)
})

data object Day17b : Task<ULong>({
    val (_, code) = parsedData
    val expectedOutput = code.flatMap { listOf(it.first.ordinal, it.second) }
    var results = listOf(0uL)
    repeat(expectedOutput.size) { idx ->
        results = results.flatMap { prevA ->
            val expectedPartialOutput = expectedOutput.takeLast(idx + 1)
            (0u..<8u).mapNotNull {
                ((prevA shl 3) + it).takeIf { registerA ->
                    code.execute(registerA) == expectedPartialOutput
                }
            }
        }
    }
    results.min()
})