package com.webasyst.x.installations

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.LinearGradient
import android.graphics.Paint
import android.graphics.Shader
import android.graphics.Typeface
import android.util.AttributeSet
import android.view.View
import androidx.databinding.BindingAdapter
import kotlin.math.min

class InstallationIcon : View {
    constructor(context: Context) : this(context, null)
    constructor(context: Context, attrs: AttributeSet?) : this(context, attrs, 0)
    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) :
        super(context, attrs, defStyleAttr)

    private var size = 0f
    private var cx = 0f
    private var cy = 0f
    private var r = 0f

    var abbr: String = if (isInEditMode) "ЩШУК" else ""
        set(value) {
            field = value
            updateSizes()
        }

    private val gradientPaint = Paint(Paint.ANTI_ALIAS_FLAG)

    private val textPaint by lazy(LazyThreadSafetyMode.NONE) {
        Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.WHITE
            typeface = Typeface.DEFAULT_BOLD
        }
    }

    private fun updateSizes() {
        cx = width / 2f
        cy = height / 2f
        r = min(cx, cy)
        size = min(width, height).toFloat()
        gradientPaint.shader = LinearGradient(
            0f,
            0f,
            size,
            0f,
            intArrayOf(0xFFFF0078.toInt(), 0xFFFF5900.toInt()),
            null,
            Shader.TileMode.MIRROR
        )
        textPaint.textSize = when(abbr.length) {
            3 -> 9 * size / 32
            4 -> 10 * size / 32
            else -> 12 * size / 32
        }
    }

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)
        updateSizes()
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) =
        super.onMeasure(widthMeasureSpec, widthMeasureSpec)

    override fun draw(canvas: Canvas) {
        super.draw(canvas)
        canvas.drawCircle(cx, cy, r, gradientPaint)
        if (abbr.length < 4) {
            drawText(canvas, abbr, 0)
        } else {
            drawText(canvas, abbr.slice(0 until (abbr.length / 2)), 1)
            drawText(canvas, abbr.slice((abbr.length / 2) until abbr.length), 2)
        }
    }

    /**
     * [line] - 0 for single line, 1 for first line, 2 for second line
     */
    private fun drawText(canvas: Canvas, text: String, line: Int) {
        val textWidth = textPaint.measureText(text)
        val lineHeight = textPaint.fontMetrics.ascent
        when (line) {
            0 -> {
                canvas.drawText(text, (size - textWidth) / 2, (size - lineHeight) / 2, textPaint)
            }
            1 -> {
                canvas.drawText(
                    text,
                    (size - textWidth) / 2,
                    (size - lineHeight) / 2 + (lineHeight * 0.6f),
                    textPaint
                )
            }
            2 -> {
                canvas.drawText(
                    text,
                    (size - textWidth) / 2,
                    (size - lineHeight) / 2 - (lineHeight * 0.6f),
                    textPaint
                )
            }
        }
    }

    companion object {
        @JvmStatic
        @BindingAdapter("app:abbr")
        fun bindAbbr(view: InstallationIcon, abbr: String) {
            view.abbr = abbr
        }
    }
}
