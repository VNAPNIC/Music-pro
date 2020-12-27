package code.theducation.music.views

import android.content.Context
import android.util.AttributeSet
import code.theducation.music.R
import com.google.android.material.imageview.ShapeableImageView
import com.google.android.material.shape.CornerFamily
import com.google.android.material.shape.ShapeAppearanceModel


class RetroShapeableImageView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyle: Int = -1
) : ShapeableImageView(context, attrs, defStyle) {


    init {
        val typedArray =
            context.obtainStyledAttributes(attrs, R.styleable.RetroShapeableImageView, defStyle, -1)
        val cornerSize =
            typedArray.getDimension(R.styleable.RetroShapeableImageView_retroCornerSize, 0f)
        updateCornerSize(cornerSize)
        typedArray.recycle()
    }

    private fun updateCornerSize(cornerSize: Float) {
        shapeAppearanceModel = ShapeAppearanceModel.Builder()
            .setAllCorners(CornerFamily.ROUNDED, cornerSize)
            .build()
    }

    //For square
    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        super.onMeasure(widthMeasureSpec, widthMeasureSpec)
    }
}