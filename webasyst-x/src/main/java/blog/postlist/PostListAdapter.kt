package com.webasyst.x.blog.postlist

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.navigation.findNavController
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.webasyst.x.R
import com.webasyst.x.databinding.RowBlogPostListItemBinding
import kotlinx.android.synthetic.main.row_blog_post_list_item.view.imageView

class PostListAdapter(private val urlBase: String) : ListAdapter<Post, PostListAdapter.PostViewHolder>(Companion) {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PostViewHolder =
        RowBlogPostListItemBinding
            .inflate(LayoutInflater.from(parent.context), parent, false)
            .let { PostViewHolder(urlBase, it) }

    override fun onBindViewHolder(holder: PostViewHolder, position: Int) =
        holder.bind(getItem(position))

    class PostViewHolder(private val urlBase: String, private val binding: RowBlogPostListItemBinding) :
        RecyclerView.ViewHolder(binding.root)
    {
        private val userPic = itemView.imageView

        init {
            itemView.setOnClickListener { view ->
                val post = binding.post ?: return@setOnClickListener
                val navController = view.findNavController()
                if (navController.currentDestination?.id == R.id.postListFragment) {
                    navController.navigate(
                        PostListFragmentDirections.actionPostListFragmentToPostViewFragment(post)
                    )
                }
            }
        }

        fun bind(post: Post) {
            post.user?.photoUrl20?.let {
                Glide.with(userPic)
                    .load(urlBase + it.replace(".20x20", ""))
                    .override(userPic.width, userPic.height)
                    .thumbnail(Glide.with(userPic).load(R.drawable.ic_userpic_placeholder))
                    .into(userPic)
            } ?: itemView.imageView.setImageResource(R.drawable.ic_userpic_placeholder)
            binding.imageView
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
