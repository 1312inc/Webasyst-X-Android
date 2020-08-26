package com.webasyst.x.api

sealed class Response<out T> {
    open fun onSuccess(block: (value: T) -> Unit) : Response<T> = this
    open fun onFailure(block: (cause: Throwable) -> Unit) : Response<T> = this

    private class Success<out T>(val value: T) : Response<T>() {
        override fun onSuccess(block: (value: T) -> Unit): Response<T> {
            block(value)
            return this
        }
    }

    private class Failure<out T>(val cause: Throwable) : Response<T>() {
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
