package com.webasyst.x

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.webasyst.x.auth.WebasystAuthService
import com.webasyst.x.auth.WebasystAuthStateManager
import kotlinx.android.synthetic.main.activity_main.login
import kotlinx.android.synthetic.main.activity_main.refreshTokenTextView
import kotlinx.android.synthetic.main.activity_main.tokenExpirationTextView
import kotlinx.android.synthetic.main.activity_main.tokenTextView
import net.openid.appauth.AuthState
import java.util.Date

class MainActivity : AppCompatActivity(), WebasystAuthStateManager.AuthStateObserver {
    private val stateStore by lazy(LazyThreadSafetyMode.NONE) {
        WebasystAuthStateManager.getInstance(this)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        login.setOnClickListener {
            val webasystAuthService = WebasystAuthService.getInstance(it.context)
            webasystAuthService.authorize(this)
        }
    }

    override fun onResume() {
        super.onResume()
        stateStore.addObserver(this)
    }

    override fun onPause() {
        super.onPause()
        stateStore.removeObserver(this)
    }

    override fun onChange(state: AuthState) {
        tokenTextView.text = state.accessToken
        tokenExpirationTextView.text = state.accessTokenExpirationTime?.let { Date(it).toString() }
        refreshTokenTextView.text = state.refreshToken
    }
}
