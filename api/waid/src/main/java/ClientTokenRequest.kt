package com.webasyst.waid

import com.google.gson.annotations.SerializedName

data class ClientTokenRequest(
    @SerializedName("client_id")
    val clientIDs: Set<String>
)
