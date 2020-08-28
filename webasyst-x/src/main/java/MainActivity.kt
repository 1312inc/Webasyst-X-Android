package com.webasyst.x

import android.os.Bundle
import androidx.databinding.DataBindingUtil
import androidx.drawerlayout.widget.DrawerLayout
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.findNavController
import com.webasyst.auth.WebasystAuthActivity
import com.webasyst.auth.WebasystAuthStateStore
import com.webasyst.x.databinding.ActivityMainBinding
import com.webasyst.x.databinding.NavHeaderAuthorizedBinding
import net.openid.appauth.AuthState

class MainActivity : WebasystAuthActivity(), WebasystAuthStateStore.Observer {
    private val viewModel by lazy {
        ViewModelProvider(this).get(MainActivityViewModel::class.java)
    }
    private val stateStore by lazy(LazyThreadSafetyMode.NONE) {
        WebasystAuthStateStore.getInstance(this)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val binding = DataBindingUtil.setContentView<ActivityMainBinding>(this, R.layout.activity_main)
        binding.lifecycleOwner = this

        val headerBinding = NavHeaderAuthorizedBinding.bind(binding.navigation.getHeaderView(0))
        headerBinding.lifecycleOwner = this
        headerBinding.viewModel = viewModel
        headerBinding.signOutButton.setOnClickListener { waSignOut() }

        viewModel.authState.observe(this, { state ->
            val navController = findNavController(R.id.navRoot)
            if (state.isAuthorized) {
                binding.drawerLayout.setDrawerLockMode(DrawerLayout.LOCK_MODE_UNLOCKED)

                if (navController.currentDestination?.id == R.id.authFragment) {
                    navController.navigate(R.id.action_authFragment_to_mainFragment)
                }
            } else {
                binding.drawerLayout.setDrawerLockMode(DrawerLayout.LOCK_MODE_LOCKED_CLOSED)

                if (navController.currentDestination?.id != R.id.authFragment) {
                    navController.setGraph(R.navigation.nav_graph)
                }
            }
        })
    }

    override fun onResume() {
        super.onResume()
        stateStore.addObserver(this)
    }

    override fun onPause() {
        super.onPause()
        stateStore.removeObserver(this)
    }

    override fun onAuthStateChange(state: AuthState) {
        viewModel.setAuthState(state)
    }
}
