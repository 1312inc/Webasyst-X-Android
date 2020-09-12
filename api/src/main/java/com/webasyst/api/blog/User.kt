package com.webasyst.api.blog

import com.google.gson.annotations.SerializedName

data class User(
    @SerializedName("id")
    val id: String,
    @SerializedName("name")
    val name: String,
    @SerializedName("photo_url_20")
    val photoUrl20: String,
)
