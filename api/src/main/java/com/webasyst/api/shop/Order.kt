package com.webasyst.api.shop

import com.google.gson.annotations.SerializedName
import java.util.Calendar

data class Order (
    @SerializedName("id")
    val id: String,
    @SerializedName("id_encoded")
    val idEncoded: String,
    @SerializedName("total")
    val total: String,
    @SerializedName("create_datetime")
    val createDatetime: Calendar,
    @SerializedName("currency")
    val currency: String,
)
