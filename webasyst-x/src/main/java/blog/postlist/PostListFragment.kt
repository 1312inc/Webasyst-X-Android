package com.webasyst.x.blog.postlist

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.webasyst.x.databinding.FragBlogPostListBinding
import com.webasyst.x.main.MainFragment
import kotlinx.android.synthetic.main.frag_blog_post_list.postListView

class PostListFragment : Fragment() {
    private val viewModel by lazy(LazyThreadSafetyMode.NONE) {
        ViewModelProvider(
            this,
            PostListViewModel.Factory(
                requireActivity().application,
                arguments?.getString(MainFragment.INSTALLATION_ID),
                arguments?.getString(MainFragment.INSTALLATION_URL)
            )
        )
            .get(PostListViewModel::class.java)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View = FragBlogPostListBinding
        .inflate(inflater, container, false)
        .let { binding ->
            binding.viewModel = viewModel
            binding.lifecycleOwner = viewLifecycleOwner
            binding.root
        }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val adapter = PostListAdapter()
        postListView.layoutManager = LinearLayoutManager(
            postListView.context,
            LinearLayoutManager.VERTICAL,
            false
        )
        postListView.adapter = adapter

        viewModel.postList.observe(viewLifecycleOwner) { posts ->
            adapter.submitList(posts.map { Post(it) })
        }
    }
}
