package app.toddlerenglish.parent

import kotlin.random.Random

/**
 * The parental gate's rules, kept free of UI so they can be tested with random input.
 *
 * Step 1: press and hold the gate button for [holdMs]. Letting go early starts again.
 * Step 2: type the answer to an addition written in words ("thirteen plus nine"), on a keypad.
 * The answer always has two digits, so a random tapper has a 1 in 100 chance after a full hold.
 * A wrong answer quietly returns to step 1 with a new question.
 */
class ParentalGate(private val random: Random = Random.Default, val holdMs: Long = 3_000) {
    sealed interface Step {
        data object Hold : Step

        data class Question(val text: String, val answer: Int, val entered: String) : Step

        data object Open : Step
    }

    var step: Step = Step.Hold
        private set

    private var holdStartedAt: Long? = null

    fun holdStart(nowMs: Long) {
        if (step == Step.Hold) holdStartedAt = nowMs
    }

    /** Call when the finger lifts or leaves the button. */
    fun holdEnd(nowMs: Long) {
        val started = holdStartedAt ?: return
        holdStartedAt = null
        if (step == Step.Hold && nowMs - started >= holdMs) step = newQuestion()
    }

    /** Call every frame while the button is held, so the question appears as soon as the hold is long enough. */
    fun tick(nowMs: Long) {
        val started = holdStartedAt ?: return
        if (step == Step.Hold && nowMs - started >= holdMs) {
            holdStartedAt = null
            step = newQuestion()
        }
    }

    /** 0..1 progress of the current hold, for the progress ring. */
    fun holdProgress(nowMs: Long): Float {
        val started = holdStartedAt ?: return 0f
        return ((nowMs - started).toFloat() / holdMs).coerceIn(0f, 1f)
    }

    fun digit(d: Int) {
        val q = step as? Step.Question ?: return
        require(d in 0..9)
        val entered = q.entered + d
        step = if (entered.length < q.answer.toString().length) {
            q.copy(entered = entered)
        } else if (entered.toInt() == q.answer) {
            Step.Open
        } else {
            Step.Hold
        }
    }

    fun reset() {
        step = Step.Hold
        holdStartedAt = null
    }

    private fun newQuestion(): Step.Question {
        while (true) {
            val a = random.nextInt(11, 20)
            val b = random.nextInt(3, 10)
            if (a + b in 10..99) return Step.Question("${words(a)} plus ${words(b)}", a + b, "")
        }
    }

    companion object {
        private val SMALL = listOf(
            "zero", "one", "two", "three", "four", "five", "six", "seven", "eight", "nine", "ten",
            "eleven", "twelve", "thirteen", "fourteen", "fifteen", "sixteen", "seventeen", "eighteen", "nineteen",
        )

        fun words(n: Int): String {
            require(n in 0..99)
            if (n < 20) return SMALL[n]
            val tens = listOf("", "", "twenty", "thirty", "forty", "fifty", "sixty", "seventy", "eighty", "ninety")[n / 10]
            return if (n % 10 == 0) tens else "$tens-${SMALL[n % 10]}"
        }
    }
}
