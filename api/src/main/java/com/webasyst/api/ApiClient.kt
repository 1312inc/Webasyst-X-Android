package com.webasyst.api

import android.content.Context
import com.webasyst.auth.withFreshAccessToken
import com.webasyst.util.SingletonHolder
import io.ktor.client.request.accept
import io.ktor.client.request.headers
import io.ktor.client.request.post
import io.ktor.client.request.request
import io.ktor.client.request.url
import io.ktor.client.statement.HttpResponse
import io.ktor.http.ContentType
import io.ktor.http.HttpMethod
import io.ktor.http.contentType
import io.ktor.http.isSuccess
import io.ktor.utils.io.jvm.javaio.copyTo
import java.io.File

class ApiClient private constructor(context: Context) : ApiClientBase(context) {
    suspend fun getInstallationList(): Response<List<Installation>> =
        wrapApiCall { doGet(INSTALLATION_LIST_ENDPOINT) }

    suspend fun getUserInfo(): Response<UserInfo> =
        wrapApiCall { doGet(USER_LIST_ENDPOINT) }

    suspend fun getInstallationApiAuthCodes(appClientIDs: Set<String>): Response<Map<String, String>> {
        val r = doPost<Map<String, String>>(CLIENT_LIST, ClientTokenRequest(appClientIDs))
        return Response.success(r)
    }

    suspend fun postCloudSignUp(): Response<CloudSignup> = wrapApiCall {
        authService.withFreshAccessToken { accessToken ->
            client.post(CLOUD_SIGNUP_ENDPOINT) {
                headers {
                    accept(ContentType.Application.Json)
                    append("Authorization", "Bearer $accessToken")
                }
            }
        }
    }

    suspend fun downloadUserpic(url: String, file: File): Unit =
        downloadFile(url, file)

    private suspend inline fun <reified T> doPost(url: String, data: Any): T =
        authService.withFreshAccessToken { accessToken ->
            client.post(url) {
                headers {
                    accept(ContentType.Application.Json)
                    append("Authorization", "Bearer $accessToken")
                }
                contentType(ContentType.Application.Json)
                body = data
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
        private const val CLOUD_SIGNUP_ENDPOINT = "${BuildConfig.WEBASYST_HOST}/id/api/v1/cloud/signup/"
        private const val INSTALLATION_LIST_ENDPOINT = "${BuildConfig.WEBASYST_HOST}/id/api/v1/installations/"
        private const val USER_LIST_ENDPOINT = "${BuildConfig.WEBASYST_HOST}/id/api/v1/profile/"
        private const val CLIENT_LIST = "${BuildConfig.WEBASYST_HOST}/id/api/v1/auth/client/"
    }
}
