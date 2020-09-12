package com.webasyst.x.shop.orders

import com.webasyst.api.shop.Order
import java.util.Calendar

data class Order(
    val id: Int,
    val title: String,
    val caption1: Calendar,
    val caption2: String
) {
    constructor(order: Order) : this(
        id = order.id.toInt(),
        title = order.idEncoded,
        caption1 = order.createDatetime,
        caption2 = "%.2f %s".format(order.total.toDouble(), order.currency)
    )
}
