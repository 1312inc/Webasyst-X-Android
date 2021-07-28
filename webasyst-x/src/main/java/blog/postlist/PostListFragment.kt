package com.webasyst.x.blog.postlist

import android.os.Bundle
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import com.webasyst.x.R
import com.webasyst.x.databinding.FragBlogPostListBinding
import com.webasyst.x.installations.Installation
import com.webasyst.x.main.MainFragment
import kotlinx.coroutines.launch

class PostListFragment : Fragment() {
    private lateinit var binding: FragBlogPostListBinding
    private val installation by lazy { arguments?.getSerializable(MainFragment.INSTALLATION) as Installation? }
    private val viewModel by lazy(LazyThreadSafetyMode.NONE) {
        ViewModelProvider(
            this,
            PostListViewModel.Factory(
                requireActivity().application,
                installation?.id,
                installation?.url,
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
    ): View {
        binding = FragBlogPostListBinding.inflate(inflater, container, false)
        binding.viewModel = viewModel
        binding.lifecycleOwner = viewLifecycleOwner
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val adapter = PostListAdapter(installation?.url ?: "")
        binding.postListView.layoutManager = LinearLayoutManager(
            binding.postListView.context,
            LinearLayoutManager.VERTICAL,
            false
        )
        binding.postListView.adapter = adapter
        binding.postListView.addItemDecoration(
            DividerItemDecoration(binding.postListView.context, DividerItemDecoration.VERTICAL).apply {
                setDrawable(ContextCompat.getDrawable(binding.postListView.context, R.drawable.list_divider)!!)
            }
        )

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
