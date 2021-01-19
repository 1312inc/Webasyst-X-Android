package com.webasyst.api

import com.google.gson.annotations.SerializedName

class ApiError(val error: String, val app: String, val description: String) : Throwable(description) {
    constructor(error: ApiCallResponseInterface) : this(
        app = error.app ?: "",
        error = error.error ?: "",
        description = error.errorDescription ?: ""
    )

    data class ApiCallResponse(
        @SerializedName("app")
        override val app: String?,
        @SerializedName("error")
        override val error: String?,
        @SerializedName("error_description")
        override val errorDescription: String?
    ) : ApiCallResponseInterface

    interface ApiCallResponseInterface {
        val app: String?
        val error: String?
        val errorDescription: String?
    }

    companion object {
        const val APP_NOT_INSTALLED = "app_not_installed"
    }
}
