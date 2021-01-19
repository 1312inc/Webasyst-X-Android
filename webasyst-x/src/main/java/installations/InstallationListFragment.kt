package com.webasyst.x.installations

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.drawerlayout.widget.DrawerLayout
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.observe
import androidx.recyclerview.widget.LinearLayoutManager
import com.webasyst.x.R
import com.webasyst.x.auth.AuthFragmentDirections
import com.webasyst.x.databinding.FragInstallationListBinding
import com.webasyst.x.main.MainFragmentDirections
import com.webasyst.x.util.findRootNavController
import kotlinx.android.synthetic.main.frag_installation_list.installationList
import kotlinx.coroutines.launch

class InstallationListFragment :
    Fragment(R.layout.frag_installation_list),
    InstallationListAdapter.SelectionChangeListener
{
    private val viewModel by lazy(LazyThreadSafetyMode.NONE) {
        ViewModelProvider(this).get(InstallationListViewModel::class.java)
    }

    private val adapter by lazy(LazyThreadSafetyMode.NONE) {
        InstallationListAdapter()
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

        adapter.addSelectionListener(this)
        installationList.adapter = adapter
        installationList.layoutManager = LinearLayoutManager(
            requireContext(),
            LinearLayoutManager.VERTICAL,
            false
        )

        viewModel.installations.observe(viewLifecycleOwner) { installations ->
            val previousSize = adapter.itemCount
            adapter.submitList(installations) {
                if (previousSize == 0 && installations.isNotEmpty()) {
                    adapter.setSelectedItem(0)
                }
            }
        }
    }

    override fun onSelectionChange(position: Int, installation: Installation) {
        val navController = view?.findRootNavController() ?: return
        requireActivity().findViewById<DrawerLayout>(R.id.drawerLayout)?.closeDrawers()
        when (navController.currentDestination?.id ?: Int.MIN_VALUE) {
            R.id.mainFragment ->
                navController.navigate(
                    MainFragmentDirections.actionMainFragmentSelf(
                        installationId = installation.id,
                        installationUrl = installation.rawUrl
                    ))
            R.id.authFragment ->
                navController.navigate(
                    AuthFragmentDirections.actionAuthFragmentToMainFragment(
                        installationId = installation.id,
                        installationUrl = installation.rawUrl
                    ))
        }
    }

    fun updateInstallations(idToSelect: String?) {
        lifecycleScope.launch {
            viewModel.updateInstallationList {
                if (idToSelect != null) {
                    adapter.setSelectedItemById(idToSelect)
                }
            }
        }
    }

    interface InstallationListView {
        fun updateInstallations(idToSelect: String?)
    }
}
