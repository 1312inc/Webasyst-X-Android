package com.webasyst.x.site.domainlist

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.LifecycleOwner
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.webasyst.x.R
import com.webasyst.x.databinding.RowSiteDomainListBinding
import java.lang.ref.WeakReference

class DomainListAdapter(lifecycleOwner: LifecycleOwner) : ListAdapter<Domain, DomainListAdapter.DomainViewHolder>(Companion) {
    private val lifecycleOwnerRef = WeakReference(lifecycleOwner)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): DomainViewHolder =
        DataBindingUtil.inflate<RowSiteDomainListBinding>(
            LayoutInflater.from(parent.context),
            R.layout.row_site_domain_list,
            parent,
            false
        ).let { binding ->
            binding.lifecycleOwner = lifecycleOwnerRef.get()
            DomainViewHolder(binding)
        }

    override fun onBindViewHolder(holder: DomainViewHolder, position: Int) =
        holder.bind(getItem(position))

    class DomainViewHolder(private val binding: RowSiteDomainListBinding) :
        RecyclerView.ViewHolder(binding.root)
    {
        fun bind(domain: Domain) {
            binding.domain = domain
            binding.executePendingBindings()
        }
    }

    companion object : DiffUtil.ItemCallback<Domain>() {
        override fun areItemsTheSame(oldItem: Domain, newItem: Domain): Boolean =
            oldItem.id == newItem.id

        override fun areContentsTheSame(oldItem: Domain, newItem: Domain): Boolean =
            oldItem == newItem
    }
}
