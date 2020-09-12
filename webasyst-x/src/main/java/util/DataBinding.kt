package com.webasyst.x.util

import android.view.View
import android.widget.TextView
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
    @BindingAdapter("android:text")
    fun bindCalendar(view: TextView, calendar: Calendar?) {
        view.text = if (null == calendar) {
            ""
        } else {
            DateFormat.getDateTimeInstance().format(calendar.time)
        }
    }
}
