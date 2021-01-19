package com.webasyst.api

interface TokenCache {
    suspend fun get(url: String, scope: String): AccessToken?
    suspend fun set(url: String, scope: String, token: AccessToken)
    suspend fun clear()
}

