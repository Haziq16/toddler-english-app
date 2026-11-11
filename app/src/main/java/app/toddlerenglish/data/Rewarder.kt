package app.toddlerenglish.data

import app.toddlerenglish.content.Content
import app.toddlerenglish.content.Sticker
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

/** Records what the child does and hands out stickers. Screens call it and forget; it never blocks the UI. */
class Rewarder(private val content: () -> Content, private val progress: ProgressRepository, private val scope: CoroutineScope) {
    private val mutex = Mutex()
    private val _newStickers = MutableSharedFlow<Sticker>(extraBufferCapacity = 16)
    val newStickers: SharedFlow<Sticker> = _newStickers

    fun wordHeard(wordId: String) = record { progress.wordHeard(wordId) }

    fun gameRound(gameId: String) = record { progress.gameRound(gameId) }

    fun songFinished(songId: String) = record { progress.songFinished(songId) }

    private fun record(update: suspend () -> Progress) {
        scope.launch {
            mutex.withLock {
                val now = update()
                val earned = Rewards.newlyEarned(now, content())
                if (earned.isNotEmpty()) {
                    progress.addStickers(earned.map { it.id })
                    earned.forEach { _newStickers.emit(it) }
                }
            }
        }
    }
}
