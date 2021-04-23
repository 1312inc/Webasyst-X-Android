package com.webasyst.x.installations

import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Drawable
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.widget.Toolbar
import androidx.core.graphics.drawable.toBitmap
import androidx.databinding.DataBindingUtil
import androidx.drawerlayout.widget.DrawerLayout
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.observe
import androidx.recyclerview.widget.LinearLayoutManager
import com.bumptech.glide.Glide
import com.bumptech.glide.request.target.CustomTarget
import com.bumptech.glide.request.transition.Transition
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
        requireActivity().also { activity ->
            activity.findViewById<DrawerLayout>(R.id.drawerLayout)?.closeDrawers()
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

            activity.findViewById<Toolbar>(R.id.toolbar)?.let { toolbar ->
                val density = activity.resources.displayMetrics.density
                if (installation.icon is Installation.Icon.ImageIcon) {
                    Glide.with(this)
                        .load(installation.icon.getThumb((24 * density).toInt()))
                        .circleCrop()
                        .into(object : CustomTarget<Drawable>() {
                            override fun onResourceReady(
                                resource: Drawable,
                                transition: Transition<in Drawable>?
                            ) {
                                toolbar.navigationIcon = resource
                            }

                            override fun onLoadCleared(placeholder: Drawable?) = Unit
                        })
                } else {
                    toolbar.navigationIcon = BitmapDrawable(
                        activity.resources,
                        InstallationIconDrawable(activity, installation.icon).let { drawable ->
                            drawable.toBitmap((24 * density).toInt(), (24 * density).toInt())
                        })
                }
            }
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
