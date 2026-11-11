package app.toddlerenglish.data

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

/** Parent-controlled settings. Stored only on the device. */
data class Settings(
    val soundOn: Boolean = true,
    /** Highest word tier games use (1..3). */
    val tier: Int = 1,
    /** Play-time limit in minutes before the calm rest screen; 0 = off. */
    val sessionLimitMinutes: Int = 10,
) {
    companion object {
        val TIERS = 1..3
        val SESSION_LIMITS = listOf(5, 10, 15, 20, 0)
    }
}

class SettingsRepository(private val store: DataStore<Preferences>) {
    val settings: Flow<Settings> = store.data.map { p ->
        Settings(
            soundOn = p[SOUND_ON] ?: true,
            tier = (p[TIER] ?: 1).coerceIn(Settings.TIERS),
            sessionLimitMinutes = p[SESSION_LIMIT] ?: 10,
        )
    }

    suspend fun setSoundOn(on: Boolean) {
        store.edit { it[SOUND_ON] = on }
    }

    suspend fun setTier(tier: Int) {
        store.edit { it[TIER] = tier.coerceIn(Settings.TIERS) }
    }

    suspend fun setSessionLimit(minutes: Int) {
        store.edit { it[SESSION_LIMIT] = minutes.coerceIn(0, 60) }
    }

    private companion object {
        val SOUND_ON = booleanPreferencesKey("sound_on")
        val TIER = intPreferencesKey("tier")
        val SESSION_LIMIT = intPreferencesKey("session_limit_minutes")
    }
}
