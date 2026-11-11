package app.toddlerenglish.data

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.MutablePreferences
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.serialization.builtins.ListSerializer
import kotlinx.serialization.builtins.MapSerializer
import kotlinx.serialization.builtins.serializer
import kotlinx.serialization.json.Json

/** Local-only progress (D-003). Each update returns the new [Progress] so callers can award stickers. */
class ProgressRepository(private val store: DataStore<Preferences>) {
    val progress: Flow<Progress> = store.data.map(::read)

    suspend fun wordHeard(wordId: String): Progress = update { p ->
        val heard = decodeMap(p[HEARD]).toMutableMap()
        heard[wordId] = (heard[wordId] ?: 0) + 1
        p[HEARD] = encodeMap(heard)
    }

    suspend fun gameRound(gameId: String): Progress = update { p ->
        val rounds = decodeMap(p[ROUNDS]).toMutableMap()
        rounds[gameId] = (rounds[gameId] ?: 0) + 1
        p[ROUNDS] = encodeMap(rounds)
    }

    suspend fun songFinished(songId: String): Progress = update { p ->
        p[SONGS] = encodeList((decodeList(p[SONGS]) + songId).distinct())
    }

    suspend fun addStickers(ids: List<String>): Progress = update { p ->
        p[STICKERS] = encodeList((decodeList(p[STICKERS]) + ids).distinct())
    }

    suspend fun addMinute(): Progress = update { p -> p[MINUTES] = (p[MINUTES] ?: 0) + 1 }

    suspend fun reset() {
        store.edit { p ->
            listOf(HEARD, ROUNDS, SONGS, STICKERS).forEach { p.remove(it) }
            p.remove(MINUTES)
        }
    }

    private suspend fun update(block: (MutablePreferences) -> Unit): Progress = read(store.edit(block))

    private companion object {
        val HEARD = stringPreferencesKey("heard")
        val ROUNDS = stringPreferencesKey("game_rounds")
        val SONGS = stringPreferencesKey("songs_finished")
        val STICKERS = stringPreferencesKey("stickers")
        val MINUTES = intPreferencesKey("minutes_played")

        val json = Json { ignoreUnknownKeys = true }
        val mapSer = MapSerializer(String.serializer(), Int.serializer())
        val listSer = ListSerializer(String.serializer())

        fun decodeMap(s: String?): Map<String, Int> = s?.let { runCatching { json.decodeFromString(mapSer, it) }.getOrNull() }.orEmpty()

        fun encodeMap(m: Map<String, Int>) = json.encodeToString(mapSer, m)

        fun decodeList(s: String?): List<String> = s?.let { runCatching { json.decodeFromString(listSer, it) }.getOrNull() }.orEmpty()

        fun encodeList(l: List<String>) = json.encodeToString(listSer, l)

        fun read(p: Preferences) = Progress(
            heard = decodeMap(p[HEARD]),
            gameRounds = decodeMap(p[ROUNDS]),
            songsFinished = decodeList(p[SONGS]).toSet(),
            stickers = decodeList(p[STICKERS]),
            minutesPlayed = p[MINUTES] ?: 0,
        )
    }
}
