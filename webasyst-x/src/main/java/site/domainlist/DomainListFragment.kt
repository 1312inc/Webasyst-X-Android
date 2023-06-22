package com.webasyst.x.site.domainlist

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
import com.webasyst.x.databinding.FragSiteDomainListBinding
import com.webasyst.x.installations.Installation
import com.webasyst.x.main.MainFragment
import kotlinx.coroutines.launch

class DomainListFragment : Fragment(R.layout.frag_site_domain_list) {
    private lateinit var binding: FragSiteDomainListBinding
    private val installation by lazy { arguments?.getSerializable(MainFragment.INSTALLATION) as Installation? }
    private val viewModel by lazy(LazyThreadSafetyMode.NONE) {
        ViewModelProvider(
            this,
            DomainListViewModel.Factory(
                requireActivity().application,
                installation?.id,
                installation?.url,
            )
        )
            .get(DomainListViewModel::class.java)
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
        binding = FragSiteDomainListBinding.inflate(inflater, container, false)
        binding.viewModel = viewModel
        binding.lifecycleOwner = viewLifecycleOwner
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val adapter = DomainListAdapter(viewLifecycleOwner)
        binding.domainList.layoutManager = LinearLayoutManager(requireContext(), LinearLayoutManager.VERTICAL, false)
        binding.domainList.adapter = adapter
        binding.domainList.addItemDecoration(
            DividerItemDecoration(binding.domainList.context, DividerItemDecoration.VERTICAL).apply {
                setDrawable(ContextCompat.getDrawable(binding.domainList.context, R.drawable.list_divider)!!)
            }
        )

        viewModel.domainList.observe(viewLifecycleOwner, { t -> adapter.submitList(t) })
    }

    @Deprecated("")
    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.std_tab_menu, menu)
    }

    @Deprecated("")
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
