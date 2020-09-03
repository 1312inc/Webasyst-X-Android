package com.webasyst.x.installations

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.observe
import androidx.recyclerview.widget.LinearLayoutManager
import com.webasyst.x.R
import com.webasyst.x.databinding.FragInstallationListBinding
import kotlinx.android.synthetic.main.frag_installation_list.installationList

class InstallationListFragment : Fragment(R.layout.frag_installation_list) {
    private val viewModel by lazy(LazyThreadSafetyMode.NONE) {
        ViewModelProvider(this).get(InstallationListViewModel::class.java)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View = DataBindingUtil.inflate<FragInstallationListBinding>(
        inflater, R.layout.frag_installation_list, container, false
    ).let { binding ->
        binding.viewModel = viewModel
        binding.lifecycleOwner = viewLifecycleOwner
        binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val adapter = InstallationListAdapter()
        installationList.adapter = adapter
        installationList.layoutManager = LinearLayoutManager(
            requireContext(),
            LinearLayoutManager.VERTICAL,
            false
        )

        viewModel.installations.observe(viewLifecycleOwner) { installations ->
            adapter.submitList(installations)
        }
    }
}
