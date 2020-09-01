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
import io.ktor.client.request.request
import io.ktor.client.request.url
import io.ktor.client.statement.HttpResponse
import io.ktor.http.ContentType
import io.ktor.http.HttpMethod
import io.ktor.http.isSuccess
import io.ktor.utils.io.jvm.javaio.copyTo
import java.io.File

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

    suspend fun getInstallationList(): Response<List<Installation>> =
        wrapApiCall { doGet(INSTALLATION_LIST_ENDPOINT) }

    suspend fun getUserInfo(): Response<UserInfo> =
        wrapApiCall { doGet(USER_LIST_ENDPOINT) }

    suspend fun downloadUserpic(url: String, file: File): Unit =
        downloadFile(url, file)

    private inline fun <reified T> wrapApiCall(block: () -> T): Response<T> = try {
        Response.success(block())
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

    private suspend inline fun downloadFile(url: String, file: File) {
        val response = client.request<HttpResponse> {
            url(url)
            method = HttpMethod.Get
        }

        if (response.status.isSuccess()) {
            if (file.exists()) {
                file.delete()
            }
            file.outputStream().use { fo ->
                response.content.copyTo(fo)
            }
        }

    }

    companion object : SingletonHolder<ApiClient, Context>(::ApiClient) {
        private const val INSTALLATION_LIST_ENDPOINT = "${BuildConfig.WEBASYST_HOST}/id/api/v1/installations/"
        private const val USER_LIST_ENDPOINT = "${BuildConfig.WEBASYST_HOST}/id/api/v1/profile/"
    }
}
