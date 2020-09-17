package com.webasyst.x

import android.app.Application
import com.webasyst.api.TokenCache
import com.webasyst.api.blog.BlogApiClient
import com.webasyst.api.shop.ShopApiClient
import com.webasyst.api.site.SiteApiClient
import com.webasyst.api.webasyst.WebasystApiClient
import com.webasyst.auth.WebasystAuthStateStore
import com.webasyst.auth.configureWebasystAuth
import net.openid.appauth.AuthState

class WebasystXApplication : Application(), WebasystAuthStateStore.Observer {
    override fun onCreate() {
        super.onCreate()

        configureWebasystAuth {
            setClientId(BuildConfig.CLIENT_ID)
            setAuthEndpoint("${BuildConfig.WEBASYST_HOST}/id/oauth2/auth/code")
            setTokenEndpoint("${BuildConfig.WEBASYST_HOST}/id/oauth2/auth/token")
            setCallbackUri(getString(R.string.app_redirect_scheme) + "://oidc_callback")
            setScope(
                listOf(
                    SiteApiClient.SCOPE,
                    ShopApiClient.SCOPE,
                    BlogApiClient.SCOPE,
                    WebasystApiClient.SCOPE
                ).joinToString(
                    prefix = "token:",
                    separator = "."
                )
            )
        }

        WebasystAuthStateStore.getInstance(this).addObserver(this)
    }

    override fun onAuthStateChange(state: AuthState?) {
        if (state?.isAuthorized == false) {
            TokenCache.getInstance(Unit).clear()
        }
    }
}
