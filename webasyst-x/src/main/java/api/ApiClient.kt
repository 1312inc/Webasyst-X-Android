package com.webasyst.x.api

import android.content.Context
import com.webasyst.auth.WebasystAuthService
import com.webasyst.auth.withFreshAccessToken
import com.webasyst.x.BuildConfig
import com.webasyst.x.util.SingletonHolder
import io.ktor.client.HttpClient
import io.ktor.client.engine.android.Android
import io.ktor.client.features.json.GsonSerializer
import io.ktor.client.features.json.JsonFeature
import io.ktor.client.request.accept
import io.ktor.client.request.get
import io.ktor.client.request.headers
import io.ktor.http.ContentType

class ApiClient private constructor(context: Context) {
    private val authService = WebasystAuthService.getInstance(context)
    private val client = HttpClient(Android) {
        engine {
            socketTimeout = 30_000
            connectTimeout = 30_000
        }

        install(JsonFeature) {
            serializer = GsonSerializer()
        }
    }

    suspend fun getInstallationList(): Response<List<Installation>> = try {
        Response.success(doGet(INSTALLATION_LIST_ENDPOINT))
    } catch (e: Throwable) {
        Response.failure(e)
    }

    private suspend inline fun <reified T> doGet(url: String): T =
        authService.withFreshAccessToken { accessToken ->
            client.get(url) {
                headers {
                    accept(ContentType.Application.Json)
                    append("Authorization", "Bearer $accessToken")
                }
            }
        }

    companion object : SingletonHolder<ApiClient, Context>(::ApiClient) {
        private const val INSTALLATION_LIST_ENDPOINT = "${BuildConfig.WEBASYST_HOST}/id/api/v1/installations/"
    }
}
