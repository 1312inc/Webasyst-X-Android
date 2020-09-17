package com.webasyst.api

import com.webasyst.api.site.AccessToken
import com.webasyst.util.SingletonHolder

class TokenCache private constructor(u: Unit) {
    private val tokens: MutableMap<Key, Token> = HashMap()

    fun get(url: String, scope: String): AccessToken? = synchronized(tokens) {
        val key = Key(url, scope)
        val token = tokens[key] ?: return null
        if (token.createdAt + TOKEN_EXPIRATION < System.currentTimeMillis()) {
            tokens.remove(key)
            return null
        }
        return token.token
    }

    fun set(url: String, scope: String, token: AccessToken) = synchronized(tokens) {
        val key = Key(url, scope)
        tokens[key] = Token(token)
    }

    fun clear() = synchronized(tokens) {
        tokens.clear()
    }

    data class Key(
        val url: String,
        val scope: String
    )

    class Token(
        val token: AccessToken,
        val createdAt: Long = System.currentTimeMillis()
    )

    companion object : SingletonHolder<TokenCache, Unit>(::TokenCache) {
        const val TOKEN_EXPIRATION: Long = 1000 * 60 * 60 * 24 // 24 hours
    }
}
