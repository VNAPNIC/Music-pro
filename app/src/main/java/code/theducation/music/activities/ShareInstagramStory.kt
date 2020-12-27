package code.theducation.music.activities

import android.content.res.ColorStateList
import android.graphics.Bitmap
import android.graphics.Color
import android.graphics.drawable.GradientDrawable
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore.Images.Media
import android.view.MenuItem
import androidx.core.view.drawToBitmap
import code.theducation.appthemehelper.ThemeStore
import code.theducation.appthemehelper.util.ColorUtil
import code.theducation.appthemehelper.util.MaterialValueHelper
import code.theducation.music.R
import code.theducation.music.activities.base.AbsBaseActivity
import code.theducation.music.glide.RetroMusicColoredTarget
import code.theducation.music.glide.SongGlideRequest
import code.theducation.music.model.Song
import code.theducation.music.util.Share
import code.theducation.music.util.color.MediaNotificationProcessor
import com.bumptech.glide.Glide
import kotlinx.android.synthetic.main.activity_share_instagram.*

/**
 * Created by nankai on 2020-02-02.
 */

class ShareInstagramStory : AbsBaseActivity() {

    companion object {
        const val EXTRA_SONG = "extra_song"
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == android.R.id.home) {
            onBackPressed()
            return true
        }
        return super.onOptionsItemSelected(item)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        setDrawUnderStatusBar()
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_share_instagram)
        setStatusbarColor(Color.TRANSPARENT)
        setNavigationbarColor(Color.BLACK)

        toolbar.setBackgroundColor(Color.TRANSPARENT)
        setSupportActionBar(toolbar)

        val song = intent.extras?.getParcelable<Song>(EXTRA_SONG)
        song?.let { songFinal ->
            SongGlideRequest.Builder.from(Glide.with(this), songFinal)
                .checkIgnoreMediaStore(this@ShareInstagramStory)
                .generatePalette(this@ShareInstagramStory)
                .build()
                .into(object : RetroMusicColoredTarget(image) {
                    override fun onColorReady(colors: MediaNotificationProcessor) {
                        val isColorLight = ColorUtil.isColorLight(colors.backgroundColor)
                        setColors(isColorLight, colors.backgroundColor)
                    }
                })

            shareTitle.text = songFinal.title
            shareText.text = songFinal.artistName
            shareButton.setOnClickListener {
                val path: String = Media.insertImage(
                    contentResolver,
                    mainContent.drawToBitmap(Bitmap.Config.ARGB_8888),
                    "Design", null
                )
                val uri = Uri.parse(path)
                Share.shareStoryToSocial(
                    this@ShareInstagramStory,
                    uri
                )
            }
        }
        shareButton.setTextColor(
            MaterialValueHelper.getPrimaryTextColor(
                this,
                ColorUtil.isColorLight(ThemeStore.accentColor(this))
            )
        )
        shareButton.backgroundTintList = ColorStateList.valueOf(ThemeStore.accentColor(this))
    }

    private fun setColors(colorLight: Boolean, color: Int) {
        setLightStatusbar(colorLight)
        toolbar.setTitleTextColor(
            MaterialValueHelper.getPrimaryTextColor(
                this@ShareInstagramStory,
                colorLight
            )
        )
        toolbar.navigationIcon?.setTintList(
            ColorStateList.valueOf(
                MaterialValueHelper.getPrimaryTextColor(
                    this@ShareInstagramStory,
                    colorLight
                )
            )
        )
        mainContent.background =
            GradientDrawable(
                GradientDrawable.Orientation.TOP_BOTTOM,
                intArrayOf(color, Color.BLACK)
            )
    }
}
