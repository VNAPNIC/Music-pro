package code.theducation.music.util

import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.core.app.ActivityCompat

/**
 * Created by nankai on 2020-02-02.
 */

object Share {
    fun shareStoryToSocial(context: Context, uri: Uri) {
        val feedIntent = Intent(Intent.ACTION_SEND)
        feedIntent.type = "image/*"
        feedIntent.putExtra(Intent.EXTRA_STREAM, uri)
        ActivityCompat.startActivity(context, feedIntent, null)
    }
}