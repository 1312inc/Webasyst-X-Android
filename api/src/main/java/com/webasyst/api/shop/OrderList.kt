package com.webasyst.api.shop

import com.google.gson.annotations.SerializedName

data class OrderList(
    @SerializedName("offset")
    val offset: Int,
    @SerializedName("limit")
    val limit: Int,
    @SerializedName("count")
    val count: Int,
    @SerializedName("orders")
    val orders: List<Order>,
)
