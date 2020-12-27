package code.theducation.music.glide

import android.graphics.drawable.Drawable
import android.widget.ImageView
import code.theducation.appthemehelper.util.ATHUtil
import code.theducation.music.App
import code.theducation.music.R
import code.theducation.music.glide.palette.BitmapPaletteTarget
import code.theducation.music.glide.palette.BitmapPaletteWrapper
import code.theducation.music.util.color.MediaNotificationProcessor
import com.bumptech.glide.request.animation.GlideAnimation

abstract class RetroMusicColoredTarget(view: ImageView) : BitmapPaletteTarget(view) {

    protected val defaultFooterColor: Int
        get() = ATHUtil.resolveColor(getView().context, R.attr.colorControlNormal)

    abstract fun onColorReady(colors: MediaNotificationProcessor)

    override fun onLoadFailed(e: Exception?, errorDrawable: Drawable?) {
        super.onLoadFailed(e, errorDrawable)
        val colors = MediaNotificationProcessor(App.getContext(), errorDrawable)
        onColorReady(colors)
    }

    override fun onResourceReady(
        resource: BitmapPaletteWrapper?,
        glideAnimation: GlideAnimation<in BitmapPaletteWrapper>?
    ) {
        super.onResourceReady(resource, glideAnimation)
        resource?.let { bitmapWrap ->
            MediaNotificationProcessor(App.getContext()).getPaletteAsync({
                onColorReady(it)
            }, bitmapWrap.bitmap)
        }
    }
}
