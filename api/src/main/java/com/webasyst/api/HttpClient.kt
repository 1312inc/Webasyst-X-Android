package com.webasyst.api

import com.webasyst.util.CalendarAdapter
import com.webasyst.util.SingletonHolder
import io.ktor.client.HttpClient
import io.ktor.client.engine.android.Android
import io.ktor.client.features.json.GsonSerializer
import io.ktor.client.features.json.JsonFeature
import java.util.Calendar

object HttpClient : SingletonHolder<HttpClient, Unit>(::createHttpClient)

private fun createHttpClient(u: Unit): HttpClient =
    HttpClient(Android) {
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
