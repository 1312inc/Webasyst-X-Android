package com.webasyst.x.installations

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.webasyst.x.R
import com.webasyst.x.databinding.RowInstallationListBinding

class InstallationListAdapter : ListAdapter<Installation, InstallationListAdapter.InstallationViewHolder>(Companion) {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): InstallationViewHolder =
        DataBindingUtil.inflate<RowInstallationListBinding>(
            LayoutInflater.from(parent.context),
            R.layout.row_installation_list,
            parent,
            false
        ).let {
            InstallationViewHolder(it)
        }

    override fun onBindViewHolder(holder: InstallationViewHolder, position: Int) =
        holder.bind(getItem(position))

    class InstallationViewHolder(private val binding: RowInstallationListBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(installation: Installation) {
            binding.installation = installation
            binding.executePendingBindings()
        }
    }

    companion object : DiffUtil.ItemCallback<Installation>() {
        override fun areItemsTheSame(oldItem: Installation, newItem: Installation): Boolean =
            oldItem.id == newItem.id
        override fun areContentsTheSame(oldItem: Installation, newItem: Installation): Boolean =
            oldItem == newItem
    }
}
