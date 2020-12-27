package code.theducation.appthemehelper.util

import android.content.Context
import android.content.res.ColorStateList
import code.theducation.appthemehelper.ThemeStore
import code.theducation.appthemehelper.util.ATHUtil.isWindowBackgroundDark
import com.afollestad.materialdialogs.internal.ThemeSingleton

/**
 * @author nankai
 */
object MaterialDialogsUtil {
    fun updateMaterialDialogsThemeSingleton(context: Context) {
        val md = ThemeSingleton.get()
        md.titleColor = ThemeStore.textColorPrimary(context)
        md.contentColor = ThemeStore.textColorSecondary(context)
        md.itemColor = md.titleColor
        md.widgetColor = ThemeStore.accentColor(context)
        md.linkColor = ColorStateList.valueOf(md.widgetColor)
        md.positiveColor = ColorStateList.valueOf(md.widgetColor)
        md.neutralColor = ColorStateList.valueOf(md.widgetColor)
        md.negativeColor = ColorStateList.valueOf(md.widgetColor)
        md.darkTheme = isWindowBackgroundDark(context)
    }
}