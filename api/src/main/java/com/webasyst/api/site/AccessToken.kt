package com.webasyst.api.site

import com.google.gson.annotations.SerializedName

data class AccessToken(
    @SerializedName("access_token")
    val token: String
)
