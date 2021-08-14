package com.webasyst.x.util

import android.graphics.Rect
import android.view.View
import android.widget.Button
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.core.text.HtmlCompat
import androidx.core.widget.TextViewCompat
import androidx.databinding.BindingAdapter
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.textfield.TextInputLayout
import com.webasyst.api.WebasystException
import com.webasyst.x.R
import java.text.DateFormat
import java.util.Calendar

object DataBinding {
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

    @JvmStatic
    @BindingAdapter("errorText")
    fun bindErrorText(view: TextInputLayout, error: Int?) {
        if (error == null) {
            view.error = null
        } else {
            view.error = view.context.getString(error)
        }
    }

    @JvmStatic
    @BindingAdapter("webasystError")
    fun bindWebasystError(view: TextView, error: Throwable?) {
        if (null == error) return

        view.text = error.localizedMessage ?: error.message
        val drawableRes = if (error is WebasystException) {
            if (error.webasystCode == WebasystException.ERROR_CONNECTION_FAILED) {
                R.drawable.ic_offline
            } else {
                R.drawable.ic_error
            }
        } else {
            R.drawable.ic_error
        }
        val resolution = (view.context.resources.displayMetrics.density * 96).toInt()
        val drawable = ContextCompat.getDrawable(view.context, drawableRes)
        drawable?.bounds = Rect(0, 0, resolution, resolution)
        TextViewCompat.setCompoundDrawablesRelative(view, null, drawable, null, null)
    }

    /**
     * This multi-purpose binding adapter is meant to fully configure "error details" button.
     * It sets button visibility, appropriate text and
     * configures onClickListener to show meaningful details dialog.
     */
    @JvmStatic
    @BindingAdapter("webasystErrorDetailsButton")
    fun bindWebasystErrorDetalsButton(button: Button, error: Throwable?) {
        when {
            null == error ->
                button.visibility = View.GONE
            error !is WebasystException ->
                button.visibility = View.GONE
            error.webasystCode == WebasystException.ERROR_INVALID_ERROR_OBJECT -> {
                button.visibility = View.VISIBLE
                button.setText(R.string.btn_error_details_malformed_response)
                button.setOnClickListener { btn ->
                    MaterialAlertDialogBuilder(btn.context)
                        .setPositiveButton(R.string.btn_ok) { dialog, _ -> dialog.dismiss() }
                        .setTitle(R.string.error_details_title_response_body)
                        .setMessage(error.responseBody)
                        .show()
                }
            }
            else ->
                button.visibility = View.GONE
        }
    }
}
