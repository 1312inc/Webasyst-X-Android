package com.webasyst.x

import android.app.PendingIntent
import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.webasyst.x.auth.WebasystAuthService
import com.webasyst.x.auth.WebasystAuthStateManager
import kotlinx.android.synthetic.main.activity_main.login
import kotlinx.android.synthetic.main.activity_main.refreshTokenTextView
import kotlinx.android.synthetic.main.activity_main.tokenExpirationTextView
import kotlinx.android.synthetic.main.activity_main.tokenTextView
import net.openid.appauth.AuthState
import net.openid.appauth.AuthorizationException
import net.openid.appauth.AuthorizationRequest
import net.openid.appauth.AuthorizationResponse
import net.openid.appauth.AuthorizationServiceDiscovery
import java.util.Date

class MainActivity : AppCompatActivity(), WebasystAuthStateManager.AuthStateObserver {
    private val stateStore by lazy(LazyThreadSafetyMode.NONE) {
        WebasystAuthStateManager.getInstance(this)
    }
    val webasystAuthService by lazy(LazyThreadSafetyMode.NONE) {
        WebasystAuthService.getInstance(this)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val intent = intent
        if (null != intent) {
            when (intent.action) {
                ACTION_UPDATE_AFTER_AUTHORIZATION -> {
                    val response = AuthorizationResponse.fromIntent(intent)
                    val e = AuthorizationException.fromIntent(intent)
                    stateStore.updateAfterAuthorization(response, e)

                    if (null != response) {
                        WebasystAuthService.getInstance(this).performTokenRequest(response.createTokenExchangeRequest())
                    }
                }
            }
        }

        login.setOnClickListener { authorize() }
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

    private fun authorize() {
        val authRequest = webasystAuthService.createAuthorizationRequest()
        val intent = createPostAuthorizationIntent(authRequest, null)
        webasystAuthService.authorize(authRequest, intent)
    }

    private fun createPostAuthorizationIntent(
        request: AuthorizationRequest,
        discoveryDoc: AuthorizationServiceDiscovery?
    ) : PendingIntent {
        val intent = Intent(this, this.javaClass)
        intent.action = ACTION_UPDATE_AFTER_AUTHORIZATION
        if (null != discoveryDoc) {
            intent.putExtra(WebasystAuthService.EXTRA_AUTH_SERVICE_DISCOVERY, discoveryDoc.docJson.toString())
        }
        return PendingIntent.getActivity(this, request.hashCode(), intent, 0)
    }

    companion object {
        internal const val ACTION_UPDATE_AFTER_AUTHORIZATION = "update_post_auth"
    }

}
