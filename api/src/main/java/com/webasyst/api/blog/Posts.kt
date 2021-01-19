package com.webasyst.api.blog

import com.google.gson.annotations.SerializedName

data class Posts(
    @SerializedName("offset")
    val offset: Int,
    @SerializedName("limit")
    val limit: Int,
    @SerializedName("count")
    val count: Int,
    @SerializedName("posts")
    val posts: List<Post>?,
)
