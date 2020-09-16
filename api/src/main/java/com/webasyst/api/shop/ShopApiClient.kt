package com.webasyst.api.shop

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

class ShopApiClient private constructor(private val apiClient: ApiClient, context: Context) :
    ApiClientBase(context)
{
    private constructor(context: Context) : this(ApiClient.getInstance(context), context)

    suspend fun getOrders(
        url: String,
        installationClientId: String
    ): Response<OrderList> = wrapApiCall {
        val authCodesResponse = apiClient.getInstallationApiAuthCodes(setOf(installationClientId))
        if (authCodesResponse.isFailure()) throw authCodesResponse.getFailureCause()

        val authCodes = authCodesResponse.getSuccess()
        val authCode = authCodes[installationClientId] ?: throw RuntimeException("Failed to obtain authorization code")

        val accessToken = getToken(url, authCode, SCOPE)

        val res = client.get<OrderList>("$url/api.php/shop.order.search") {
            parameter("access_token", accessToken.token)
            parameter("limit", 10)
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

    companion object : SingletonHolder<ShopApiClient, Context>(::ShopApiClient){
        const val SCOPE = "shop"
    }
}
