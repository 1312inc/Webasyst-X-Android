package com.webasyst.x

import android.os.Bundle
import androidx.databinding.DataBindingUtil
import androidx.drawerlayout.widget.DrawerLayout
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.observe
import androidx.navigation.findNavController
import com.webasyst.auth.WebasystAuthActivity
import com.webasyst.auth.WebasystAuthStateStore
import com.webasyst.x.auth.AuthFragmentDirections
import com.webasyst.x.databinding.ActivityMainBinding
import kotlinx.android.synthetic.main.activity_main.drawerLayout
import kotlinx.android.synthetic.main.activity_main.navRoot
import kotlinx.android.synthetic.main.activity_main.toolbar
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

        setSupportActionBar(binding.toolbar)

        binding.toolbar.setNavigationOnClickListener {
            val navController = navRoot.findNavController()
            when (navRoot.findNavController().currentDestination?.id ?: Int.MIN_VALUE) {
                R.id.mainFragment -> drawerLayout.openDrawer(binding.navigation)
                R.id.addWebasystFragment -> navController.popBackStack()
            }
        }

        viewModel.authState.observe(this) { state ->
            val navController = findNavController(R.id.navRoot)
            if (state.isAuthorized) {
                binding.drawerLayout.setDrawerLockMode(DrawerLayout.LOCK_MODE_UNLOCKED)

                if (navController.currentDestination?.id == R.id.authFragment) {
                    navController.navigate(AuthFragmentDirections.actionAuthFragmentToMainFragment(
                        installationId = null,
                        installationUrl = null
                    ))
                }
            } else {
                binding.drawerLayout.setDrawerLockMode(DrawerLayout.LOCK_MODE_LOCKED_CLOSED)

                if (navController.currentDestination?.id != R.id.authFragment) {
                    navController.setGraph(R.navigation.nav_graph)
                }
            }
        }
    }

    override fun onStart() {
        super.onStart()

        findNavController(R.id.navRoot)
            .addOnDestinationChangedListener { _, destination, _ ->
                when (destination.id) {
                    R.id.authFragment -> {
                        toolbar.navigationIcon = null
                        toolbar.setTitle(R.string.app_name)
                    }
                    R.id.mainFragment -> {
                        drawerLayout.setDrawerLockMode(DrawerLayout.LOCK_MODE_UNLOCKED)
                        toolbar.setNavigationIcon(R.drawable.ic_hamburger)
                    }
                    R.id.addWebasystFragment -> {
                        drawerLayout.setDrawerLockMode(DrawerLayout.LOCK_MODE_LOCKED_CLOSED)
                        toolbar.setTitle(R.string.add_webasyst)
                        toolbar.setNavigationIcon(R.drawable.ic_arrow_back)
                    }
                }
            }
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
