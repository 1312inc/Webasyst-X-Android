package com.webasyst.x

import android.app.Application
import com.webasyst.api.site.SiteApiClient
import com.webasyst.auth.configureWebasystAuth

class WebasystXApplication : Application() {
    override fun onCreate() {
        super.onCreate()

        configureWebasystAuth {
            setClientId(BuildConfig.CLIENT_ID)
            setAuthEndpoint("${BuildConfig.WEBASYST_HOST}/id/oauth2/auth/code")
            setTokenEndpoint("${BuildConfig.WEBASYST_HOST}/id/oauth2/auth/token")
            setCallbackUri(getString(R.string.app_redirect_scheme) + "://oidc_callback")
            setScope("token:${SiteApiClient.SCOPE}")
        }
    }
}
