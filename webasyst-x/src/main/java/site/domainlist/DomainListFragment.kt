package com.webasyst.x.site.domainlist

import android.os.Bundle
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.webasyst.x.R
import com.webasyst.x.databinding.FragSiteDomainListBinding
import com.webasyst.x.main.MainFragment
import kotlinx.android.synthetic.main.frag_site_domain_list.domainList
import kotlinx.coroutines.launch

class DomainListFragment : Fragment(R.layout.frag_site_domain_list) {
    private val viewModel by lazy(LazyThreadSafetyMode.NONE) {
        ViewModelProvider(
            this,
            DomainListViewModel.Factory(
                requireActivity().application,
                arguments?.getString(MainFragment.INSTALLATION_ID),
                arguments?.getString(MainFragment.INSTALLATION_URL)
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
    ): View = DataBindingUtil.inflate<FragSiteDomainListBinding>(
        inflater,
        R.layout.frag_site_domain_list,
        container,
        false
    ).let { binding ->
        binding.viewModel = viewModel
        binding.lifecycleOwner = viewLifecycleOwner
        binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val adapter = DomainListAdapter(viewLifecycleOwner)
        domainList.layoutManager = LinearLayoutManager(requireContext(), LinearLayoutManager.VERTICAL, false)
        domainList.adapter = adapter

        viewModel.domainList.observe(viewLifecycleOwner, { t -> adapter.submitList(t) })
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
