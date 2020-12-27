package code.theducation.music.views

import android.content.Context
import android.content.res.ColorStateList
import android.graphics.Color
import android.util.AttributeSet
import androidx.appcompat.widget.AppCompatImageView
import androidx.core.content.ContextCompat
import code.theducation.appthemehelper.util.ATHUtil
import code.theducation.appthemehelper.util.ColorUtil
import code.theducation.music.R
import code.theducation.music.util.PreferenceUtil
import code.theducation.music.util.RetroColorUtil


class ColorIconsImageView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = -1
) : AppCompatImageView(context, attrs, defStyleAttr) {


    init {
        // Load the styled attributes and set their properties
        val attributes =
            context.obtainStyledAttributes(attrs, R.styleable.ColorIconsImageView, 0, 0)
        val color =
            attributes.getColor(R.styleable.ColorIconsImageView_iconBackgroundColor, Color.RED)
        setIconBackgroundColor(color)
        attributes.recycle()
    }

    fun setIconBackgroundColor(color: Int) {
        background = ContextCompat.getDrawable(context, R.drawable.color_circle_gradient)
        if (ATHUtil.isWindowBackgroundDark(context) && PreferenceUtil.isDesaturatedColor) {
            val desaturatedColor = RetroColorUtil.desaturateColor(color, 0.4f)
            backgroundTintList = ColorStateList.valueOf(desaturatedColor)
            imageTintList =
                ColorStateList.valueOf(ATHUtil.resolveColor(context, R.attr.colorSurface))
        } else {
            backgroundTintList = ColorStateList.valueOf(ColorUtil.adjustAlpha(color, 0.22f))
            imageTintList = ColorStateList.valueOf(ColorUtil.withAlpha(color, 0.75f))
        }
        requestLayout()
        invalidate()
    }
}
