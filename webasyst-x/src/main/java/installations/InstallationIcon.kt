package com.webasyst.x.installations

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.LinearGradient
import android.graphics.Matrix
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

    var icon: Installation.Icon = Installation.Icon.AutoIcon(if (isInEditMode) "ЩШУК" else "")
        set(value) {
            field = value
            updateSizes()
        }
    private val iconMatrix = Matrix()

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
                val i = icon as Installation.Icon.GradientIcon
                gradientPaint.shader = LinearGradient(
                    0f,
                    0f,
                    0f,
                    size,
                    intArrayOf(Color.parseColor(i.from), Color.parseColor(i.to)),
                    null,
                    Shader.TileMode.MIRROR
                )
                textPaint.color = try {
                    Color.parseColor(i.textColor)
                } catch (e: Throwable) {
                    Color.WHITE
                }
                iconMatrix.setRotate(i.angle.toFloat(), size / 2, size / 2)
            }
        }
        textPaint.textSize = when(icon.text.length) {
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

    companion object {
        @JvmStatic
        @BindingAdapter("app:icon")
        fun bindAbbr(view: InstallationIcon, icon: Installation.Icon?) {
            view.icon = icon ?: Installation.Icon.AutoIcon("")
        }
    }
}
