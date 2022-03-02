package com.resurrection.qrcodescanner.base.widget

import android.content.Context
import android.graphics.*
import android.util.AttributeSet
import android.util.TypedValue
import android.widget.FrameLayout
import androidx.core.content.ContextCompat
import androidx.core.graphics.ColorUtils
import kotlin.math.min
import kotlin.math.roundToInt


@Suppress("TooManyFunctions")
internal class QRFrameView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : FrameLayout(context, attrs, defStyleAttr) {

    companion object {
        private const val BACKGROUND_ALPHA = 0.77 * 255
        private const val STROKE_WIDTH = 4f
        private const val OUT_RADIUS = 16f
        private const val FRAME_MARGIN_RATIO = 1f / 4
    }

    private val grayColor = ContextCompat.getColor(context, com.resurrection.base.R.color.gray)
    private val backgroundColor =
        ColorUtils.setAlphaComponent(Color.BLACK, BACKGROUND_ALPHA.roundToInt())
    private val alphaPaint = Paint().apply { alpha = BACKGROUND_ALPHA.roundToInt() }
    private val strokePaint = Paint(Paint.ANTI_ALIAS_FLAG)
    private val transparentPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.TRANSPARENT
        xfermode = PorterDuffXfermode(PorterDuff.Mode.CLEAR)
    }
    private val outerRadius = OUT_RADIUS.toPx()
    private val innerRadius = (OUT_RADIUS - STROKE_WIDTH).toPx()
    private val outerFrame = RectF()
    private val innerFrame = RectF()
    private var maskBitmap: Bitmap? = null
    private var maskCanvas: Canvas? = null
    private var horizontalFrameRatio = 1f

    init {
        setWillNotDraw(false)
    }

    override fun onLayout(changed: Boolean, left: Int, top: Int, right: Int, bottom: Int) {
        super.onLayout(changed, left, top, right, bottom)

        if (maskBitmap == null && width > 0 && height > 0) {
            maskBitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
                .apply { maskCanvas = Canvas(this) }
            calculateFrameAndTitlePos()
        }
    }

    @Suppress("UnsafeCallOnNullableType")
    override fun onDraw(canvas: Canvas) {
        strokePaint.color = grayColor
        maskCanvas!!.drawColor(backgroundColor)
        maskCanvas!!.drawRoundRect(outerFrame, outerRadius, outerRadius, strokePaint)
        maskCanvas!!.drawRoundRect(innerFrame, innerRadius, innerRadius, transparentPaint)
        canvas.drawBitmap(maskBitmap!!, 0f, 0f, alphaPaint)
        super.onDraw(canvas)
    }

    private fun calculateFrameAndTitlePos() {
        val centralX = width / 2
        val centralY = height / 2
        val minLength = min(centralX, centralY)
        val marginRatio =
            if (horizontalFrameRatio > 1f) FRAME_MARGIN_RATIO * ((1f / horizontalFrameRatio) * 1.5f)
            else FRAME_MARGIN_RATIO
        val strokeLength = minLength - (minLength * marginRatio) + 80
        val strokeWidth = STROKE_WIDTH.toPx()
        outerFrame.set(
            centralX - strokeLength,
            centralY - strokeLength / horizontalFrameRatio,
            centralX + strokeLength,
            centralY + strokeLength / horizontalFrameRatio
        )
        innerFrame.set(
            outerFrame.left + strokeWidth,
            outerFrame.top + strokeWidth,
            outerFrame.right - strokeWidth,
            outerFrame.bottom - strokeWidth
        )

    }

    private fun Float.toPx() =
        TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, this, resources.displayMetrics)

}