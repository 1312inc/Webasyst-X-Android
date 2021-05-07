package com.webasyst.x.installations

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.webasyst.x.R
import com.webasyst.x.WebasystXApplication
import com.webasyst.x.cache.DataCache
import com.webasyst.x.databinding.FragInstallationListBinding
import com.webasyst.x.util.findRootNavController
import kotlinx.android.synthetic.main.frag_installation_list.installationList

class InstallationListFragment :
    Fragment(R.layout.frag_installation_list)
{
    private val viewModel by lazy(LazyThreadSafetyMode.NONE) {
        ViewModelProvider(requireActivity()).get(InstallationListViewModel::class.java)
    }

    private val adapter by lazy(LazyThreadSafetyMode.NONE) {
        InstallationListAdapter()
    }

    private val dataCache: DataCache by lazy(LazyThreadSafetyMode.NONE) {
        (requireActivity().application as WebasystXApplication).dataCache
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

        viewModel.navController = view.findRootNavController()

        installationList.adapter = adapter
        installationList.layoutManager = LinearLayoutManager(
            requireContext(),
            LinearLayoutManager.VERTICAL,
            false
        )

        viewModel.installations.observe(viewLifecycleOwner) { installations ->
            val previousSize = adapter.itemCount
            adapter.submitList(installations) {
                if (previousSize == 0 && installations?.isNotEmpty() == true) {
                    val selectedInstallation = dataCache.selectedInstallationId
                    if (selectedInstallation.isNotEmpty()) {
                        adapter.setSelectedItemById(selectedInstallation)
                    } else {
                        adapter.setSelectedItem(0)
                    }
                }
            }
        }

        viewModel.state.observe(viewLifecycleOwner) { state ->
            if (state == InstallationListViewModel.STATE_EMPTY) {
                view.findRootNavController().navigate(R.id.action_global_noInstallationsFragment)
            }
        }
    }

    fun updateInstallations(idToSelect: String?) {
        InstallationsController.updateInstallations()
    }

    interface InstallationListView {
        fun updateInstallations(idToSelect: String?)
    }
}
