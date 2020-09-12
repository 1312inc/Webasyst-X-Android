package com.webasyst.api

import android.content.Context
import com.webasyst.auth.WebasystAuthService
import com.webasyst.auth.withFreshAccessToken
import com.webasyst.util.CalendarAdapter
import io.ktor.client.HttpClient
import io.ktor.client.engine.android.Android
import io.ktor.client.features.json.GsonSerializer
import io.ktor.client.features.json.JsonFeature
import io.ktor.client.request.accept
import io.ktor.client.request.get
import io.ktor.client.request.headers
import io.ktor.client.request.parameter
import io.ktor.http.ContentType
import java.util.Calendar

abstract class ApiClientBase(context: Context) {
    protected val authService: WebasystAuthService = WebasystAuthService.getInstance(context)

    val client = HttpClient(Android) {
        engine {
            socketTimeout = 30_000
            connectTimeout = 30_000
        }

        install(JsonFeature) {
            serializer = GsonSerializer {
                registerTypeAdapter(Calendar::class.java, CalendarAdapter())
            }
        }
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
