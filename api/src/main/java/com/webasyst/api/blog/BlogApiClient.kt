package com.webasyst.api.blog

import android.content.Context
import com.webasyst.api.ApiClient
import com.webasyst.api.ApiClientBase
import com.webasyst.api.ApiError
import com.webasyst.api.Response
import com.webasyst.util.SingletonHolder
import io.ktor.client.request.accept
import io.ktor.client.request.get
import io.ktor.client.request.headers
import io.ktor.client.request.parameter
import io.ktor.http.ContentType

class BlogApiClient private constructor (private val apiClient: ApiClient, context: Context) :
    ApiClientBase(context)
{
    private constructor(context: Context) : this(ApiClient.getInstance(context), context)

    suspend fun getPosts(
        url: String,
        installationClientId: String
    ): Response<Posts> = wrapApiCall {
        val authCodesResponse = apiClient.getInstallationApiAuthCodes(setOf(installationClientId))
        if (authCodesResponse.isFailure()) throw authCodesResponse.getFailureCause()

        val authCodes = authCodesResponse.getSuccess()
        val authCode = authCodes[installationClientId] ?: throw RuntimeException("Failed to obtain authorization code")

        val accessToken = getToken(url, authCode, SCOPE)

        val res = client.get<Posts>("$url/api.php/blog.post.search") {
            parameter("access_token", accessToken.token)
            parameter("limit", 10)
            parameter("hash", "author")
            headers {
                accept(ContentType.Application.Json)
            }
        }

        if (res.error != null) {
            throw ApiError(res)
        } else {
            res
        }
    }

    companion object : SingletonHolder<BlogApiClient, Context>(::BlogApiClient) {
        const val SCOPE = "blog"
    }
}
