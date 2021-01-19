package com.webasyst.api

inline fun <reified T> apiRequest(block: () -> T): Response<T> = try {
    Response.success(block())
} catch (e: Throwable) {
    Response.failure(e)
}

sealed class Response<out T> {
    open fun getSuccess(): T =
        throw IllegalStateException("getSuccess() can be called only on successful response")
    open fun getFailureCause(): Throwable =
        throw IllegalStateException("getFailureCause() can be called only on failed response")

    open fun onSuccess(block: (value: T) -> Unit) : Response<T> = this
    open fun onFailure(block: (cause: Throwable) -> Unit) : Response<T> = this

    open fun isSuccess(): Boolean = false
    open fun isFailure(): Boolean = false

    private class Success<out T>(val value: T) : Response<T>() {
        override fun getSuccess(): T = value
        override fun isSuccess() = true
        override fun onSuccess(block: (value: T) -> Unit): Response<T> {
            block(value)
            return this
        }
    }

    private class Failure<out T>(val cause: Throwable) : Response<T>() {
        override fun getFailureCause(): Throwable = cause
        override fun isFailure(): Boolean = true
        override fun onFailure(block: (cause: Throwable) -> Unit): Response<T> {
            block(cause)
            return this
        }
    }

    companion object {
        fun <T> success(value: T): Response<T> = Success(value)
        fun <T> failure(cause: Throwable): Response<T> = Failure(cause)
    }
}
