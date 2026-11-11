package app.toddlerenglish.parent

/**
 * Counts play time for the parent's limit. The child never sees a countdown (D-005): when the limit is reached
 * the app shows a calm rest screen, and only a grown-up (through the gate) can continue.
 * Time in the gate, the parent corner and the rest screen does not count.
 */
class SessionTimer {
    var playedMs: Long = 0
        private set
    private var sinceMinuteMs: Long = 0

    /**
     * Adds [deltaMs] of play. Returns how many whole minutes were completed by this tick (for the parent's
     * minutes-played total).
     */
    fun tick(deltaMs: Long, playing: Boolean): Int {
        if (!playing || deltaMs <= 0) return 0
        val d = deltaMs.coerceAtMost(MAX_TICK_MS) // a long pause (app in background) must not count
        playedMs += d
        sinceMinuteMs += d
        val minutes = (sinceMinuteMs / 60_000).toInt()
        sinceMinuteMs %= 60_000
        return minutes
    }

    fun isRestTime(limitMinutes: Int): Boolean = limitMinutes > 0 && playedMs >= limitMinutes * 60_000L

    /** A grown-up opened the gate: start a fresh session. */
    fun reset() {
        playedMs = 0
    }

    companion object {
        const val MAX_TICK_MS = 5_000L
    }
}
