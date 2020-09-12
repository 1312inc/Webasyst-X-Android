package com.webasyst.api.blog

import com.google.gson.annotations.SerializedName
import java.util.Calendar

data class Post(
    @SerializedName("id")
    val id: String,
    @SerializedName("datetime")
    val dateTime: Calendar,
    @SerializedName("title")
    val title: String,
    @SerializedName("text")
    val text: String,
    @SerializedName("user")
    val user: User,
)
