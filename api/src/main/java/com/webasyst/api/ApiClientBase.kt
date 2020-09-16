package com.webasyst.api

import android.content.Context
import com.webasyst.api.site.AccessToken
import com.webasyst.auth.WebasystAuthService
import com.webasyst.auth.withFreshAccessToken
import io.ktor.client.request.accept
import io.ktor.client.request.forms.MultiPartFormDataContent
import io.ktor.client.request.forms.formData
import io.ktor.client.request.get
import io.ktor.client.request.headers
import io.ktor.client.request.parameter
import io.ktor.client.request.post
import io.ktor.http.ContentType

abstract class ApiClientBase(context: Context) {
    protected val authService: WebasystAuthService = WebasystAuthService.getInstance(context)
    private val gson = GSON.getInstance(Unit)
    protected val client = HttpClient.getInstance(Unit)

    protected suspend fun getToken(url: String, authCode: String, scope: String): AccessToken = try {
        val response = client.post<String>("$url/api.php/token-headless") {
            headers {
                accept(ContentType.Application.Json)
            }
            body = MultiPartFormDataContent(formData {
                append("code", authCode)
                append("scope", scope)
                append("client_id", "com.webasyst.x")
            })
        }

        gson.fromJson(response, AccessToken::class.java)
    } catch (e: Throwable) {
        throw TokenException(e)
    }

    protected inline fun <reified T> wrapApiCall(block: () -> T): Response<T> = try {
        Response.success(block())
    } catch (e: Throwable) {
        Response.failure(e)
    }

    protected suspend inline fun <reified T> doGet(url: String, params: Map<String, String>? = null): T =
        authService.withFreshAccessToken { accessToken ->
            client.get(url) {
                params?.let {
                    it.forEach { (key, value) ->
                        parameter(key, value)
                    }
                }
                headers {
                    accept(ContentType.Application.Json)
                    append("Authorization", "Bearer $accessToken")
                }
            }
        }

}
