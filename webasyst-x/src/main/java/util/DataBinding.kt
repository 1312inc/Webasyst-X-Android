package com.webasyst.x.util

import android.view.View
import android.widget.TextView
import androidx.core.text.HtmlCompat
import androidx.databinding.BindingAdapter
import java.text.DateFormat
import java.util.Calendar

object DataBinding {
    @JvmStatic
    @BindingAdapter("app:gone")
    fun bindGone(view: View, gone: Boolean) {
        view.visibility = if (gone) View.GONE else View.VISIBLE
    }

    @JvmStatic
    @BindingAdapter("app:html")
    fun bindHtml(view: TextView, html: String?) {
        val imageGetter = GlideImageGetter(view.context, view)
        view.text = if (null != html) {
            HtmlCompat.fromHtml(html, HtmlCompat.FROM_HTML_MODE_COMPACT, imageGetter, null)
        } else {
            null
        }
    }

    @JvmStatic
    @BindingAdapter("android:text")
    fun bindCalendar(view: TextView, calendar: Calendar?) {
        view.text = if (null == calendar) {
            ""
        } else {
            DateFormat.getDateTimeInstance().format(calendar.time)
        }
    }
}
