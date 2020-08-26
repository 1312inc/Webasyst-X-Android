package com.webasyst.x

import android.app.Application
import com.webasyst.x.auth.WebasystAuthService

class WebasystXApplication : Application() {
    override fun onCreate() {
        super.onCreate()

        WebasystAuthService.configure {
            this.clientId = BuildConfig.CLIENT_ID
            this.authEndpoint = "${BuildConfig.WEBASYST_HOST}/id/oauth2/auth/code"
            this.tokenEndpoint = "${BuildConfig.WEBASYST_HOST}/id/oauth2/auth/token"
            this.callbackUri = getString(R.string.app_redirect_scheme) + "://oidc_callback"
        }
    }
}
