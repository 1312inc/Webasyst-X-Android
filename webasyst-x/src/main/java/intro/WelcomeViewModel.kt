package com.webasyst.x.intro

import android.content.Intent
import android.net.Uri
import android.view.View
import androidx.core.content.ContextCompat
import androidx.lifecycle.ViewModel
import com.webasyst.auth.WebasystAuthHelper
import com.webasyst.x.R
import com.webasyst.x.util.getActivity

class WelcomeViewModel : ViewModel() {
    fun onOpenGithub(view: View) {
        val browserIntent = Intent(Intent.ACTION_VIEW, Uri.parse(view.context.getString(R.string.intro_welcome_github_repo_url)))
        ContextCompat.startActivity(view.context, browserIntent, null)
    }

    fun onSignIn(view: View) {
        view.getActivity()?.javaClass?.let {
            val authHelper = WebasystAuthHelper(view.context)
            authHelper.signIn(it)
        }
    }
}
