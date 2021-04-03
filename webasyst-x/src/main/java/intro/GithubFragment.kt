package com.webasyst.x.intro

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.View
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import com.webasyst.x.R
import kotlinx.android.synthetic.main.frag_intro_github.button

class GithubFragment : Fragment(R.layout.frag_intro_github) {
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        button.setOnClickListener {
            val browserIntent = Intent(Intent.ACTION_VIEW, Uri.parse(it.context.getString(R.string.intro_welcome_github_repo_url)))
            ContextCompat.startActivity(it.context, browserIntent, null)
        }
    }
}
