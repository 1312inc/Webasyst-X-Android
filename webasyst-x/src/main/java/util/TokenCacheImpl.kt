package com.webasyst.x.util

import android.content.Context
import androidx.core.content.edit
import com.google.gson.Gson
import com.google.gson.annotations.SerializedName
import com.google.gson.reflect.TypeToken
import com.webasyst.api.AccessToken
import com.webasyst.api.TokenCache
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import java.lang.reflect.Type
import kotlin.reflect.KProperty

class TokenCacheImpl(context: Context, private val expiration: Long = TOKEN_EXPIRATION) : TokenCache {
    private val prefs = context.getSharedPreferences("tokens", Context.MODE_PRIVATE)
    private val gson = Gson()
    private val lock = Mutex()

    private var authCodes: Map<String, String> by preference(AUTH_CODES)
    private var tokens: Map<String, Token> by preference(TOKENS)

    override suspend fun clear() = lock.withLock {
        prefs.edit {
            remove(AUTH_CODES)
            remove(TOKENS)
        }
    }

    override suspend fun get(url: String, scope: String): AccessToken? = lock.withLock {
        val key = Key(url, scope).toString()
        val token = tokens[key] ?: return null
        return if (token.createdAt + expiration < System.currentTimeMillis()) {
            tokens = (tokens.toMutableMap().apply { remove(key) })
            null
        } else {
            token.token
        }
    }

    override suspend fun set(url: String, scope: String, token: AccessToken) = lock.withLock {
        val key = Key(url, scope).toString()
        tokens = tokens + (key to Token(token, System.currentTimeMillis()))
    }

    override suspend fun getAuthCode(installationId: String): String? = lock.withLock {
        return authCodes[installationId]
    }

    override suspend fun setAuthCode(installationId: String, code: String) = lock.withLock {
        authCodes = authCodes + (installationId to code)
    }

    private inline fun <reified T> preference(key: String): Preference<T> {
        val type = object : TypeToken<T>() {}
            .type
        return Preference(key, type)
    }

    inner class Preference<T>(private val key: String, private val type: Type) {
        operator fun getValue(thisRef: Any?, property: KProperty<*>): T =
            gson.fromJson(prefs.getString(key, "{}"), type)

        operator fun setValue(thisRef: Any?, property: KProperty<*>, value: T) =
            prefs.edit { putString(key, gson.toJson(value)) }
    }

    private class Key(val url: String, val scope: String) {
        override fun toString() = "$url:$scope"
    }

    class Token(
        @SerializedName("token")
        val token: AccessToken,
        @SerializedName("created_at")
        val createdAt: Long = System.currentTimeMillis()
    )

    companion object {
        const val AUTH_CODES = "auth_codes"
        const val TOKENS = "tokens"
        const val TOKEN_EXPIRATION: Long = 1000 * 60 * 60 * 24 // 24 hours
    }
}
