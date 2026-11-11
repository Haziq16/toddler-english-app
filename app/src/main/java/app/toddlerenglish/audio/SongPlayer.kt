package app.toddlerenglish.audio

import android.content.Context
import androidx.media3.common.MediaItem
import androidx.media3.common.Player
import androidx.media3.exoplayer.ExoPlayer

/**
 * Plays one bundled song through Media3 ExoPlayer from `asset:///`. Nothing is streamed.
 * The UI polls [positionMs] to highlight the word being sung.
 */
class SongPlayer(private val context: Context) {
    private var player: ExoPlayer? = null

    val isPlaying: Boolean get() = player?.isPlaying == true

    fun play(assetPath: String, onEnded: () -> Unit) {
        release()
        player = ExoPlayer.Builder(context).build().apply {
            addListener(object : Player.Listener {
                override fun onPlaybackStateChanged(state: Int) {
                    if (state == Player.STATE_ENDED) onEnded()
                }
            })
            setMediaItem(MediaItem.fromUri("asset:///$assetPath"))
            prepare()
            play()
        }
    }

    fun positionMs(): Long = player?.currentPosition ?: 0L

    fun pause() {
        player?.pause()
    }

    fun resume() {
        player?.play()
    }

    fun release() {
        player?.release()
        player = null
    }
}
