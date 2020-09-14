package com.webasyst.api

class ApiError(val error: String, description: String) : Throwable(description) {
    constructor(res: ApiCallResponse) : this(
        error = res.error ?: "",
        description = res.errorDescription ?: "Unknown api error"
    )

    interface ApiCallResponse {
        val error: String?
        val errorDescription: String?
    }
}
