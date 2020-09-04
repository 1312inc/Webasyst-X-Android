package com.webasyst.x

import android.os.Bundle
import androidx.databinding.DataBindingUtil
import androidx.drawerlayout.widget.DrawerLayout
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.observe
import androidx.navigation.findNavController
import com.webasyst.auth.WebasystAuthActivity
import com.webasyst.auth.WebasystAuthStateStore
import com.webasyst.x.api.ApiClient
import com.webasyst.x.databinding.ActivityMainBinding
import com.webasyst.x.databinding.NavHeaderAuthorizedBinding
import com.webasyst.x.util.USERPIC_FILE
import com.webasyst.x.util.decodeBitmap
import com.webasyst.x.util.getCacheFile
import kotlinx.android.synthetic.main.activity_main.drawerLayout
import kotlinx.android.synthetic.main.activity_main.navRoot
import kotlinx.android.synthetic.main.activity_main.toolbar
import kotlinx.android.synthetic.main.nav_header_authorized.userpicView
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import net.openid.appauth.AuthState
import java.io.File

class MainActivity : WebasystAuthActivity(), WebasystAuthStateStore.Observer {
    private val apiClient by lazy(LazyThreadSafetyMode.NONE) { ApiClient.getInstance(this) }

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
                    navController.navigate(R.id.action_authFragment_to_mainFragment)
                }
            } else {
                binding.drawerLayout.setDrawerLockMode(DrawerLayout.LOCK_MODE_LOCKED_CLOSED)

                if (navController.currentDestination?.id != R.id.authFragment) {
                    navController.setGraph(R.navigation.nav_graph)
                }
            }
        }

        viewModel.userpicUrl.observe(this) { url ->
            if (url.isEmpty()) {
                userpicView.setImageResource(R.drawable.ic_userpic_placeholder)
            } else {
                lifecycleScope.launch(Dispatchers.Main) {
                    val userpicFile = this@MainActivity.getCacheFile(USERPIC_FILE)
                    try {
                        if (!userpicFile.exists() ||
                            userpicFile.lastModified() + MAX_USERPIC_AGE < System.currentTimeMillis()
                        ) {
                            apiClient.downloadUserpic(url, userpicFile)
                        }
                        updateUserpicFromFile(userpicFile)
                    } catch (e: Throwable) {
                        updateUserpicFromFile(userpicFile)
                    }
                }
            }
        }
    }

    private suspend fun updateUserpicFromFile(userpicFile: File) {
        if (userpicFile.exists()) {
            userpicView.setImageBitmap(withContext(Dispatchers.Default) {
                userpicFile.decodeBitmap(userpicView)
            })
        } else {
            userpicView.setImageResource(R.drawable.ic_userpic_placeholder)
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

    companion object {
        private const val MAX_USERPIC_AGE = 1000 * 60 * 60 * 2 // 2 hours
    }
}
