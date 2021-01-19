package com.webasyst.waid

import com.google.gson.annotations.SerializedName

data class Installation(
    @SerializedName("id")
    val id: String,
    @SerializedName("domain")
    val domain: String,
    @SerializedName("url")
    val url: String
)
