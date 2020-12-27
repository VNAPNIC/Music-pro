package code.theducation.music.util

import android.app.Activity
import android.content.Context
import android.util.DisplayMetrics
import android.util.TypedValue

/**
 * Created by nankai on 16/7/30.
 */
object DensityUtil {
    fun getScreenHeight(context: Context): Int {
        val displayMetrics = DisplayMetrics()
        (context as Activity).windowManager.defaultDisplay.getMetrics(displayMetrics)
        return displayMetrics.heightPixels
    }

    fun getScreenWidth(context: Context): Int {
        val displayMetrics = DisplayMetrics()
        (context as Activity).windowManager.defaultDisplay.getMetrics(displayMetrics)
        return displayMetrics.widthPixels
    }

    private fun toDP(context: Context, value: Int): Int {
        return TypedValue.applyDimension(
            TypedValue.COMPLEX_UNIT_DIP,
            value.toFloat(), context.resources.displayMetrics
        ).toInt()
    }

    @JvmStatic
    fun dip2px(context: Context, dpVale: Float): Int {
        val scale = context.resources.displayMetrics.density
        return (dpVale * scale + 0.5f).toInt()
    }
}