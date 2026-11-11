package app.toddlerenglish.parent

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class SessionTimerTest {
    @Test
    fun restsAfterTheLimit() {
        val t = SessionTimer()
        repeat(599) { t.tick(1_000, playing = true) }
        assertFalse(t.isRestTime(10))
        t.tick(1_000, playing = true)
        assertTrue(t.isRestTime(10))
    }

    @Test
    fun parentAreasAndPausesDoNotCount() {
        val t = SessionTimer()
        repeat(1_000) { t.tick(1_000, playing = false) }
        t.tick(3_600_000, playing = true) // app came back from the background: capped
        assertEquals(SessionTimer.MAX_TICK_MS, t.playedMs)
    }

    @Test
    fun offMeansNoLimit() {
        val t = SessionTimer()
        repeat(10_000) { t.tick(1_000, playing = true) }
        assertFalse(t.isRestTime(0))
    }

    @Test
    fun countsWholeMinutesAndResets() {
        val t = SessionTimer()
        var minutes = 0
        repeat(150) { minutes += t.tick(1_000, playing = true) }
        assertEquals(2, minutes)
        t.reset()
        assertFalse(t.isRestTime(1))
    }
}
