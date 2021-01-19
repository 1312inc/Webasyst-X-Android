package com.webasyst.api

/**
 * In-memory [TokenCache] implementation
 */
class TokenCacheRamImpl(
    private val expiration: Long = TOKEN_EXPIRATION
) : TokenCache {
    private val tokens: MutableMap<Key, Token> = HashMap()

    override suspend fun get(url: String, scope: String): AccessToken? = synchronized(tokens) {
        val key = Key(url, scope)
        val token = tokens[key] ?: return null
        if (token.createdAt + expiration < System.currentTimeMillis()) {
            tokens.remove(key)
            return null
        }
        return token.token
    }

    override suspend fun set(url: String, scope: String, token: AccessToken) = synchronized(tokens) {
        val key = Key(url, scope)
        tokens[key] = Token(token)
    }

    override suspend fun clear() = synchronized(tokens) {
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

    companion object {
        const val TOKEN_EXPIRATION: Long = 1000 * 60 * 60 * 24 // 24 hours
    }
}
