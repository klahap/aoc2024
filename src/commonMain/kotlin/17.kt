package io.github.klahap

private val parsedData by lazy {
    val (ra, rb, rc, _, code) = fileReader("data/17.txt").lineSequence().toList()
    Program.Register(
        a = ra.substringAfterLast(' ').toULong(),
        b = rb.substringAfterLast(' ').toULong(),
        c = rc.substringAfterLast(' ').toULong(),
    ) to code.substringAfterLast(' ').split(',').map(String::toInt).toIntArray()
}

private class Program(
    val register: Register,
    var p: Int = 0,
    val output: MutableList<UInt> = mutableListOf(),
    val code: IntArray,
) {
    data class Register(
        var a: ULong,
        var b: ULong,
        var c: ULong,
    )

    val opcode get() = code[p]
    val literal get() = code[p + 1].toULong()
    val combo
        get() = when (val l = literal) {
            0uL, 1uL, 2uL, 3uL -> l
            4uL -> register.a
            5uL -> register.b
            6uL -> register.c
            else -> error("Invalid combo operand")
        }

    val advValue get() = register.a / (1UL shl combo.toInt())

    fun run(): List<UInt> {
        while (p < code.size)
            instructions[opcode]()
        return output.toList()
    }
}

private val instructions = listOf<Program.() -> Unit>(
    { register.a = advValue; p += 2 }, // 0: adv
    { register.b = register.b xor literal; p += 2 }, // 1: bxl
    { register.b = combo % 8u; p += 2 }, // 2: bst
    { if (register.a == 0UL) p += 2 else p = literal.toInt() }, // 3: jnz
    { register.b = register.b xor register.c; p += 2 }, // 4: bxc
    { output.add(combo.toUInt() % 8u); p += 2 }, // 5: out
    { register.b = advValue; p += 2 }, // 6: bdv
    { register.c = advValue; p += 2 }, // 7: cdv
)

data object Day17a : Task<String>({
    val (register, code) = parsedData
    val result = Program(register = register.copy(), code = code).run().joinToString(",") { it.toString() }
    result
})

data object Day17b : Task<Int>({
    -1
})