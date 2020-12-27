package code.theducation.appthemehelper.common.views

import android.content.Context
import android.util.AttributeSet

import androidx.appcompat.widget.AppCompatTextView
import code.theducation.appthemehelper.ThemeStore

class ATEAccentTextView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : AppCompatTextView(context, attrs, defStyleAttr) {

    init {
        setTextColor(ThemeStore.accentColor(context))
    }
}
