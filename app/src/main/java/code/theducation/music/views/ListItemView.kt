package code.theducation.music.views

import android.content.Context
import android.util.AttributeSet
import android.view.View
import android.widget.FrameLayout
import code.theducation.music.R
import code.theducation.music.extensions.hide
import code.theducation.music.extensions.show
import kotlinx.android.synthetic.main.list_item_view.view.*

/**
 * Created by nankai on 2019-10-02.
 */
class ListItemView : FrameLayout {


    constructor(context: Context) : super(context) {
        init(context, null)
    }

    constructor(context: Context, attrs: AttributeSet) : super(context, attrs) {
        init(context, attrs)
    }

    constructor(context: Context, attrs: AttributeSet, defStyleAttr: Int) : super(
        context,
        attrs,
        defStyleAttr
    ) {
        init(context, attrs)
    }

    private fun init(context: Context, attrs: AttributeSet?) {
        View.inflate(context, R.layout.list_item_view_no_card, this)

        val typedArray = context.obtainStyledAttributes(attrs, R.styleable.ListItemView)
        if (typedArray.hasValue(R.styleable.ListItemView_listItemIcon)) {
            icon.setImageDrawable(typedArray.getDrawable(R.styleable.ListItemView_listItemIcon))
        } else {

            icon.hide()
        }

        title.text = typedArray.getText(R.styleable.ListItemView_listItemTitle)
        if (typedArray.hasValue(R.styleable.ListItemView_listItemSummary)) {
            summary.text = typedArray.getText(R.styleable.ListItemView_listItemSummary)
        } else {
            summary.hide()
        }
        typedArray.recycle()
    }

    fun setSummary(appVersion: String) {
        summary.show()
        summary.text = appVersion
    }
}