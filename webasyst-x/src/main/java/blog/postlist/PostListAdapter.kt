package com.webasyst.x.blog.postlist

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.navigation.findNavController
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.webasyst.x.databinding.RowBlogPostListItemBinding

class PostListAdapter : ListAdapter<Post, PostListAdapter.PostViewHolder>(Companion) {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PostViewHolder =
        RowBlogPostListItemBinding
            .inflate(LayoutInflater.from(parent.context), parent, false)
            .let { PostViewHolder(it) }

    override fun onBindViewHolder(holder: PostViewHolder, position: Int) =
        holder.bind(getItem(position))

    class PostViewHolder(private val binding: RowBlogPostListItemBinding) :
        RecyclerView.ViewHolder(binding.root)
    {
        init {
            itemView.setOnClickListener { view ->
                val post = binding.post ?: return@setOnClickListener
                view
                    .findNavController()
                    .navigate(PostListFragmentDirections.actionPostListFragmentToPostViewFragment(post))
            }
        }

        fun bind(post: Post) {
            binding.post = post
            binding.executePendingBindings()
        }
    }

    companion object : DiffUtil.ItemCallback<Post>() {
        override fun areItemsTheSame(oldItem: Post, newItem: Post): Boolean =
            oldItem.id == newItem.id

        override fun areContentsTheSame(oldItem: Post, newItem: Post): Boolean =
            oldItem == newItem
    }
}
