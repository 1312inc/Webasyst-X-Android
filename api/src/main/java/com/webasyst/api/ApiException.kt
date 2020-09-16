package com.webasyst.api

sealed class ApiException(cause: Throwable) : RuntimeException(cause)

class WaidException(cause: Throwable) : ApiException(cause) {
    override val message: String
        get() = "WAID error: ${cause?.message}"

    override fun getLocalizedMessage(): String =
        "WAID error: ${cause?.localizedMessage}"

    override fun toString() = "WAID error"
}

class TokenException(cause: Throwable) : ApiException(cause) {
    override val message: String
        get() = "Failed to obtain access token: ${cause?.message}"

    override fun getLocalizedMessage(): String =
        "Failed to obtain access token: ${cause?.localizedMessage}"

    override fun toString() = "Failed to obtain access token"
}
