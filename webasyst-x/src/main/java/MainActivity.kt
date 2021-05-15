package com.webasyst.x

import android.content.Intent
import android.graphics.drawable.Drawable
import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.core.graphics.drawable.toBitmap
import androidx.databinding.DataBindingUtil
import androidx.drawerlayout.widget.DrawerLayout
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.navigation.findNavController
import com.bumptech.glide.Glide
import com.bumptech.glide.request.target.CustomTarget
import com.bumptech.glide.request.transition.Transition
import com.webasyst.auth.WebasystAuthActivity
import com.webasyst.auth.WebasystAuthHelper
import com.webasyst.auth.WebasystAuthStateStore
import com.webasyst.x.auth.AuthViewModel
import com.webasyst.x.databinding.ActivityMainBinding
import com.webasyst.x.installations.Installation
import com.webasyst.x.installations.InstallationIconDrawable
import com.webasyst.x.installations.InstallationListFragment
import com.webasyst.x.installations.InstallationsController
import com.webasyst.x.intro.IntroActivity
import com.webasyst.x.util.BackPressHandler
import kotlinx.android.synthetic.main.activity_main.drawerLayout
import kotlinx.android.synthetic.main.activity_main.navRoot
import kotlinx.android.synthetic.main.activity_main.toolbar
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
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
    private val navController by lazy(LazyThreadSafetyMode.NONE) { navRoot.findNavController() }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)

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
                    R.id.mainFragment,
                    R.id.noInstallationsFragment -> drawerLayout.openDrawer(binding.navigation)
                    R.id.addWebasystFragment -> navController.popBackStack()
                }
            }
        }

        var wasAuthorized: Boolean? = null
        viewModel.authState.observe(this) { state ->
            if (wasAuthorized != state.isAuthorized) {
                if (state.isAuthorized) {
                    binding.drawerLayout.setDrawerLockMode(DrawerLayout.LOCK_MODE_UNLOCKED)
                } else {
                    InstallationsController.setSelectedInstallation(null)
                    val intent = Intent(this, IntroActivity::class.java)
                    startActivity(intent)
                    finish()
                }
            }
            wasAuthorized = state.isAuthorized
        }
    }

    override fun onStart() {
        super.onStart()

        lifecycleScope.launch {
            InstallationsController.installations.collect {
                if (it == null) {
                    Log.d(TAG, "Navigating to LoadingFragment")
                    navController.navigate(NavDirections.actionGlobalLoadingFragment())
                } else if (it.isEmpty()) {
                    Log.d(TAG, "Navigating to NoInstallationsFragment")
                    navController.navigate(NavDirections.actionGlobalNoInstallationsFragment())
                }
            }
        }

        lifecycleScope.launch {
            var previousInstallation: Installation? = null
            InstallationsController.currentInstallation.collect {
                if (it != null && previousInstallation?.id != it.id) {
                    Log.d(TAG, "Navigating to ${it.domain}")
                    navController.navigate(NavDirections.actionGlobalMainFragment(installation = it))
                    drawerLayout.closeDrawers()
                }
                it?.icon?.let { icon -> updateToolbarIcon(icon) }
                previousInstallation = it
            }
        }

        findNavController(R.id.navRoot)
            .addOnDestinationChangedListener { _, destination, _ ->
                runOnUiThread {
                    when (destination.id) {
                        R.id.mainFragment -> {
                            toolbar.visibility = View.VISIBLE
                            drawerLayout.setDrawerLockMode(DrawerLayout.LOCK_MODE_UNLOCKED)
                        }
                        R.id.addWebasystFragment -> {
                            toolbar.visibility = View.VISIBLE
                            drawerLayout.setDrawerLockMode(DrawerLayout.LOCK_MODE_LOCKED_CLOSED)
                            toolbar.setTitle(R.string.add_webasyst)
                        }
                        R.id.noInstallationsFragment -> {
                            toolbar.visibility = View.VISIBLE
                            drawerLayout.setDrawerLockMode(DrawerLayout.LOCK_MODE_UNLOCKED)
                            toolbar.setTitle(R.string.app_name)
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

    private fun updateToolbarIcon(icon: Installation.Icon) {
        val density = resources.displayMetrics.density
        val res = 32
        Glide.with(this)
            .let { glide ->
                if (icon is Installation.Icon.ImageIcon) {
                    glide.load(icon.getThumb((res * density).toInt()))
                } else {
                    glide.load(InstallationIconDrawable(this, icon).let { drawable ->
                        drawable.toBitmap((res * density).toInt(), (res * density).toInt())
                    })
                }
            }
            .circleCrop()
            .into(object : CustomTarget<Drawable>((res * density).toInt(), (res * density).toInt()) {
                override fun onResourceReady(
                    resource: Drawable,
                    transition: Transition<in Drawable>?
                ) {
                    toolbar.navigationIcon = resource
                }

                override fun onLoadCleared(placeholder: Drawable?) = Unit
            })
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

    companion object {
        const val TAG = "MainActivity"
    }
}
