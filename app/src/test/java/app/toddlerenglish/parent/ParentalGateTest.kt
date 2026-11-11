package app.toddlerenglish.parent

import kotlin.random.Random
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class ParentalGateTest {
    @Test
    fun aSimpleTapDoesNotOpenTheGate() {
        val gate = ParentalGate(Random(1))
        gate.holdStart(0)
        gate.holdEnd(200)
        assertEquals(ParentalGate.Step.Hold, gate.step)
        repeat(10) { gate.digit(it) }
        assertEquals(ParentalGate.Step.Hold, gate.step)
    }

    @Test
    fun anAdultOpensItWithAHoldAndTheRightAnswer() {
        val gate = ParentalGate(Random(2))
        gate.holdStart(1_000)
        gate.holdEnd(4_100)
        val q = gate.step as ParentalGate.Step.Question
        assertTrue(q.text.contains(" plus "))
        q.answer.toString().forEach { gate.digit(it - '0') }
        assertEquals(ParentalGate.Step.Open, gate.step)
    }

    @Test
    fun aWrongAnswerGoesBackToTheHoldStep() {
        val gate = ParentalGate(Random(3))
        gate.holdStart(0)
        gate.holdEnd(3_000)
        val q = gate.step as ParentalGate.Step.Question
        val wrong = if (q.answer == 99) 98 else q.answer + 1
        wrong.toString().forEach { gate.digit(it - '0') }
        assertEquals(ParentalGate.Step.Hold, gate.step)
    }

    /**
     * Simulates a toddler: 20,000 sessions of 40 random actions each (short and long presses on the hold
     * button, random keypad taps). Toddlers mostly tap, but some presses are long.
     */
    @Test
    fun randomTappingAlmostNeverOpensTheGate() {
        val rnd = Random(42)
        var opened = 0
        val sessions = 20_000
        repeat(sessions) {
            val gate = ParentalGate(Random(rnd.nextLong()))
            var now = 0L
            for (action in 0 until 40) {
                when (rnd.nextInt(3)) {
                    0 -> {
                        val press = if (rnd.nextInt(10) == 0) rnd.nextLong(0, 6_000) else rnd.nextLong(30, 600)
                        gate.holdStart(now)
                        now += press
                        gate.holdEnd(now)
                    }
                    else -> gate.digit(rnd.nextInt(10))
                }
                now += rnd.nextLong(50, 800)
                if (gate.step == ParentalGate.Step.Open) {
                    opened++
                    break
                }
            }
        }
        val rate = opened.toDouble() / sessions
        assertTrue("random tapping opened the gate in ${"%.3f".format(rate * 100)}% of sessions", rate < 0.01)
    }

    @Test
    fun numbersAreWrittenInWords() {
        assertEquals("thirteen", ParentalGate.words(13))
        assertEquals("twenty-two", ParentalGate.words(22))
        assertEquals("forty", ParentalGate.words(40))
    }
}
