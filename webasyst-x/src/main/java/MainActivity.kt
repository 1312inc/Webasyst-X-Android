package com.webasyst.x

import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.databinding.DataBindingUtil
import androidx.drawerlayout.widget.DrawerLayout
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.observe
import androidx.navigation.findNavController
import com.webasyst.auth.WebasystAuthActivity
import com.webasyst.auth.WebasystAuthHelper
import com.webasyst.auth.WebasystAuthStateStore
import com.webasyst.x.auth.AuthFragmentDirections
import com.webasyst.x.auth.AuthViewModel
import com.webasyst.x.databinding.ActivityMainBinding
import com.webasyst.x.installations.InstallationListFragment
import com.webasyst.x.intro.IntroActivity
import com.webasyst.x.util.BackPressHandler
import kotlinx.android.synthetic.main.activity_main.drawerLayout
import kotlinx.android.synthetic.main.activity_main.navRoot
import kotlinx.android.synthetic.main.activity_main.toolbar
import net.openid.appauth.AuthState
import net.openid.appauth.AuthorizationException
import java.lang.ref.WeakReference

class MainActivity : WebasystAuthActivity(), WebasystAuthStateStore.Observer, InstallationListFragment.InstallationListView {
    private val viewModel by lazy {
        ViewModelProvider(this).get(MainActivityViewModel::class.java)
    }
    private val stateStore by lazy(LazyThreadSafetyMode.NONE) {
        WebasystAuthStateStore.getInstance(this)
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)

        val navController = navRoot.findNavController()
        if (intent.action == WebasystAuthHelper.ACTION_UPDATE_AFTER_AUTHORIZATION) {
            val exception = AuthorizationException.fromIntent(intent)
            if (exception == null) {
                navController.setGraph(R.navigation.nav_graph, Bundle().apply {
                    putInt("state", AuthViewModel.STATE_AUTHENTICATING)
                })
            } else {
                navController.setGraph(R.navigation.nav_graph, Bundle().apply {
                    putInt("state", AuthViewModel.STATE_IDLE)
                })
            }
        } else if (intent.action == WebasystAuthHelper.ACTION_AFTER_AUTHORIZATION_CANCELLED) {
            navController.setGraph(R.navigation.nav_graph, Bundle().apply {
                putInt("state", AuthViewModel.STATE_IDLE)
            })
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val binding = DataBindingUtil.setContentView<ActivityMainBinding>(
            this,
            R.layout.activity_main
        )
        binding.lifecycleOwner = this

        setSupportActionBar(binding.toolbar)

        binding.toolbar.setNavigationOnClickListener {
            if (!handleBackButton()) {
                val navController = navRoot.findNavController()
                when (navRoot.findNavController().currentDestination?.id ?: Int.MIN_VALUE) {
                    R.id.mainFragment -> drawerLayout.openDrawer(binding.navigation)
                    R.id.addWebasystFragment -> navController.popBackStack()
                }
            }
        }

        viewModel.authState.observe(this) { state ->
            val navController = findNavController(R.id.navRoot)
            if (state.isAuthorized) {
                binding.drawerLayout.setDrawerLockMode(DrawerLayout.LOCK_MODE_UNLOCKED)

                if (navController.currentDestination?.id == R.id.authFragment) {
                    navController.navigate(
                        AuthFragmentDirections.actionAuthFragmentToMainFragment(
                            installationId = null,
                            installationUrl = null
                        )
                    )
                }
            } else {
                val intent = Intent(this, IntroActivity::class.java)
                startActivity(intent)
                finish()
            }
        }
    }

    override fun onStart() {
        super.onStart()

        findNavController(R.id.navRoot)
            .addOnDestinationChangedListener { _, destination, _ ->
                runOnUiThread {
                    when (destination.id) {
                        R.id.authFragment -> {
                            toolbar.visibility = View.GONE
                            toolbar.setTitle(R.string.app_name)
                        }
                        R.id.mainFragment -> {
                            toolbar.visibility = View.VISIBLE
                            drawerLayout.setDrawerLockMode(DrawerLayout.LOCK_MODE_UNLOCKED)
                        }
                        R.id.addWebasystFragment -> {
                            toolbar.visibility = View.VISIBLE
                            drawerLayout.setDrawerLockMode(DrawerLayout.LOCK_MODE_LOCKED_CLOSED)
                            toolbar.setTitle(R.string.add_webasyst)
                        }
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

    override fun onBackPressed() {
        if (!handleBackButton()) {
            super.onBackPressed()
        }
    }

    /**
     * Tries to handle back button click with [backPressHandlers].
     * Returns true if click was handled, false otherwise.
     */
    private fun handleBackButton(): Boolean {
        val handlersIterator = backPressHandlers.listIterator()
        var handled = false
        while (handlersIterator.hasNext()) {
            val handler = handlersIterator.next().get()
            if (handler == null) {
                handlersIterator.remove()
            } else if (!handled) {
                handled = handler.onBackPressed()
            }
        }
        return handled
    }

    private val backPressHandlers = mutableListOf<WeakReference<BackPressHandler>>()
    fun addBackPressHandler(handler: BackPressHandler) {
        backPressHandlers.add(WeakReference(handler))
    }
    fun removeBackPresshandler(handler: BackPressHandler) {
        val i = backPressHandlers.listIterator()
        while (i.hasNext()) {
            val handlerRef = i.next()
            if (handlerRef.get() == null || handlerRef.get() == handler) {
                i.remove()
            }
        }
    }

    override fun updateInstallations(idToSelect: String?) {
        (supportFragmentManager.findFragmentById(R.id.installationList) as InstallationListFragment)
            .updateInstallations(idToSelect)
    }
}
