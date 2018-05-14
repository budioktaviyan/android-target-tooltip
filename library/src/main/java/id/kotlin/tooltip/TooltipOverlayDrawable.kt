package id.kotlin.tooltip

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.animation.AnimatorSet
import android.animation.ObjectAnimator
import android.animation.ValueAnimator
import android.content.Context
import android.graphics.Canvas
import android.graphics.ColorFilter
import android.graphics.Paint
import android.graphics.PixelFormat
import android.graphics.Rect
import android.graphics.drawable.Drawable
import android.view.animation.AccelerateDecelerateInterpolator

class TooltipOverlayDrawable(context: Context, defStyleResId: Int) : Drawable() {

    private val outerPaint = Paint(Paint.ANTI_ALIAS_FLAG)
    private val innerPaint = Paint(Paint.ANTI_ALIAS_FLAG)
    private var repeatIndex = 0
    private var repeatCount = 1
    private var started = false
    private var currentDuration = 400L
    private var firstAnimatorSet: AnimatorSet? = null
    private var secondAnimatorSet: AnimatorSet? = null
    private var firstAnimator: ValueAnimator? = null
    private var secondAnimator: ValueAnimator? = null

    private var outerAlpha: Int
        get() = outerPaint.alpha
        set(value) {
            outerPaint.alpha = value
            invalidateSelf()
        }
    private var innerAlpha: Int
        get() = innerPaint.alpha
        set(value) {
            innerPaint.alpha = value
            invalidateSelf()
        }
    private var outerRadius: Float = 0f
        set(value) {
            field = value
            invalidateSelf()
        }
    private var innerRadius: Float = 0f
        set(value) {
            field = value
            invalidateSelf()
        }

    init {
        outerPaint.style = Paint.Style.FILL
        innerPaint.style = Paint.Style.FILL

        val theme = context.theme.obtainStyledAttributes(defStyleResId, R.styleable.TooltipOverlay)
        for (i in 0 until theme.indexCount) {
            val index = theme.getIndex(i)
            when (index) {
                R.styleable.TooltipOverlay_android_color -> {
                    val color = theme.getColor(index, 0)
                    outerPaint.color = color
                    innerPaint.color = color
                }
                R.styleable.TooltipOverlay_ttlm_repeatCount -> {
                    repeatCount = theme.getInt(index, 1)
                }
                R.styleable.TooltipOverlay_android_alpha -> {
                    val alpha = (theme.getFloat(index, innerPaint.alpha / 255f) * 255).toInt()
                    innerPaint.alpha = alpha
                    outerPaint.alpha = alpha
                }
                R.styleable.TooltipOverlay_ttlm_duration -> {
                    currentDuration = theme.getInt(index, 400).toLong()
                }
            }
        }
        theme.recycle()

        // first
        var fadeIn = ObjectAnimator.ofInt(this, "outerAlpha", 0, outerAlpha).apply {
            duration = (currentDuration * 0.3).toLong()
        }
        var fadeOut = ObjectAnimator.ofInt(this, "outerAlpha", outerAlpha, 0, 0).apply {
            startDelay = (currentDuration * 0.55).toLong()
            duration = (currentDuration * (1.0 - 0.55)).toLong()
        }
        firstAnimator = ObjectAnimator.ofFloat(this, "outerRadius", 0f, 1f).apply {
            duration = currentDuration
        }
        firstAnimatorSet = AnimatorSet().apply {
            playTogether(fadeIn, firstAnimator, fadeOut)
            interpolator = AccelerateDecelerateInterpolator()
            duration = currentDuration
            addListener(object : AnimatorListenerAdapter() {
                var cancelled = false

                override fun onAnimationCancel(animation: Animator?) {
                    super.onAnimationCancel(animation)
                    cancelled = true
                }

                override fun onAnimationEnd(animation: Animator?) {
                    if (!cancelled && isVisible && ++repeatIndex < repeatCount) {
                        start()
                    }
                }
            })
        }

        // second
        fadeIn = ObjectAnimator.ofInt(this, "innerAlpha", 0, innerAlpha).apply {
            duration = (currentDuration * 0.3).toLong()
        }
        fadeOut = ObjectAnimator.ofInt(this, "innerAlpha", innerAlpha, 0, 0).apply {
            startDelay = (currentDuration * 0.55).toLong()
            duration = (currentDuration * (1.0 - 0.55)).toLong()
        }
        secondAnimator = ObjectAnimator.ofFloat(this, "innerRadius", 0f, 1f).apply {
            duration = currentDuration
        }
        secondAnimatorSet = AnimatorSet().apply {
            playTogether(fadeIn, secondAnimator, fadeOut)
            interpolator = AccelerateDecelerateInterpolator()
            startDelay = (currentDuration * 0.25).toLong()
            duration = currentDuration
            addListener(object : AnimatorListenerAdapter() {
                var cancelled = false

                override fun onAnimationCancel(animation: Animator?) {
                    super.onAnimationCancel(animation)
                    cancelled = true
                }

                override fun onAnimationEnd(animation: Animator?) {
                    if (!cancelled && isVisible && repeatIndex < repeatCount) {
                        startDelay = 0
                        start()
                    }
                }
            })
        }
    }

    override fun draw(canvas: Canvas?) {
        val currentBounds = bounds
        val centerX = (currentBounds.width() / 2).toFloat()
        val centerY = (currentBounds.height() / 2).toFloat()
        canvas?.drawCircle(centerX, centerY, outerRadius, outerPaint)
        canvas?.drawCircle(centerX, centerY, innerRadius, innerPaint)
    }

    override fun setAlpha(alpha: Int) {}
    override fun getOpacity(): Int = PixelFormat.TRANSLUCENT
    override fun setColorFilter(colorFilter: ColorFilter?) {}
    override fun setVisible(visible: Boolean, restart: Boolean): Boolean {
        val changed = isVisible != visible
        when (visible) {
            true -> if (restart || !started) {
                replay()
            }
            false -> stop()
        }

        return changed
    }

    override fun onBoundsChange(bounds: Rect?) {
        super.onBoundsChange(bounds)
        bounds?.let {
            outerRadius = (Math.min(it.width(), it.height()) / 2).toFloat()
            firstAnimator?.setFloatValues(0f, outerRadius)
            secondAnimator?.setFloatValues(0f, outerRadius)
        }
    }

    override fun getIntrinsicWidth(): Int = 96
    override fun getIntrinsicHeight(): Int = 96

    private fun play() {
        repeatIndex = 0
        started = true
        firstAnimatorSet?.start()
        secondAnimatorSet?.apply {
            startDelay = (currentDuration * 0.25).toLong()
            start()
        }
    }

    private fun stop() {
        firstAnimatorSet?.cancel()
        secondAnimatorSet?.cancel()

        repeatIndex = 0
        started = false
        innerRadius = 0f
        outerRadius = 0f
    }

    private fun replay() {
        stop()
        play()
    }
}