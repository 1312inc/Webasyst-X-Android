package com.webasyst.x.installations

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.drawerlayout.widget.DrawerLayout
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.webasyst.x.R
import com.webasyst.x.databinding.RowInstallationListBinding
import com.webasyst.x.main.MainFragmentDirections
import com.webasyst.x.util.findRootNavController

class InstallationListAdapter : ListAdapter<Installation, InstallationListAdapter.InstallationViewHolder>(Companion) {
    private var selectedPosition = RecyclerView.NO_POSITION

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
        holder.bind(getItem(position), position == selectedPosition)

    inner class InstallationViewHolder(private val binding: RowInstallationListBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(installation: Installation, selected: Boolean) {
            itemView.setOnClickListener { view ->
                notifyItemChanged(selectedPosition)
                selectedPosition = layoutPosition
                notifyItemChanged(selectedPosition)

                view.rootView.findViewById<DrawerLayout>(R.id.drawerLayout)?.closeDrawers()
                view.findRootNavController().navigate(
                    MainFragmentDirections.actionMainFragmentSelf(
                        installationId = installation.id,
                        installationUrl = installation.url
                    ))
            }

            itemView.isSelected = selected
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
