package com.webasyst.api.blog

import com.google.gson.annotations.SerializedName
import com.webasyst.api.ApiError

data class Posts(
    @SerializedName("offset")
    val offset: Int,
    @SerializedName("limit")
    val limit: Int,
    @SerializedName("count")
    val count: Int,
    @SerializedName("posts")
    val posts: List<Post>?,
    @SerializedName("error")
    override val error: String?,
    @SerializedName("error_description")
    override val errorDescription: String?
) : ApiError.ApiCallResponse
