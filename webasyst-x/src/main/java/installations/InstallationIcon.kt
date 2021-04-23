package com.webasyst.x.installations

import android.content.Context
import android.util.AttributeSet
import androidx.databinding.BindingAdapter
import com.bumptech.glide.Glide

class InstallationIcon : androidx.appcompat.widget.AppCompatImageView {
    constructor(context: Context) : this(context, null)
    constructor(context: Context, attrs: AttributeSet?) : this(context, attrs, 0)
    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(context, attrs, defStyleAttr) {
        if (isInEditMode) {
            setImageDrawable(InstallationIconDrawable(context, Installation.Icon.AutoIcon("TEST")))
        }
        setWillNotDraw(false)
    }

    val glide by lazy { Glide.with(this) }

    var icon: Installation.Icon = Installation.Icon.AutoIcon("")
        set(value) {
            if (value is Installation.Icon.ImageIcon) {
                glide
                    .load(value.getThumb((50 * context.resources.displayMetrics.density).toInt()))
                    .circleCrop()
                    .into(this)
            } else {
                setImageDrawable(InstallationIconDrawable(context, value))
            }
            field = value
            invalidate()
        }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) =
        super.onMeasure(widthMeasureSpec, widthMeasureSpec)

    companion object {
        @JvmStatic
        @BindingAdapter("app:icon")
        fun bindAbbr(view: InstallationIcon, icon: Installation.Icon?) {
            view.icon = icon ?: Installation.Icon.AutoIcon("")
        }
    }
}
