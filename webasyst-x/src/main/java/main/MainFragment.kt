package com.webasyst.x.main

import android.os.Bundle
import android.view.View
import androidx.annotation.IdRes
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.navArgs
import com.webasyst.x.R
import kotlinx.android.synthetic.main.frag_main.bottomNav

class MainFragment : Fragment(R.layout.frag_main) {
    private val args: MainFragmentArgs by navArgs()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        bottomNav.setOnNavigationItemSelectedListener { item ->
            when (item.itemId) {
                R.id.vini, R.id.vidi, R.id.vici -> {
                    onTabChange(item.itemId)
                    true
                }
                else -> false
            }
        }

        onTabChange(bottomNav.selectedItemId)
    }

    private fun onTabChange(@IdRes id: Int) {
        val fragment = ExampleFragment.newInstance(
            when (id) {
                R.id.vini -> "Vini"
                R.id.vidi -> "Vidi"
                R.id.vici -> "Vici"
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
