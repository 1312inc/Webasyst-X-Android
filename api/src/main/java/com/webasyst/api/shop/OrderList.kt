package com.webasyst.api.shop

import com.google.gson.annotations.SerializedName
import com.webasyst.api.ApiError

data class OrderList(
    @SerializedName("offset")
    val offset: Int,
    @SerializedName("limit")
    val limit: Int,
    @SerializedName("count")
    val count: Int,
    @SerializedName("orders")
    val orders: List<Order>,
    @SerializedName("error")
    override val error: String?,
    @SerializedName("error_description")
    override val errorDescription: String?
) : ApiError.ApiCallResponse
