package com.webasyst.x.auth

import android.app.Application
import android.content.Intent
import android.view.View
import androidx.annotation.DrawableRes
import androidx.lifecycle.AndroidViewModel
import com.webasyst.auth.WebasystAuthHelper
import com.webasyst.x.common.getActivity

class WelcomeViewModel(application: Application) : AndroidViewModel(application) {
    open val welcomeTitle: String = application.getString(R.string.intro_welcome_title)
    open val welcomeText: String = application.getString(R.string.intro_welcome_text)
    @DrawableRes
    open val appLogoRes = R.drawable.img_appicon_example

    fun onPhoneSignIn(view: View) {
        val intent = Intent(view.context, SignInActivity::class.java)
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
