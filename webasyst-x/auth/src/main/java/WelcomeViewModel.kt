package com.webasyst.x.auth

import android.app.Application
import android.content.Intent
import android.view.View
import androidx.annotation.DrawableRes
import androidx.core.os.bundleOf
import androidx.lifecycle.AndroidViewModel
import com.webasyst.auth.WebasystAuthHelper
import com.webasyst.x.auth.SignInActivity.Companion.ARG_AUTH_TYPE
import com.webasyst.x.auth.SignInActivity.Companion.AUTH_TYPE_PHONE
import com.webasyst.x.auth.SignInActivity.Companion.AUTH_TYPE_QR
import com.webasyst.x.common.getActivity

class WelcomeViewModel(application: Application) : AndroidViewModel(application) {
    val welcomeTitle: String = application.getString(R.string.intro_welcome_title)
    val welcomeText: String = application.getString(R.string.intro_welcome_text)
    @DrawableRes
    val appLogoRes = R.drawable.img_appicon_example

    fun onPhoneSignIn(view: View) {
        val intent = Intent(view.context, SignInActivity::class.java).apply {
            putExtras(bundleOf(ARG_AUTH_TYPE to AUTH_TYPE_PHONE))
        }
        view.context.startActivity(intent)
    }

    fun onQRSignIn(view: View) {
        val intent = Intent(view.context, SignInActivity::class.java).apply {
            putExtras(bundleOf(ARG_AUTH_TYPE to AUTH_TYPE_QR))
        }
        view.context.startActivity(intent)
    }

    fun onSignIn(view: View) {
        view.getActivity()?.let { activity ->
            activity.javaClass.let { activityClass ->
                val authHelper = WebasystAuthHelper(activity)
                authHelper.signIn(activityClass)
            }
        }
    }
}
