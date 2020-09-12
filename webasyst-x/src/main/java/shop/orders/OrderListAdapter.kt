package com.webasyst.x.shop.orders

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.webasyst.x.databinding.RowShopOrderListItemBinding

class OrderListAdapter : ListAdapter<Order, OrderListAdapter.OrderViewHolder>(Companion) {
    class OrderViewHolder(private val binding: RowShopOrderListItemBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(order: Order) {
            binding.order = order
            binding.executePendingBindings()
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): OrderViewHolder =
        RowShopOrderListItemBinding
            .inflate(LayoutInflater.from(parent.context), parent, false)
            .let { OrderViewHolder(it) }

    override fun onBindViewHolder(holder: OrderViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    companion object : DiffUtil.ItemCallback<Order>() {
        override fun areItemsTheSame(oldItem: Order, newItem: Order): Boolean =
            oldItem.id == newItem.id

        override fun areContentsTheSame(oldItem: Order, newItem: Order): Boolean =
            oldItem == newItem
    }
}
