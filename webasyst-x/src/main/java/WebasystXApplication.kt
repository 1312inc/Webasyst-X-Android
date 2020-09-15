package com.webasyst.x

import android.app.Application
import android.content.Context
import androidx.core.content.edit
import com.webasyst.api.blog.BlogApiClient
import com.webasyst.api.shop.ShopApiClient
import com.webasyst.api.site.SiteApiClient
import com.webasyst.api.webasyst.WebasystApiClient
import com.webasyst.auth.WebasystAuthStateStore
import com.webasyst.auth.configureWebasystAuth
import com.webasyst.x.add_wa.AddWebasystViewModel

class WebasystXApplication : Application() {
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

        WebasystAuthStateStore.getInstance(this).addObserver(authStateobserver)
    }

    val authStateobserver = WebasystAuthStateStore.Observer { authState ->
        val preferences = this.getSharedPreferences(AddWebasystViewModel.PREFS_NAME, Context.MODE_PRIVATE)
        if (!authState.isAuthorized) {
            preferences.edit {
                remove(AddWebasystViewModel.AUTH_ENDPOINT_KEY)
            }
        }
    }
}
