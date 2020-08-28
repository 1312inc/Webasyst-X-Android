package com.webasyst.auth

import kotlin.coroutines.suspendCoroutine

fun configureWebasystAuth(block: WebasystAuthConfiguration.Builder.() -> Unit) {
    val builder = WebasystAuthConfiguration.Builder(WebasystAuthService.currentConfiguration)
    builder.apply(block)
    WebasystAuthService.configure(builder.build())
}

suspend fun <T> WebasystAuthService.withFreshAccessToken(block: suspend (accessToken: String) -> T): T {
    val token = suspendCoroutine<String> { continuation ->
        this.withFreshAccessToken { accessToken, exception ->
            continuation.resumeWith(when {
                null != accessToken -> Result.success(accessToken)
                null != exception -> Result.failure(exception)
                else -> Result.failure(
                    IllegalStateException("Either token or exception should be set")
                )
            })
        }
    }
    return block(token)
}
