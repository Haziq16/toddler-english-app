package app.toddlerenglish.content

import kotlinx.serialization.Serializable

/** One word a child can hear. Mirrors `assets/content/words.json` (schema in docs/CONTENT.md). */
@Serializable
data class Word(
    val id: String,
    val text: String,
    val scene: String,
    val tier: Int,
    val audio: String,
    val image: String,
    val tags: List<String> = emptyList(),
    val translations: Map<String, String> = emptyMap(),
)

@Serializable
data class WordsFile(val schemaVersion: Int, val words: List<Word>)

/** A spoken line used by the mascot and the UI. Every instruction is spoken. */
@Serializable
data class Phrase(val id: String, val text: String, val audio: String, val translations: Map<String, String> = emptyMap())

@Serializable
data class PhrasesFile(val schemaVersion: Int, val phrases: List<Phrase>)

@Serializable
data class SceneInfo(
    val id: String,
    val title: String,
    val tint: String,
    val icon: String,
    val background: String,
    val phrase: String,
    val layout: String,
)

@Serializable
data class ScenesFile(val schemaVersion: Int, val scenes: List<SceneInfo>)

/** Where one word sits in a scene. x and y are the centre, 0..1 of the scene; size is a fraction of the shorter side. */
@Serializable
data class Placement(val x: Float, val y: Float, val size: Float = 0.16f)

@Serializable
data class SceneLayout(val scene: String, val placements: Map<String, Placement> = emptyMap())

@Serializable
data class GameDeck(val words: List<String>)

@Serializable
data class Game(
    val id: String,
    val type: String,
    val title: String,
    val phrase: String,
    val icon: String,
    val minWordsPerTier: Int = 2,
    val decks: Map<String, GameDeck>,
)

@Serializable
data class GamesFile(val schemaVersion: Int, val games: List<Game>)

@Serializable
data class SongToken(val text: String, val word: String? = null)

@Serializable
data class SongLine(val tokens: List<SongToken>, val restBeats: Int = 1)

@Serializable
data class SongTiming(val line: Int, val token: Int, val startMs: Int, val endMs: Int)

@Serializable
data class Song(
    val id: String,
    val title: String,
    val icon: String,
    val audio: String,
    val bpm: Int = 92,
    val melody: List<String> = emptyList(),
    val lines: List<SongLine>,
    val timings: List<SongTiming> = emptyList(),
)

@Serializable
data class SongsFile(val schemaVersion: Int, val songs: List<Song>)

/** When a sticker is earned. `type` is scene_words, game_rounds or song. */
@Serializable
data class StickerRule(val type: String, val count: Int = 1, val scene: String? = null, val game: String? = null, val song: String? = null)

/** A collectable sticker. Its picture is the art of [word]. */
@Serializable
data class Sticker(val id: String, val word: String, val rule: StickerRule)

@Serializable
data class StickersFile(val schemaVersion: Int, val stickers: List<Sticker>)

/** Everything the app shows, loaded once at start. */
data class Content(
    val words: List<Word>,
    val phrases: List<Phrase>,
    val scenes: List<SceneInfo>,
    val layouts: Map<String, SceneLayout>,
    val games: List<Game>,
    val songs: List<Song>,
    val stickers: List<Sticker> = emptyList(),
) {
    private val wordIndex = words.associateBy { it.id }
    private val phraseIndex = phrases.associateBy { it.id }

    fun word(id: String): Word? = wordIndex[id]

    fun phrase(id: String): Phrase? = phraseIndex[id]

    fun wordsInScene(sceneId: String): List<Word> = words.filter { it.scene == sceneId }

    /** Words a child at [tier] sees: that tier and every easier one. */
    fun wordsUpToTier(ids: List<String>, tier: Int): List<Word> = ids.mapNotNull { wordIndex[it] }.filter { it.tier <= tier }

    companion object {
        val Empty = Content(emptyList(), emptyList(), emptyList(), emptyMap(), emptyList(), emptyList())
    }
}
