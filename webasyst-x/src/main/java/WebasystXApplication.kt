package com.webasyst.x

import android.app.Application
import com.webasyst.api.ApiClient
import com.webasyst.api.TokenCache
import com.webasyst.api.TokenCacheRamImpl
import com.webasyst.api.blog.BlogApiClient
import com.webasyst.api.blog.BlogApiClientFactory
import com.webasyst.api.shop.ShopApiClient
import com.webasyst.api.shop.ShopApiClientFactory
import com.webasyst.api.site.SiteApiClient
import com.webasyst.api.site.SiteApiClientFactory
import com.webasyst.api.webasyst.WebasystApiClient
import com.webasyst.api.webasyst.WebasystApiClientFactory
import com.webasyst.auth.WebasystAuthService
import com.webasyst.auth.WebasystAuthStateStore
import com.webasyst.auth.configureWebasystAuth
import com.webasyst.waid.WAIDClient
import com.webasyst.x.cache.DataCache
import io.ktor.client.engine.HttpClientEngine
import io.ktor.client.engine.android.Android
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import net.openid.appauth.AuthState

class WebasystXApplication : Application(), WebasystAuthStateStore.Observer {
    override fun onCreate() {
        super.onCreate()

        configureWebasystAuth {
            setClientId(BuildConfig.CLIENT_ID)
            setHost(BuildConfig.WEBASYST_HOST)
            setCallbackUri(getString(R.string.app_redirect_scheme) + "://oidc_callback")
            setScope(
                webasystScope.joinToString(
                    prefix = "token:",
                    separator = "."
                )
            )
        }

        WebasystAuthStateStore.getInstance(this).addObserver(this)
    }

    val webasystScope = listOf(
        SiteApiClient.SCOPE,
        ShopApiClient.SCOPE,
        BlogApiClient.SCOPE,
        WebasystApiClient.SCOPE,
    )

    val httpEngine: HttpClientEngine by lazy {
        Android.create {
            connectTimeout = 30_000
            socketTimeout = 30_000
        }
    }
    val tokenCache: TokenCache by lazy {
        TokenCacheRamImpl()
    }
    val webasystAuthService: WebasystAuthService by lazy {
        WebasystAuthService.getInstance(this)
    }
    val waidClient by lazy {
        WAIDClient(
            authService = webasystAuthService,
            engine = httpEngine,
            waidHost = BuildConfig.WEBASYST_HOST,
        )
    }

    val apiClient: ApiClient by lazy {
        ApiClient {
            addModuleFactory(BlogApiClient::class.java) { config, waidAuthenticator ->
                BlogApiClientFactory(config, waidAuthenticator)
            }
            addModuleFactory(ShopApiClient::class.java) { config, waidAuthenticator ->
                ShopApiClientFactory(config, waidAuthenticator)
            }
            addModuleFactory(SiteApiClient::class.java) { config, waidAuthenticator ->
                SiteApiClientFactory(config, waidAuthenticator)
            }
            addModuleFactory(WebasystApiClient::class.java) { config, waidAuthenticator ->
                WebasystApiClientFactory(config, waidAuthenticator)
            }
            waidAuthenticator = waidClient
            httpClientEngine = httpEngine
            tokenCache = this@WebasystXApplication.tokenCache
        }
    }

    val dataCache by lazy {
        DataCache(this)
    }

    override fun onAuthStateChange(state: AuthState?) {
        if (state?.isAuthorized == false) {
            GlobalScope.launch(Dispatchers.Default) {
                tokenCache.clear()
            }
        }
    }
}
