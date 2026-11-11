package app.toddlerenglish.audio

import android.content.Context
import android.media.AudioAttributes
import android.media.SoundPool
import java.util.concurrent.ConcurrentHashMap
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

/** What the UI needs from audio. Lets screens be tested with a fake. */
interface WordAudio {
    /** Plays one clip, cutting off whatever was playing. */
    fun play(path: String)

    /** Plays clips one after another. Returns the total time in ms. */
    fun playSequence(paths: List<String>, gapMs: Long = 150): Long

    /** Starts loading clips so the first tap plays instantly. */
    fun preload(paths: Collection<String>)

    fun stop()

    fun durationMs(path: String): Long
}

/**
 * Short clips (words, phrases) through SoundPool. All files are bundled assets: nothing touches the network.
 * Songs use [SongPlayer] (Media3) instead.
 */
class AudioService(private val context: Context, private val durations: Map<String, Long>) : WordAudio {
    @Volatile
    var enabled: Boolean = true

    private val pool = SoundPool.Builder()
        .setMaxStreams(4)
        .setAudioAttributes(
            AudioAttributes.Builder()
                .setUsage(AudioAttributes.USAGE_GAME)
                .setContentType(AudioAttributes.CONTENT_TYPE_SPEECH)
                .build(),
        )
        .build()
    private val soundIds = ConcurrentHashMap<String, Int>()
    private val loaded = ConcurrentHashMap.newKeySet<Int>()
    private val playWhenLoaded = ConcurrentHashMap.newKeySet<Int>()
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Main.immediate)
    private var sequence: Job? = null

    @Volatile
    private var currentStream = 0

    init {
        pool.setOnLoadCompleteListener { _, soundId, status ->
            if (status == 0) {
                loaded += soundId
                if (playWhenLoaded.remove(soundId)) startStream(soundId)
            }
        }
    }

    override fun preload(paths: Collection<String>) {
        paths.forEach { soundIdFor(it) }
    }

    override fun play(path: String) {
        sequence?.cancel()
        playNow(path)
    }

    override fun playSequence(paths: List<String>, gapMs: Long): Long {
        sequence?.cancel()
        val total = paths.sumOf { durationMs(it) + gapMs }
        sequence = scope.launch {
            paths.forEach { path ->
                playNow(path)
                delay(durationMs(path) + gapMs)
            }
        }
        return total
    }

    override fun stop() {
        sequence?.cancel()
        if (currentStream != 0) pool.stop(currentStream)
        playWhenLoaded.clear()
    }

    override fun durationMs(path: String): Long = durations[path] ?: DEFAULT_DURATION_MS

    fun release() {
        stop()
        pool.release()
    }

    private fun playNow(path: String) {
        if (!enabled) return
        if (currentStream != 0) pool.stop(currentStream)
        playWhenLoaded.clear()
        val id = soundIdFor(path) ?: return
        if (id in loaded) startStream(id) else playWhenLoaded += id
    }

    private fun startStream(soundId: Int) {
        if (!enabled) return
        currentStream = pool.play(soundId, 1f, 1f, 1, 0, 1f)
    }

    private fun soundIdFor(path: String): Int? = soundIds[path] ?: runCatching {
        context.assets.openFd(path).use { fd -> pool.load(fd, 1) }
    }.getOrNull()?.also { soundIds[path] = it }

    companion object {
        const val DEFAULT_DURATION_MS = 900L
    }
}
