package app.toddlerenglish

import android.app.Application
import android.content.Context
import androidx.datastore.preferences.preferencesDataStore
import app.toddlerenglish.audio.AudioIndex
import app.toddlerenglish.audio.AudioService
import app.toddlerenglish.content.Content
import app.toddlerenglish.content.ContentLoader
import app.toddlerenglish.data.ProgressRepository
import app.toddlerenglish.data.Rewarder
import app.toddlerenglish.data.SettingsRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob

private val Context.settingsStore by preferencesDataStore(name = "settings")
private val Context.progressStore by preferencesDataStore(name = "progress")

/** Hand-made dependency container. One per process. */
class AppContainer(private val context: Context) {
    val content: Content by lazy {
        ContentLoader { name -> readAsset("content/$name") }.load()
    }

    val audio: AudioService by lazy {
        AudioService(context, AudioIndex.parse(readAsset("audio/index.json")))
    }

    val settings: SettingsRepository by lazy { SettingsRepository(context.settingsStore) }

    val progress: ProgressRepository by lazy { ProgressRepository(context.progressStore) }

    /** Background work that must outlive a screen (saving progress). */
    val appScope = CoroutineScope(SupervisorJob() + Dispatchers.Default)

    val rewarder: Rewarder by lazy { Rewarder({ content }, progress, appScope) }

    /** Pebble says hello once per app start, not every time the child returns home. */
    var greeted: Boolean = false

    private fun readAsset(path: String): String? = runCatching {
        context.assets.open(path).bufferedReader().use { it.readText() }
    }.getOrNull()
}

class ToddlerApplication : Application() {
    lateinit var container: AppContainer
        private set

    override fun onCreate() {
        super.onCreate()
        container = AppContainer(this)
    }
}
