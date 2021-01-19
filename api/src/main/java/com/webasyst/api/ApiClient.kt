package com.webasyst.api

import com.google.gson.Gson
import com.google.gson.GsonBuilder
import io.ktor.client.HttpClient
import io.ktor.client.engine.HttpClientEngine
import io.ktor.client.features.json.GsonSerializer
import io.ktor.client.features.json.JsonFeature

class ApiClient private constructor(
    modules: Map<Class<out ApiModule>, (config: ApiClientConfiguration, waidAuthenticator: WAIDAuthenticator) -> ApiModuleFactory<ApiModule>>,
    engine: HttpClientEngine,
    override val tokenCache: TokenCache,
    val waidAuthenticator: WAIDAuthenticator,
) : ApiClientConfiguration {
    val modules = modules
        .mapValues { (_, creator) -> creator.invoke(this, waidAuthenticator) }

    override val scope = this.modules
        .map { (_, factory) -> factory.scope }
    override val gson: Gson = GsonBuilder()
        .apply {
            configure(this@ApiClient.modules.mapNotNull { (_, factory) -> factory.gsonConfigurator })
        }
        .create()
    override val httpClient = HttpClient(engine) {
        install(JsonFeature) {
            serializer = GsonSerializer {
                configure(this@ApiClient.modules.mapNotNull { (_, factory) -> factory.gsonConfigurator })
            }
        }
    }

    fun <T : ApiModule> getFactory(cls: Class<T>): ApiModuleFactory<*> =
        modules[cls] ?: throw IllegalArgumentException("Factory for $cls not found")

    fun configure(gsonBuilder: GsonBuilder) =
        gsonBuilder.configure(this@ApiClient.modules.mapNotNull { (_, factory) -> factory.gsonConfigurator })

    private fun GsonBuilder.configure(
        blocks: List<GsonBuilder.() -> Unit>
    ) = blocks.forEach(::apply)

    class Builder {
        private val modules = mutableMapOf<Class<out ApiModule>, (config: ApiClientConfiguration, waidAuthenticator: WAIDAuthenticator) -> ApiModuleFactory<ApiModule>>()
        var waidAuthenticator: WAIDAuthenticator? = null
        var httpClientEngine: HttpClientEngine? = null
        var tokenCache: TokenCache = TokenCacheRamImpl()

        fun <T: ApiModule> addModuleFactory(cls: Class<T>, factory: (config: ApiClientConfiguration, waidAuthenticator: WAIDAuthenticator) -> ApiModuleFactory<T>) {
            modules[cls] = factory
        }

        fun build(): ApiClient = ApiClient(
            modules = modules,
            waidAuthenticator = waidAuthenticator ?: throw IllegalStateException("WAID authenticator must be set"),
            engine = httpClientEngine ?: throw IllegalStateException("HttpClientEngine must be set"),
            tokenCache = tokenCache,
        )
    }

    companion object {
        operator fun invoke(block: Builder.() -> Unit): ApiClient =
            Builder().apply(block).build()
    }
}
