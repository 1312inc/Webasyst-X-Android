package com.webasyst.x

import android.app.Activity
import android.app.Application
import android.content.Context
import androidx.fragment.app.Fragment
import coil.ImageLoader
import coil.decode.SvgDecoder
import com.google.gson.GsonBuilder
import com.webasyst.api.ApiClient
import com.webasyst.api.TokenCache
import com.webasyst.api.adapter.ListAdapter
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
import com.webasyst.x.auth.WelcomeFragment
import com.webasyst.x.cache.DataCache
import com.webasyst.x.common.InstallationListStore
import com.webasyst.x.common.UserInfoStore
import com.webasyst.x.common.XComponentProvider
import com.webasyst.x.installations.InstallationsController
import com.webasyst.x.intro.GithubFragment
import com.webasyst.x.pin_code.PinCodeStore
import com.webasyst.x.pin_code.PinCodeStoreImpl
import com.webasyst.x.util.TokenCacheImpl
import io.ktor.client.engine.HttpClientEngine
import io.ktor.client.engine.android.Android
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.launch
import net.openid.appauth.AuthState
import org.koin.android.ext.koin.androidContext
import org.koin.core.context.startKoin
import org.koin.dsl.module

class WebasystXApplication : Application(), WebasystAuthStateStore.Observer, XComponentProvider {
    private val applicationScope = MainScope()
    private val prefs by lazy {
        this.getSharedPreferences("config", Context.MODE_PRIVATE)
    }

    override fun onCreate() {
        super.onCreate()

        instance = this

        configureWebasystAuth {
            setClientId(BuildConfig.CLIENT_ID)
            setHost(BuildConfig.WEBASYST_HOST)
            setCallbackUri(BuildConfig.APPLICATION_ID + "://oidc_callback")
            setScope(webasystScope())
            setDeviceId("deviceId")
        }

        WebasystAuthStateStore.getInstance(this).addObserver(this)

        startKoin {
            androidContext(this@WebasystXApplication)

            modules(
                module {
                    single {
                        GsonBuilder()
                            .registerTypeAdapter(List::class.java, ListAdapter())
                            .serializeNulls()
                            .create()
                    }
                    single {
                        ImageLoader.Builder(get())
                            .components {
                                add(SvgDecoder.Factory())
                            }
                            .build()
                    }
                    single<PinCodeStore> {
                        PinCodeStoreImpl(get())
                    }
                }
            )
        }
    }

    override fun webasystScope() = listOf(
        SiteApiClient.SCOPE,
        ShopApiClient.SCOPE,
        BlogApiClient.SCOPE,
        WebasystApiClient.SCOPE,
    ).joinToString(
        prefix = "profile:write token:",
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

    override fun userInfoStore(): UserInfoStore = dataCache

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
        DataCache(this, applicationScope)
    }

    override fun getInstallationListStore(): InstallationListStore = dataCache

    override fun getApiClient(): ApiClient = apiClient_

    override fun getWAIDClient(): WAIDClient = waidClient

    override fun mainActivityClass(): Class<out Activity> = MainActivity::class.java

    override fun introSlides(): List<Fragment> = listOf(
        Fragment(R.layout.frag_intro_hello_world),
        Fragment(R.layout.frag_intro_projects),
        GithubFragment(),
        WelcomeFragment(),
    )

    override fun clientId(): String = BuildConfig.CLIENT_ID

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
