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
        Day07b.execute() shouldBe  472290821152397L
    }
}