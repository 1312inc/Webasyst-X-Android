package com.webasyst.x.auth

import android.app.Service
import android.content.Intent
import android.os.IBinder
import net.openid.appauth.AuthorizationException
import net.openid.appauth.AuthorizationResponse

class WebasystAuthHelperService : Service() {
    private val authStateManager by lazy(LazyThreadSafetyMode.NONE) {
        WebasystAuthStateManager.getInstance(this)
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        if (null == intent) return START_NOT_STICKY

        when (intent.action) {
            ACTION_UPDATE_AFTER_AUTHORIZATION -> {
                val response = AuthorizationResponse.fromIntent(intent)
                val e = AuthorizationException.fromIntent(intent)
                authStateManager.updateAfterAuthorization(response, e)

                if (null != response) {
                    WebasystAuthService.getInstance(this).performTokenRequest(response.createTokenExchangeRequest())
                }
            }
        }

        return START_NOT_STICKY
    }

    override fun onBind(intent: Intent?): IBinder? = null

    companion object {
        internal const val ACTION_UPDATE_AFTER_AUTHORIZATION = "update_post_auth"
    }
}
