package code.theducation.music.views

import android.content.Context
import android.graphics.Color
import android.util.AttributeSet
import android.view.View
import android.widget.FrameLayout
import code.theducation.music.R
import kotlinx.android.synthetic.main.list_setting_item_view.view.*

/**
 * Created by nankai on 2019-12-10.
 */
class SettingListItemView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = -1,
    defStyleRes: Int = -1
) : FrameLayout(context, attrs, defStyleAttr, defStyleRes) {
    init {
        View.inflate(context, R.layout.list_setting_item_view, this)
        val typedArray =
            context.obtainStyledAttributes(attrs, R.styleable.SettingListItemView)
        icon as ColorIconsImageView
        if (typedArray.hasValue(R.styleable.SettingListItemView_settingListItemIcon)) {
            icon.setImageDrawable(typedArray.getDrawable(R.styleable.SettingListItemView_settingListItemIcon))
        }
        icon.setIconBackgroundColor(
            typedArray.getColor(
                R.styleable.SettingListItemView_settingListItemIconColor,
                Color.WHITE
            )
        )
        title.text = typedArray.getText(R.styleable.SettingListItemView_settingListItemTitle)
        text.text = typedArray.getText(R.styleable.SettingListItemView_settingListItemText)
        typedArray.recycle()
    }
}