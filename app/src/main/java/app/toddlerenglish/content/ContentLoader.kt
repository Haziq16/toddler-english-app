package app.toddlerenglish.content

import kotlinx.serialization.json.Json

/**
 * Parses content JSON. [read] returns the text of a file under `assets/content/`, or null if it does not exist.
 * Pure Kotlin so unit tests can run it against the real files.
 */
class ContentLoader(private val read: (String) -> String?) {
    private val json = Json {
        ignoreUnknownKeys = true
        isLenient = false
    }

    fun load(): Content {
        val words = json.decodeFromString<WordsFile>(require("words.json")).words
        val phrases = json.decodeFromString<PhrasesFile>(require("phrases.json")).phrases
        val scenes = json.decodeFromString<ScenesFile>(require("scenes.json")).scenes
        val layouts = scenes.associate { scene ->
            val layout = read(scene.layout)?.let { json.decodeFromString<SceneLayout>(it) } ?: SceneLayout(scene.id)
            scene.id to layout
        }
        val games = read("games.json")?.let { json.decodeFromString<GamesFile>(it).games }.orEmpty()
        val songs = read("songs.json")?.let { json.decodeFromString<SongsFile>(it).songs }.orEmpty()
        return Content(words, phrases, scenes, layouts, games, songs)
    }

    private fun require(name: String): String = read(name) ?: error("Missing content file $name")
}
