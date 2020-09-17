package com.webasyst.x.blog.postlist

import android.os.Bundle
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.webasyst.x.R
import com.webasyst.x.databinding.FragBlogPostListBinding
import com.webasyst.x.main.MainFragment
import kotlinx.android.synthetic.main.frag_blog_post_list.postListView
import kotlinx.coroutines.launch

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

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(true)
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

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.std_tab_menu, menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean =
        when (item.itemId) {
            R.id.refresh -> {
                lifecycleScope.launch {
                    viewModel.updateData(requireContext())
                }
                true
            }

            else -> super.onOptionsItemSelected(item)
        }

    override fun onStart() {
        super.onStart()
        lifecycleScope.launch {
            viewModel.updateData(requireContext())
        }
    }
}
