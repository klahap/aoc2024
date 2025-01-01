import io.github.klahap.*
import kotlin.test.Test

private infix fun <T> T.shouldBe(expected: T) {
    if (this != expected) throw AssertionError("'$this' is not equal to expected value '$expected'")
}

class Tests {
    @Test
    fun day01() {
        Day01a.execute() shouldBe 1603498
        Day01b.execute() shouldBe 25574739
    }

    @Test
    fun day02() {
        Day02a.execute() shouldBe 341
        Day02b.execute() shouldBe 404
    }

    @Test
    fun day03() {
        Day03a.execute() shouldBe 178538786
        Day03b.execute() shouldBe 102467299
    }

    @Test
    fun day04() {
        Day04a.execute() shouldBe 2517
        Day04b.execute() shouldBe 1960
    }

    @Test
    fun day05() {
        Day05a.execute() shouldBe 6384
        Day05b.execute() shouldBe 5353
    }

    @Test
    fun day06() {
        Day06a.execute() shouldBe 5131
        Day06b.execute() shouldBe 1784
    }

    @Test
    fun day07() {
        Day07a.execute() shouldBe 5540634308362L
        Day07b.execute() shouldBe 472290821152397L
    }

    @Test
    fun day08() {
        Day08a.execute() shouldBe 336
        Day08b.execute() shouldBe 1131
    }

    @Test
    fun day09() {
        Day09a.execute() shouldBe 6385338159127
        Day09b.execute() shouldBe 6415163624282
    }

    @Test
    fun day10() {
        Day10a.execute() shouldBe 611
        Day10b.execute() shouldBe 1380
    }

    @Test
    fun day11() {
        Day11a.execute() shouldBe 199946L
        Day11b.execute() shouldBe 237994815702032L
    }

    @Test
    fun day12() {
        Day12a.execute() shouldBe 1344578
        Day12b.execute() shouldBe 814302
    }

    @Test
    fun day13() {
        Day13a.execute() shouldBe 29187L
        Day13b.execute() shouldBe 99968222587852L
    }

    @Test
    fun day14() {
        Day14a.execute() shouldBe 222062148L
        Day14b.execute() shouldBe 7520
    }

    @Test
    fun day15() {
        Day15a.execute() shouldBe 1514333L
        Day15b.execute() shouldBe 1528453L
    }

    @Test
    fun day16() {
        Day16a.execute() shouldBe 109496
        Day16b.execute() shouldBe 551
    }

    @Test
    fun day17() {
        Day17a.execute() shouldBe "6,2,7,2,3,1,6,0,5"
        Day17b.execute() shouldBe 236548287712877uL
    }

    @Test
    fun day18() {
        Day18a.execute() shouldBe 336
        Day18b.execute() shouldBe "24,30"
    }

    @Test
    fun day19() {
        Day19a.execute() shouldBe 233
        Day19b.execute() shouldBe 691316989225259uL
    }

    @Test
    fun day20() {
        Day20a.execute() shouldBe 1381
        Day20b.execute() shouldBe 982124
    }

    @Test
    fun day21() {
        Day21a.execute() shouldBe 206798uL
        Day21b.execute() shouldBe 251508572750680uL
    }

    @Test
    fun day22() {
        Day22a.execute() shouldBe 20401393616uL
        Day22b.execute() shouldBe 2272
    }

    @Test
    fun day23() {
        Day23a.execute() shouldBe 1314
        Day23b.execute() shouldBe "bg,bu,ce,ga,hw,jw,nf,nt,ox,tj,uu,vk,wp"
    }

    @Test
    fun day24() {
        Day24a.execute() shouldBe 53190357879014uL
        Day24b.execute() shouldBe "bks,hnd,nrn,tdv,tjp,z09,z16,z23"
    }

    @Test
    fun day25() {
        Day25a.execute() shouldBe 2586
    }
}