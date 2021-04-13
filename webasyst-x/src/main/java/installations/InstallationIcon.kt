package com.webasyst.x.installations

import android.content.Context
import android.util.AttributeSet
import androidx.databinding.BindingAdapter

class InstallationIcon : androidx.appcompat.widget.AppCompatImageView {
    constructor(context: Context) : this(context, null)
    constructor(context: Context, attrs: AttributeSet?) : this(context, attrs, 0)
    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(context, attrs, defStyleAttr) {
        if (isInEditMode) {
            setImageDrawable(InstallationIconDrawable(context, Installation.Icon.AutoIcon("TEST")))
        }
        setWillNotDraw(false)
    }

    var icon: Installation.Icon = Installation.Icon.AutoIcon("")
        set(value) {
            setImageDrawable(InstallationIconDrawable(context, value))
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
