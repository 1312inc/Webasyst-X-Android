package com.webasyst.x.site.domainlist

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.webasyst.x.R
import com.webasyst.x.databinding.FragSiteDomainListBinding
import kotlinx.android.synthetic.main.frag_site_domain_list.domainList

class DomainListFragment : Fragment(R.layout.frag_site_domain_list) {
    private val viewModel by lazy(LazyThreadSafetyMode.NONE) {
        ViewModelProvider(
            this,
            DomainListViewModel.Factory(
                requireActivity().application,
                arguments?.getString(INSTALLATION_ID),
                arguments?.getString(INSTALLATION_URL)
            )
        )
            .get(DomainListViewModel::class.java)
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

        viewModel.domainList.observe(viewLifecycleOwner,
            Observer<List<Domain>> { t -> adapter.submitList(t) })
    }

    companion object {
        const val INSTALLATION_ID = "installationId"
        const val INSTALLATION_URL = "installationUrl"
    }
}
