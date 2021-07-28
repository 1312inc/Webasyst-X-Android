package com.webasyst.x.intro

import android.content.Intent
import android.view.View
import androidx.lifecycle.ViewModel
import com.webasyst.auth.WebasystAuthHelper
import com.webasyst.x.common.getActivity
import com.webasyst.x.signin.SignInActivity

class WelcomeViewModel : ViewModel() {
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
