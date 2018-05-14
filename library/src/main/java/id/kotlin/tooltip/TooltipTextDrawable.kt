package id.kotlin.tooltip

import android.annotation.TargetApi
import android.content.Context
import android.graphics.Canvas
import android.graphics.ColorFilter
import android.graphics.Outline
import android.graphics.Paint
import android.graphics.Path
import android.graphics.PixelFormat
import android.graphics.Point
import android.graphics.Rect
import android.graphics.RectF
import android.graphics.drawable.Drawable
import android.os.Build

class TooltipTextDrawable(context: Context, builder: Tooltip.Builder) : Drawable() {

    private val currentPoint = Point()
    private val outlineRect = Rect()
    private var path: Path? = null
    private var bgPaint: Paint? = null
    private var stPaint: Paint? = null
    private var rectF: RectF? = null
    private var point: Point? = null
    private var gravity: Tooltip.Gravity? = null
    private var ellipseSize = 0f
    private var arrowRatio = 0f
    private var arrowWeight = 0
    private var padding = 0

    init {
        val theme = context.theme.obtainStyledAttributes(null, R.styleable.TooltipLayout, builder.defStyleAttr, builder.defStyleRes)
        ellipseSize = theme.getDimensionPixelSize(R.styleable.TooltipLayout_ttlm_cornerRadius, 4).toFloat()

        val strokeWidth = theme.getDimensionPixelSize(R.styleable.TooltipLayout_ttlm_strokeWeight, 2).toFloat()
        val backgroundColor = theme.getColor(R.styleable.TooltipLayout_ttlm_backgroundColor, 0)
        val strokeColor = theme.getColor(R.styleable.TooltipLayout_ttlm_strokeColor, 0)
        arrowRatio = theme.getFloat(R.styleable.TooltipLayout_ttlm_arrowRatio, 1.4f)
        theme.recycle()

        rectF = RectF()
        path = Path()
        when (backgroundColor) {
            0 -> bgPaint = null
            else -> {
                bgPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                    color = backgroundColor
                    style = Paint.Style.FILL
                }
            }
        }

