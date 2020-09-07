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
import java.lang.IllegalArgumentException

class MainFragment : Fragment(R.layout.frag_main) {
    private val args: MainFragmentArgs by navArgs()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        bottomNav.setOnNavigationItemSelectedListener { item ->
            when (item.itemId) {
                R.id.domains, R.id.x, R.id.y -> {
                    onTabChange(item.itemId)
                    true
                }
                else -> false
            }
        }

        onTabChange(bottomNav.selectedItemId)
    }

    private fun initDomainsFragment(): Fragment =
        DomainListFragment::class.java.newInstance().apply {
            arguments = Bundle().apply {
                putString(DomainListFragment.INSTALLATION_ID, args.installationId)
                putString(DomainListFragment.INSTALLATION_URL, args.installationUrl)

            }
        }

    private fun onTabChange(@IdRes id: Int) {
        val fragment = when(id) {
            R.id.domains -> initDomainsFragment()
            R.id.x -> ExampleFragment.newInstance("Hello X!")
            R.id.y -> ExampleFragment.newInstance("Hello Y!")
            else -> throw IllegalArgumentException("Tab not found")
        }
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
