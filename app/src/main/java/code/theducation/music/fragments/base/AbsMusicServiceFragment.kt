package code.theducation.music.fragments.base

import android.content.Context
import android.net.Uri
import android.os.Bundle
import android.view.View
import android.webkit.MimeTypeMap
import androidx.annotation.LayoutRes
import androidx.fragment.app.Fragment
import androidx.navigation.navOptions
import code.theducation.music.R
import code.theducation.music.activities.base.AbsMusicServiceActivity
import code.theducation.music.interfaces.IMusicServiceEventListener
import code.theducation.music.model.Song
import code.theducation.music.util.Utils
import org.jaudiotagger.audio.AudioFileIO
import java.io.File
import java.net.URLEncoder
import java.util.*

/**
 * Created by nankai on 18/08/17.
 */

open class AbsMusicServiceFragment(@LayoutRes layout: Int) : Fragment(layout),
    IMusicServiceEventListener {

    val navOptions by lazy {
        navOptions {
            launchSingleTop = false
            anim {
                enter = R.anim.retro_fragment_open_enter
                exit = R.anim.retro_fragment_open_exit
                popEnter = R.anim.retro_fragment_close_enter
                popExit = R.anim.retro_fragment_close_exit
            }
        }
    }

    var serviceActivity: AbsMusicServiceActivity? = null
        private set

    override fun onAttach(context: Context) {
        super.onAttach(context)
        try {
            serviceActivity = context as AbsMusicServiceActivity?
        } catch (e: ClassCastException) {
            throw RuntimeException(context.javaClass.simpleName + " must be an instance of " + AbsMusicServiceActivity::class.java.simpleName)
        }
    }

    override fun onDetach() {
        super.onDetach()
        serviceActivity = null
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        serviceActivity?.addMusicServiceEventListener(this)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        serviceActivity?.removeMusicServiceEventListener(this)
    }

    override fun onPlayingMetaChanged() {
    }

    override fun onServiceConnected() {
    }

    override fun onServiceDisconnected() {
    }

    override fun onQueueChanged() {
    }

    override fun onPlayStateChanged() {
    }

    override fun onRepeatModeChanged() {
    }

    override fun onShuffleModeChanged() {
    }

    override fun onMediaStoreChanged() {
    }

    fun getSongInfo(song: Song): String {
        val file = File(song.data)
        if (file.exists()) {
            return try {
                val audioHeader = AudioFileIO.read(File(song.data)).audioHeader
                val string: StringBuilder = StringBuilder()
                val uriFile = Uri.fromFile(file)
                string.append(getMimeType(uriFile.toString())).append(" • ")
                string.append(audioHeader.bitRate).append(" kb/s").append(" • ")
                string.append(Utils.frequencyCount(audioHeader.sampleRate.toInt()))
                    .append(" kHz")
                string.toString()
            } catch (er: Exception) {
                " - "
            }
        }
        return "-"
    }

    private fun getMimeType(url: String): String? {
        var type: String? = MimeTypeMap.getFileExtensionFromUrl(
            URLEncoder.encode(url, "utf-8")
        ).toUpperCase(Locale.getDefault())
        if (type == null) {
            type = url.substring(url.lastIndexOf(".") + 1)
        }
        return type
    }
}
