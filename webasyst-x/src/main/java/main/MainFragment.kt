package com.webasyst.x.main

import android.os.Bundle
import android.view.View
import androidx.annotation.IdRes
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.navArgs
import com.webasyst.x.R
import com.webasyst.x.site.domainlist.DomainListFragment
import kotlinx.android.synthetic.main.activity_main.toolbar
import kotlinx.android.synthetic.main.frag_main.bottomNav

class MainFragment : Fragment(R.layout.frag_main) {
    private val args: MainFragmentArgs by navArgs()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        bottomNav.setOnNavigationItemSelectedListener { item ->
            when (item.itemId) {
                R.id.sites -> {
                    onSitesTabSelected()
                    true
                }
                R.id.x, R.id.y -> {
                    onTabChange(item.itemId)
                    true
                }
                else -> false
            }
        }

        onTabChange(bottomNav.selectedItemId)
    }

    private fun onSitesTabSelected() {
        val fragment = DomainListFragment::class.java.newInstance()
        fragment.arguments = Bundle().apply {
            putString(DomainListFragment.INSTALLATION_ID, args.installationId)
            putString(DomainListFragment.INSTALLATION_URL, args.installationUrl)
        }
        loadFragment(fragment)
        requireActivity().toolbar.setTitle(R.string.domain_list)
    }

    private fun onTabChange(@IdRes id: Int) {
        val fragment = ExampleFragment.newInstance(
            when (id) {
                R.id.x -> "Hello X!"
                R.id.y -> "Hello Y!"
                else -> ""
            }
        )
        loadFragment(fragment)
        requireActivity().toolbar.title = bottomNav.menu.findItem(id).title
    }

    private fun loadFragment(fragment: Fragment) {
        childFragmentManager
            .beginTransaction()
            .replace(R.id.tabContent, fragment)
            .commit()
    }
}
