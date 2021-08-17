package com.webasyst.x.installations

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.webasyst.x.common.InstallationListStore
import com.webasyst.x.common.XComponentProvider
import com.webasyst.x.common.findRootNavController
import com.webasyst.x.installations.databinding.FragInstallationListBinding
import kotlinx.coroutines.runBlocking

class InstallationListFragment : Fragment(R.layout.frag_installation_list) {
    lateinit var binding: FragInstallationListBinding

    private val componentProvider: XComponentProvider by lazy(LazyThreadSafetyMode.NONE) {
        requireActivity().application as XComponentProvider
    }

    private val viewModel by lazy(LazyThreadSafetyMode.NONE) {
        ViewModelProvider(requireActivity()).get(InstallationListViewModel::class.java)
    }

    private val adapter by lazy(LazyThreadSafetyMode.NONE) {
        InstallationListAdapter(componentProvider)
    }

    private val installationsController: InstallationsController by lazy(LazyThreadSafetyMode.NONE) {
        InstallationsController.instance(componentProvider)
    }

    private val dataCache: InstallationListStore by lazy(LazyThreadSafetyMode.NONE) {
        (requireActivity().application as XComponentProvider).getInstallationListStore()
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragInstallationListBinding.inflate(inflater, container, false)
        binding.viewModel = viewModel
        binding.lifecycleOwner = viewLifecycleOwner
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        viewModel.navController = view.findRootNavController()

        binding.swipeRefresh.setOnRefreshListener {
            viewModel.updateInstallations(force = true)
        }

        binding.installationList.adapter = adapter
        binding.installationList.layoutManager = LinearLayoutManager(
            requireContext(),
            LinearLayoutManager.VERTICAL,
            false
        )

        viewModel.installations.observe(viewLifecycleOwner) { installations ->
            val previousSize = adapter.itemCount
            adapter.submitList(installations) {
                if (previousSize == 0 && installations?.isNotEmpty() == true) {
                    val selectedInstallation = runBlocking { dataCache.getSelectedInstallationId() }
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

            binding.swipeRefresh.isRefreshing = state == InstallationListViewModel.STATE_LOADING
        }
    }

    override fun onResume() {
        super.onResume()
        viewModel.updateInstallations(force = false)
    }

    fun updateInstallations(idToSelect: String?) {
        installationsController.updateInstallations {}
    }

    interface InstallationListView {
        fun updateInstallations(idToSelect: String?)
    }
}
