package com.webasyst.api

import androidx.annotation.CallSuper
import com.google.gson.reflect.TypeToken
import io.ktor.client.HttpClient
import io.ktor.client.request.HttpRequestBuilder
import io.ktor.client.request.accept
import io.ktor.client.request.forms.MultiPartFormDataContent
import io.ktor.client.request.forms.formData
import io.ktor.client.request.get
import io.ktor.client.request.headers
import io.ktor.client.request.parameter
import io.ktor.client.request.post
import io.ktor.client.statement.HttpResponse
import io.ktor.http.ContentType
import io.ktor.util.toByteArray
import java.nio.charset.Charset

abstract class ApiModule(
    config: ApiClientConfiguration,
    installation: Installation,
    private val waidAuthenticator: WAIDAuthenticator,
) {
    protected val client = config.httpClient
    protected val gson = config.gson
    private val tokenCache = config.tokenCache
    protected val scope = config.scope
    private val installationId = installation.id
    protected val urlBase = installation.urlBase
    private val joinedScope = scope.joinToString(separator = ",")

    @CallSuper
    open suspend fun HttpRequestBuilder.configureRequest() {
        val accessToken = getToken()
        parameter(ACCESS_TOKEN, accessToken.token)
        accept(ContentType.Application.Json)
    }

    suspend fun getToken(): AccessToken {
        try {
            val cached = tokenCache.get(url = urlBase, scope = joinedScope)
            if (null != cached) return cached

            val authCodesResponse = waidAuthenticator.getInstallationApiAuthCodes(setOf(installationId))
            if (authCodesResponse.isFailure()) throw authCodesResponse.getFailureCause()

            val authCodes = authCodesResponse.getSuccess()
            val authCode = authCodes[installationId]
                ?: throw RuntimeException("Failed to obtain authorization code")

            return getToken(urlBase, authCode)
        } catch (e: Throwable) {
            throw TokenException(e)
        }
    }

    protected suspend fun getToken(url: String, authCode: String): AccessToken {
        try {
            val response = client.post<String>("$url/api.php/token-headless") {
                headers {
                    accept(ContentType.Application.Json)
                }
                body = MultiPartFormDataContent(formData {
                    append("code", authCode)
                    append("scope", joinedScope)
                    append("client_id", "com.webasyst.x")
                })
            }

            val token = gson.fromJson(response, AccessToken::class.java)
            if (token.error != null) {
                throw TokenError(token)
            }
            tokenCache.set(url, joinedScope, token)
            return token
        } catch (e: Throwable) {
            throw TokenException(e)
        }
    }

    protected suspend inline fun <reified T> HttpClient.doGet(urlString: String, block: HttpRequestBuilder.() -> Unit = {}) = apiRequest {
        get<HttpResponse>(urlString) {
            configureRequest()
            apply(block)
        }.parse(object : TypeToken<T>() {})
    }

    protected suspend inline fun <reified T> HttpClient.doPost(urlString: String, block: HttpRequestBuilder.() -> Unit = {}) = apiRequest {
        post<HttpResponse>(urlString) {
            configureRequest()
            apply(block)
        }.parse(object : TypeToken<T>() {})
    }

    protected suspend fun <T> HttpResponse.parse(typeToken: TypeToken<T>): T {
        val body = content.toByteArray().toString(Charset.forName("UTF8"))
        try {
            return gson.fromJson(body, typeToken.type)
        } catch (e: Throwable) {
            throw ApiError(gson.fromJson(body, ApiError.ApiCallResponse::class.java))
        }
    }

    companion object {
        const val ACCESS_TOKEN = "access_token"
    }
}
