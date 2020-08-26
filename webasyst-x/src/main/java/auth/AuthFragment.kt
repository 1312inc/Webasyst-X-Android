package com.webasyst.x.auth

import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import com.webasyst.x.MainActivity
import com.webasyst.x.R
import kotlinx.android.synthetic.main.frag_auth.signInButton

class AuthFragment : Fragment(R.layout.frag_auth) {
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        signInButton.setOnClickListener {
            (requireActivity() as MainActivity).authorize()
        }
    }
}
