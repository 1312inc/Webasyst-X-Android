package com.webasyst.x.auth

import android.widget.ImageView
import androidx.databinding.BindingAdapter
import com.google.android.material.textfield.TextInputLayout

object DataBinding {
    @BindingAdapter("app:srcCompat")
    @JvmStatic
    fun bindSrcCompatRes(imageView: ImageView, res: Int) {
        imageView.setImageResource(res)
    }

    @JvmStatic
    @BindingAdapter("errorText")
    fun bindErrorText(view: TextInputLayout, error: Int?) {
        if (error == null) {
            view.error = null
        } else {
            view.error = view.context.getString(error)
        }
    }
}
