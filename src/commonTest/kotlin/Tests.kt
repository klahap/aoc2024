import io.github.klahap.*
import io.kotest.matchers.shouldBe
import kotlin.test.Test

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
}