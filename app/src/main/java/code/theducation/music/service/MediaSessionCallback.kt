package code.theducation.music.service

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.support.v4.media.session.MediaSessionCompat
import code.theducation.music.helper.MusicPlayerRemote
import code.theducation.music.helper.MusicPlayerRemote.cycleRepeatMode
import code.theducation.music.model.Song
import code.theducation.music.service.MusicService.*
import code.theducation.music.util.MusicUtil
import java.util.*


/**
 * Created by nankai on 2019-08-01.
 */

class MediaSessionCallback(
    private val context: Context,
    private val musicService: MusicService
) : MediaSessionCompat.Callback() {

    override fun onPlay() {
        super.onPlay()
        musicService.play()
    }

    override fun onPause() {
        super.onPause()
        musicService.pause()
    }

    override fun onSkipToNext() {
        super.onSkipToNext()
        musicService.playNextSong(true)
    }

    override fun onSkipToPrevious() {
        super.onSkipToPrevious()
        musicService.back(true)
    }

    override fun onStop() {
        super.onStop()
        musicService.quit()
    }

    override fun onSeekTo(pos: Long) {
        super.onSeekTo(pos)
        musicService.seek(pos.toInt())
    }

    override fun onMediaButtonEvent(mediaButtonIntent: Intent): Boolean {
        return MediaButtonIntentReceiver.handleIntent(context, mediaButtonIntent)
    }

    override fun onCustomAction(action: String, extras: Bundle?) {
        when (action) {
            CYCLE_REPEAT -> {
                cycleRepeatMode()
                musicService.updateMediaSessionPlaybackState()
            }

            TOGGLE_SHUFFLE -> {
                musicService.toggleShuffle()
                musicService.updateMediaSessionPlaybackState()
            }
            TOGGLE_FAVORITE -> {
                MusicUtil.toggleFavorite(context, MusicPlayerRemote.currentSong)
                musicService.updateMediaSessionPlaybackState()
            }
            else -> {
                println("Unsupported action: $action")
            }
        }
    }

    private fun checkAndStartPlaying(songs: ArrayList<Song>, itemId: Long) {
        var songIndex = MusicUtil.indexOfSongInList(songs, itemId)
        if (songIndex == -1) {
            songIndex = 0
        }
        openQueue(songs, songIndex)
    }

    private fun openQueue(songs: ArrayList<Song>, index: Int, startPlaying: Boolean = true) {
        MusicPlayerRemote.openQueue(songs, index, startPlaying)
    }
}