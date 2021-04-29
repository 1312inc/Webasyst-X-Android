package com.webasyst.x.installations

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.ColorFilter
import android.graphics.LinearGradient
import android.graphics.Matrix
import android.graphics.Paint
import android.graphics.PixelFormat
import android.graphics.Rect
import android.graphics.Shader
import android.graphics.Typeface
import android.graphics.drawable.Drawable
import kotlin.math.min

class InstallationIconDrawable(context: Context, val icon: Installation.Icon) : Drawable() {
    private val locale =
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.N) {
            context.resources.configuration.locales[0]
        } else {
            context.resources.configuration.locale
        }
    private var size = 0f
    private var cx = 0f
    private var cy = 0f
    private var r = 0f

    private val iconMatrix = Matrix()

    private val gradientPaint = Paint(Paint.ANTI_ALIAS_FLAG)

    private val textPaint by lazy(LazyThreadSafetyMode.NONE) {
        Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.WHITE
            typeface = Typeface.DEFAULT_BOLD
        }
    }

    override fun onBoundsChange(bounds: Rect) {
        super.onBoundsChange(bounds)

        cx = bounds.left / 2f + bounds.right / 2f
        cy = bounds.top / 2f + bounds.bottom / 2f
        val minSize = min(bounds.width(), bounds.height())
        r = minSize / 2f
        size = minSize.toFloat()
        when (icon) {
            is Installation.Icon.AutoIcon -> {
                gradientPaint.shader = LinearGradient(
                    0f,
                    0f,
                    size,
                    0f,
                    intArrayOf(0xFFFF0078.toInt(), 0xFFFF5900.toInt()),
                    null,
                    Shader.TileMode.MIRROR
                )
                textPaint.color = Color.WHITE
                iconMatrix.reset()
            }
            is Installation.Icon.GradientIcon -> {
                gradientPaint.shader = LinearGradient(
                    0f,
                    size,
                    0f,
                    0f,
                    intArrayOf(Color.parseColor(icon.from), Color.parseColor(icon.to)),
                    null,
                    Shader.TileMode.MIRROR
                )
                textPaint.color = try {
                    Color.parseColor(icon.textColor)
                } catch (e: Throwable) {
                    Color.WHITE
                }
                iconMatrix.setRotate(icon.angle.toFloat(), size / 2, size / 2)
            }
        }
        textPaint.textSize = when(icon.text.length) {
            3 -> 9 * size / 32
            4 -> 10 * size / 32
            else -> 12 * size / 32
        }
    }

    override fun draw(canvas: Canvas) {
        canvas.save()
        canvas.setMatrix(iconMatrix)
        canvas.drawCircle(cx, cy, r, gradientPaint)
        canvas.restore()
        if (icon.twoLine) {
            drawText(canvas, icon.text.slice(0 until (icon.text.length / 2)), 1)
            drawText(canvas, icon.text.slice((icon.text.length / 2) until icon.text.length), 2)
        } else {
            drawText(canvas, icon.text, 0)
        }
    }

    /**
     * [line] - 0 for single line, 1 for first line, 2 for second line
     */
    private fun drawText(canvas: Canvas, text: String, line: Int) {
        val textUpper = text.toUpperCase(locale)
        val textWidth = textPaint.measureText(textUpper)
        val lineHeight = textPaint.fontMetrics.ascent
        val offset = lineHeight / 10
        when (line) {
            0 -> {
                canvas.drawText(textUpper, (size - textWidth) / 2, (size - lineHeight) / 2 + offset, textPaint)
            }
            1 -> {
                canvas.drawText(
                    textUpper,
                    (size - textWidth) / 2,
                    (size - lineHeight) / 2 + (lineHeight * 0.6f) + offset,
                    textPaint
                )
            }
            2 -> {
                canvas.drawText(
                    textUpper,
                    (size - textWidth) / 2,
                    (size - lineHeight) / 2 - (lineHeight * 0.6f) + offset,
                    textPaint
                )
            }
        }
    }

    override fun setAlpha(alpha: Int) {
        textPaint.alpha = alpha
        gradientPaint.alpha = alpha
    }

    override fun setColorFilter(colorFilter: ColorFilter?) {
        textPaint.colorFilter = colorFilter
        gradientPaint.colorFilter = colorFilter
    }

    override fun getOpacity(): Int = PixelFormat.TRANSPARENT
}
