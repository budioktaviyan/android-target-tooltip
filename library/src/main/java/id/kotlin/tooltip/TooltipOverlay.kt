package id.kotlin.tooltip

import android.content.Context
import android.support.v7.widget.AppCompatImageView
import android.util.AttributeSet

class TooltipOverlay @JvmOverloads constructor(context: Context,
                                               attrs: AttributeSet? = null,
                                               defStyleAttr: Int = R.style.ToolTipOverlayDefaultStyle,
                                               defStyleResId: Int = R.style.ToolTipLayoutDefaultStyle) : AppCompatImageView(context, attrs, defStyleAttr) {

    var layoutMargins: Int = 0

    init {
        val drawable = TooltipOverlayDrawable(context, defStyleResId)
        setImageDrawable(drawable)

        val typedArray = context.theme.obtainStyledAttributes(defStyleResId, R.styleable.TooltipOverlay)
        layoutMargins = typedArray.getDimensionPixelSize(R.styleable.TooltipOverlay_android_layout_margin, 0)
        typedArray.recycle()
    }
}