package com.webasyst.x

import android.app.Application
import com.webasyst.api.ApiClient
import com.webasyst.api.TokenCache
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
import com.webasyst.x.common.InstallationListStore
import com.webasyst.x.common.XComponentProvider
import com.webasyst.x.installations.InstallationsController
import com.webasyst.x.util.TokenCacheImpl
import io.ktor.client.engine.HttpClientEngine
import io.ktor.client.engine.android.Android
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import net.openid.appauth.AuthState

class WebasystXApplication : Application(), WebasystAuthStateStore.Observer, XComponentProvider {
    override fun onCreate() {
        super.onCreate()

        instance = this

        configureWebasystAuth {
            setClientId(BuildConfig.CLIENT_ID)
            setHost(BuildConfig.WEBASYST_HOST)
            setCallbackUri(BuildConfig.APPLICATION_ID + "://oidc_callback")
            setScope(webasystScope)
        }

        WebasystAuthStateStore.getInstance(this).addObserver(this)
    }

    val webasystScope = listOf(
        SiteApiClient.SCOPE,
        ShopApiClient.SCOPE,
        BlogApiClient.SCOPE,
        WebasystApiClient.SCOPE,
    ).joinToString(
        prefix = "token:",
        separator = "."
    )

    val httpEngine: HttpClientEngine by lazy {
        Android.create {
            connectTimeout = 30_000
            socketTimeout = 30_000
        }
    }
    val tokenCache: TokenCache by lazy {
        TokenCacheImpl(this)
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

    private val apiClient_: ApiClient by lazy {
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
            clientId = "com.webasyst.x.android"
            waidAuthenticator = waidClient
            httpClientEngine = httpEngine
            tokenCache = this@WebasystXApplication.tokenCache
        }
    }

    val dataCache by lazy {
        DataCache(this)
    }

    override fun getInstallationListStore(): InstallationListStore = dataCache

    override fun getApiClient(): ApiClient = apiClient_

    override fun getWAIDClient(): WAIDClient = waidClient

    override fun onAuthStateChange(state: AuthState) {
        if (!state.isAuthorized) {
            GlobalScope.launch(Dispatchers.Default) {
                val installationsController = InstallationsController.instance(this@WebasystXApplication)
                installationsController.clearInstallations()
                installationsController.setSelectedInstallation(null)
                tokenCache.clear()
                dataCache.clearUserInfo()
            }
        }
    }

    companion object {
        lateinit var instance: WebasystXApplication
    }
}
