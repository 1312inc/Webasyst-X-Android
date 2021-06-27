package com.webasyst.x.signin

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.webasyst.auth.WebasystAuthStateStore
import com.webasyst.x.MainActivity
import com.webasyst.x.R
import net.openid.appauth.AuthState

class SignInActivity : AppCompatActivity(), WebasystAuthStateStore.Observer {
    private val authState: WebasystAuthStateStore by lazy(LazyThreadSafetyMode.NONE) {
        WebasystAuthStateStore.getInstance(this)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity_sign_in)
    }

    override fun onStart() {
        super.onStart()
        authState.addObserver(this)
    }

    override fun onStop() {
        super.onStop()
        authState.removeObserver(this)
    }

    override fun onAuthStateChange(authState: AuthState) {
        if (authState.isAuthorized) {
            val intent = Intent(this, MainActivity::class.java)
            startActivity(intent)
            finish()
        }
    }
}
