package com.webasyst.x.common

import android.text.TextWatcher
import android.view.View
import android.widget.TextView
import androidx.core.text.HtmlCompat
import androidx.databinding.BindingAdapter
import androidx.databinding.adapters.ListenerUtil

object DataBinding {

    @JvmStatic
    @BindingAdapter("app:html")
    fun bindHtml(view: TextView, html: String?) {
        // TODO: Implement image getter. Search for GlideImageGetter.
        view.text = if (null != html) {
            HtmlCompat.fromHtml(html, HtmlCompat.FROM_HTML_MODE_COMPACT, null, null)
        } else {
            null
        }
    }

    @JvmStatic
    @BindingAdapter("gone")
    fun bindGone(view: View, gone: Boolean?) {
        view.visibility =  when (gone) {
            true -> View.GONE
            false -> View.VISIBLE
            else -> View.VISIBLE
        }
    }

    @JvmStatic
    @BindingAdapter("textWatcher")
    fun setTextWatcher(view: TextView, newValue: TextWatcher?) {
        val oldValue = ListenerUtil.trackListener(view, newValue, R.id.listenerId)
        if (oldValue != null) {
            view.removeTextChangedListener(oldValue)
        }
        if (newValue != null) {
            view.addTextChangedListener(newValue)
        }
    }
}