        when (strokeColor) {
            0 -> stPaint = null
            else -> {
                stPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                    color = strokeColor
                    style = Paint.Style.STROKE
                    setStrokeWidth(strokeWidth)
                }
            }
        }
    }

    override fun draw(canvas: Canvas?) {
        bgPaint?.let { canvas?.drawPath(path, bgPaint) }
        stPaint?.let { canvas?.drawPath(path, stPaint) }
    }

    override fun getAlpha(): Int = bgPaint?.alpha ?: 0
    override fun setAlpha(alpha: Int) {
        bgPaint?.alpha = alpha
        stPaint?.alpha = alpha
    }

    override fun getOpacity(): Int = PixelFormat.TRANSLUCENT
    override fun setColorFilter(colorFilter: ColorFilter?) {}

    override fun onBoundsChange(bounds: Rect?) {
        super.onBoundsChange(bounds)
        calculatePath(bounds)
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    override fun getOutline(outline: Outline?) {
        copyBounds(outlineRect)
        outlineRect.inset(padding, padding)
        outline?.setRoundRect(outlineRect, ellipseSize)

        if (alpha < 255) {
            outline?.alpha = 0f
        }
    }

    fun setAnchor(currentGravity: Tooltip.Gravity, currentPadding: Int, currentPoint: Point?) {
        if (currentGravity != gravity || currentPadding != padding || !equals(point, currentPoint)) {
            gravity = currentGravity
            padding = currentPadding
            arrowWeight = (currentPadding.toFloat() / arrowRatio).toInt()

            point = if (null != currentPoint) {
                Point(currentPoint)
            } else {
                null
            }

            val currentBounds = bounds
            if (!currentBounds.isEmpty) {
                calculatePath(bounds)
                invalidateSelf()
            }
        }
    }

    private fun calculatePath(outBounds: Rect?) {
        outBounds?.let {
            val left = it.left + padding
            val top = it.top + padding
            val right = it.right - padding
            val bottom = it.bottom - padding

            val maxY = bottom - ellipseSize
            val maxX = right - ellipseSize
            val minY = top + ellipseSize
            val minX = left + ellipseSize

            if (null != point && null != gravity) {
                calculatePathWithGravity(it, left, top, right, bottom, maxY, maxX, minY, minX)
            } else {
                rectF?.set(left.toFloat(), top.toFloat(), right.toFloat(), bottom.toFloat())
                path?.addRoundRect(rectF, ellipseSize, ellipseSize, Path.Direction.CW)
            }
        }
    }

    private fun calculatePathWithGravity(outBounds: Rect,
                                         left: Int,
                                         top: Int,
                                         right: Int,
                                         bottom: Int,
                                         maxY: Float,
                                         maxX: Float,
                                         minY: Float,
                                         minX: Float) {
        val drawPoint = isDrawPoint(left, top, right, bottom, maxY, maxX, minY, minX, currentPoint, point, gravity, arrowWeight)
        clampPoint(left, top, right, bottom, currentPoint)
        path?.reset()

        // top/left
        path?.moveTo(left + ellipseSize, top.toFloat())
        if (drawPoint && gravity == Tooltip.Gravity.BOTTOM) {
            path?.lineTo(left + currentPoint.x - arrowWeight.toFloat(), top.toFloat())
            path?.lineTo(left + currentPoint.x.toFloat(), outBounds.top.toFloat())
            path?.lineTo(left + currentPoint.x + arrowWeight.toFloat(), top.toFloat())
        }

        // top/right
        path?.lineTo(right - ellipseSize, top.toFloat())
        path?.quadTo(right.toFloat(), top.toFloat(), right.toFloat(), top + ellipseSize)
        if (drawPoint && gravity == Tooltip.Gravity.LEFT) {
            path?.lineTo(right.toFloat(), top + currentPoint.y - arrowWeight.toFloat())
            path?.lineTo(outBounds.right.toFloat(), top + currentPoint.y.toFloat())
            path?.lineTo(right.toFloat(), top + currentPoint.y + arrowWeight.toFloat())
        }

        // bottom/right
        path?.lineTo(right.toFloat(), bottom - ellipseSize)
        path?.quadTo(right.toFloat(), bottom.toFloat(), right - ellipseSize, bottom.toFloat())
        if (drawPoint && gravity == Tooltip.Gravity.TOP) {
            path?.lineTo(left + currentPoint.x + arrowWeight.toFloat(), bottom.toFloat())
            path?.lineTo(left + currentPoint.x.toFloat(), outBounds.bottom.toFloat())
            path?.lineTo(left + currentPoint.x - arrowWeight.toFloat(), bottom.toFloat())
        }

        // bottom/left
        path?.lineTo(left + ellipseSize, bottom.toFloat())
        path?.quadTo(left.toFloat(), bottom.toFloat(), left.toFloat(), bottom - ellipseSize)
        if (drawPoint && gravity == Tooltip.Gravity.RIGHT) {
            path?.lineTo(left.toFloat(), top + currentPoint.y + arrowWeight.toFloat())
            path?.lineTo(outBounds.left.toFloat(), top + currentPoint.y.toFloat())
            path?.lineTo(left.toFloat(), top + currentPoint.y - arrowWeight.toFloat())
        }

        // top/left
        path?.lineTo(left.toFloat(), top + ellipseSize)
        path?.quadTo(left.toFloat(), top.toFloat(), left + ellipseSize, top.toFloat())
    }

    private fun isDrawPoint(left: Int,
                            top: Int,
                            right: Int,
                            bottom: Int,
                            maxY: Float,
                            maxX: Float,
                            minY: Float,
                            minX: Float,
                            currentPoint: Point,
                            point: Point?,
                            gravity: Tooltip.Gravity?,
                            arrowWeight: Int): Boolean {
        var drawPoint = false
        point?.let { currentPoint.set(it.x, it.y) }

        if (gravity == Tooltip.Gravity.RIGHT || gravity == Tooltip.Gravity.LEFT) {
            if (currentPoint.y in top..bottom) {
                if (top + currentPoint.y + arrowWeight > maxY) {
                    currentPoint.y = (maxY - arrowWeight - top).toInt()
                } else if (top + currentPoint.y - arrowWeight < minY) {
                    currentPoint.y = (minY + arrowWeight - top).toInt()
                }
                drawPoint = true
            }
        } else {
            if (currentPoint.x in left..right) {
                if (currentPoint.x in left..right) {
                    if (left + currentPoint.x + arrowWeight > maxX) {
                        currentPoint.x = (maxX - arrowWeight - left).toInt()
                    } else if (left + currentPoint.x - arrowWeight < minX) {
                        currentPoint.x = (minX + arrowWeight - left).toInt()
                    }
                    drawPoint = true
                }
            }
        }

        return drawPoint
    }

    private fun clampPoint(left: Int, top: Int, right: Int, bottom: Int, point: Point) {
        if (point.y < top) {
            point.y = top
        } else if (point.y > bottom) {
            point.y = bottom
        }

        if (point.x < left) {
            point.x = left
        }

        if (point.x > right) {
            point.x = right
        }
    }
}