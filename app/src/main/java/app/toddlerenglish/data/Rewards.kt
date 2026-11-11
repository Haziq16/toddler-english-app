package app.toddlerenglish.data

import app.toddlerenglish.content.Content
import app.toddlerenglish.content.Sticker

/** What the child has done. Only counts: nothing about the child is stored (no names, no recordings). */
data class Progress(
    val heard: Map<String, Int> = emptyMap(),
    val gameRounds: Map<String, Int> = emptyMap(),
    val songsFinished: Set<String> = emptySet(),
    val stickers: List<String> = emptyList(),
    val minutesPlayed: Int = 0,
) {
    val distinctWordsHeard: Int get() = heard.count { it.value > 0 }
}

/**
 * Sticker rules, pure Kotlin. Stickers are only ever added, never taken away, and there is no "missed" state:
 * every rule counts upward from things the child already did.
 */
object Rewards {
    fun isEarned(sticker: Sticker, progress: Progress, content: Content): Boolean {
        val rule = sticker.rule
        return when (rule.type) {
            "scene_words" -> content.wordsInScene(rule.scene.orEmpty()).count { (progress.heard[it.id] ?: 0) > 0 } >= rule.count
            "game_rounds" -> (progress.gameRounds[rule.game] ?: 0) >= rule.count
            "song" -> rule.song in progress.songsFinished
            else -> false
        }
    }

    /** Stickers earned by [progress] that are not yet in the collection, in catalogue order. */
    fun newlyEarned(progress: Progress, content: Content): List<Sticker> = content.stickers.filter {
        it.id !in progress.stickers &&
            isEarned(it, progress, content)
    }
}
