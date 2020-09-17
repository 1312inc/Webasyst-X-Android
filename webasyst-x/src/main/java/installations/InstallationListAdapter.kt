package com.webasyst.x.installations

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.webasyst.x.R
import com.webasyst.x.databinding.RowInstallationListBinding
import com.webasyst.x.util.Observable

class InstallationListAdapter : ListAdapter<Installation, InstallationListAdapter.InstallationViewHolder>(Companion) {
    var selectedPosition = RecyclerView.NO_POSITION
        private set
    private val selectionListeners = Observable<SelectionChangeListener>()

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

    fun setSelectedItem(position: Int) {
        if (position < itemCount) {
            notifyItemChanged(selectedPosition)
            selectedPosition = position
            notifyItemChanged(selectedPosition)
            selectionListeners.notifyObservers {
                onSelectionChange(selectedPosition, getItem(selectedPosition))
            }
        }
    }

    fun setSelectedItemById(id: String) {
        val position = currentList.indexOfFirst { it.id == id }
        if (position >= 0) setSelectedItem(position)
    }

    fun addSelectionListener(listener: SelectionChangeListener) =
        selectionListeners.addObserver(listener)

    inner class InstallationViewHolder(private val binding: RowInstallationListBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(installation: Installation, selected: Boolean) {
            itemView.setOnClickListener {
                setSelectedItem(layoutPosition)
            }

            itemView.isSelected = selected
            binding.installation = installation
            binding.executePendingBindings()
        }
    }

    fun interface SelectionChangeListener {
        fun onSelectionChange(position: Int, installation: Installation)
    }

    companion object : DiffUtil.ItemCallback<Installation>() {
        override fun areItemsTheSame(oldItem: Installation, newItem: Installation): Boolean =
            oldItem.id == newItem.id
        override fun areContentsTheSame(oldItem: Installation, newItem: Installation): Boolean =
            oldItem == newItem
    }
}
