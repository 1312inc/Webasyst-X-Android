package com.webasyst.x.util

import android.view.View
import androidx.databinding.BindingAdapter

object DataBinding {
    @JvmStatic
    @BindingAdapter("app:gone")
    fun bindGone(view: View, gone: Boolean) {
        view.visibility = if (gone) View.GONE else View.VISIBLE
    }
}
