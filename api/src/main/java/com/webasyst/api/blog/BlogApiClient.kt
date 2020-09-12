package com.webasyst.api.blog

import android.content.Context
import com.google.gson.Gson
import com.webasyst.api.ApiClient
import com.webasyst.api.ApiClientBase
import com.webasyst.api.Response
import com.webasyst.api.site.AccessToken
import com.webasyst.util.SingletonHolder
import io.ktor.client.request.accept
import io.ktor.client.request.forms.MultiPartFormDataContent
import io.ktor.client.request.forms.formData
import io.ktor.client.request.get
import io.ktor.client.request.headers
import io.ktor.client.request.parameter
import io.ktor.client.request.post
import io.ktor.http.ContentType

class BlogApiClient private constructor (private val apiClient: ApiClient, context: Context) :
    ApiClientBase(context)
{
    private constructor(context: Context) : this(ApiClient.getInstance(context), context)

    private val gson = Gson()

    private suspend fun getToken(url: String, authCode: String): AccessToken {
        val response = client.post<String>("$url/api.php/token-headless") {
            headers {
                accept(ContentType.Application.Json)
            }
            body = MultiPartFormDataContent(formData {
                append("code", authCode)
                append("scope", SCOPE)
                append("client_id", "com.webasyst.x")
            })
        }
        return gson.fromJson(response, AccessToken::class.java)
    }

    suspend fun getPosts(
        url: String,
        installationClientId: String
    ): Response<Posts> = wrapApiCall {
        val authCodesResponse = apiClient.getInstallationApiAuthCodes(setOf(installationClientId))
        if (authCodesResponse.isFailure()) throw authCodesResponse.getFailureCause()

        val authCodes = authCodesResponse.getSuccess()
        val authCode = authCodes[installationClientId] ?: throw RuntimeException("Failed to obtain authorization code")

        val accessToken = getToken(url, authCode)

        client.get("$url/api.php/blog.post.search") {
            parameter("access_token", accessToken.token)
            parameter("limit", 10)
            parameter("hash", "author")
            headers {
                accept(ContentType.Application.Json)
            }
        }
    }

    companion object : SingletonHolder<BlogApiClient, Context>(::BlogApiClient) {
        const val SCOPE = "blog"
    }
}
