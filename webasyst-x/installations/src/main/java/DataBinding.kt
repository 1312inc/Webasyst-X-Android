package com.webasyst.x.installations

import android.graphics.Typeface
import android.text.format.DateFormat
import android.view.View
import android.widget.TextView
import androidx.databinding.BindingAdapter
import java.util.Calendar

object DataBinding {
    @JvmStatic
    @BindingAdapter("app:gone")
    fun bindGone(view: View, gone: Boolean) {
        view.visibility = if (gone) View.GONE else View.VISIBLE
    }

    @JvmStatic
    @BindingAdapter("insecureOrExpiration")
    fun bindInsecureOrExpiration(view: TextView, installation: Installation?) {
        when {
            null == installation -> {
                view.visibility = View.GONE
            }
            null != installation.cloudExpireDate -> {
                val now = Calendar.getInstance()
                val expireDate = DateFormat.getMediumDateFormat(view.context).format(installation.cloudExpireDate!!.time)
                if (installation.cloudExpireDate!!.before(now)) {
                    view.setTypeface(null, Typeface.BOLD)
                    view.text = view.context.resources.getString(R.string.expired_on, expireDate)
                    view.visibility = View.VISIBLE
                } else {
                    view.setTypeface(null, Typeface.NORMAL)
                    view.text = view.context.resources.getString(R.string.expires_on, expireDate)
                    view.visibility = View.VISIBLE
                }
            }
            installation.isInsecure -> {
                view.setTypeface(null, Typeface.NORMAL)
                view.setText(R.string.installation_connection_not_secure)
                view.visibility = View.VISIBLE
            }
            else -> {
                view.visibility = View.GONE
            }
        }
    }
}
