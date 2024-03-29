package com.webasyst.x

import android.content.Intent
import android.graphics.drawable.Drawable
import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.core.graphics.drawable.toBitmap
import androidx.databinding.DataBindingUtil
import androidx.drawerlayout.widget.DrawerLayout
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.findNavController
import androidx.navigation.fragment.FragmentNavigator
import com.bumptech.glide.Glide
import com.bumptech.glide.request.target.CustomTarget
import com.bumptech.glide.request.transition.Transition
import com.webasyst.auth.WebasystAuthActivity
import com.webasyst.auth.WebasystAuthHelper
import com.webasyst.auth.WebasystAuthStateStore
import com.webasyst.x.auth.AuthViewModel
import com.webasyst.x.common.UserInfoNavigator
import com.webasyst.x.databinding.ActivityMainBinding
import com.webasyst.x.installations.Installation
import com.webasyst.x.installations.InstallationIconDrawable
import com.webasyst.x.installations.InstallationListFragment
import com.webasyst.x.installations.InstallationsController
import com.webasyst.x.intro.IntroActivity
import com.webasyst.x.pin_code.PinCodeStore
import com.webasyst.x.util.BackPressHandler
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import net.openid.appauth.AuthState
import net.openid.appauth.AuthorizationException
import org.koin.android.ext.android.inject
import java.lang.ref.WeakReference

class MainActivity : WebasystAuthActivity(),
    WebasystAuthStateStore.Observer,
    InstallationListFragment.InstallationListView,
    UserInfoNavigator
{
    lateinit var binding: ActivityMainBinding
    private val viewModel by lazy {
        ViewModelProvider(this).get(MainActivityViewModel::class.java)
    }
    private val stateStore by lazy(LazyThreadSafetyMode.NONE) {
        WebasystAuthStateStore.getInstance(this)
    }
    private val navController by lazy(LazyThreadSafetyMode.NONE) { binding.navRoot.findNavController() }
    private val installationsController by lazy(LazyThreadSafetyMode.NONE) {
        InstallationsController.instance(WebasystXApplication.instance)
    }
    val toolbar by lazy(LazyThreadSafetyMode.NONE) { binding.toolbar }

    var previousInstallation: Installation? = null

    private val pinCodeStore: PinCodeStore by inject()
    private var isStartHappenedPin = true


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

        binding = DataBindingUtil.setContentView(
            this,
            R.layout.activity_main
        )
        binding.lifecycleOwner = this

        setSupportActionBar(binding.toolbar)

        binding.toolbar.setNavigationOnClickListener {
            if (!handleBackButton()) {
                val navController = binding.navRoot.findNavController()
                when (binding.navRoot.findNavController().currentDestination?.id ?: Int.MIN_VALUE) {
                    R.id.mainFragment,
                    R.id.noInstallationsFragment -> binding.drawerLayout.openDrawer(binding.navigation)
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
                    installationsController.setSelectedInstallation(null)
                    val intent = Intent(this, IntroActivity::class.java)
                    startActivity(intent)
                    finish()
                }
            }
            wasAuthorized = state.isAuthorized
        }

        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                installationsController.installations.collect {
                    if (it == null) {
                        Log.d(TAG, "Navigating to LoadingFragment")
                        navController.navigate(NavDirections.actionGlobalLoadingFragment())
                    } else if (it.isEmpty()) {
                        Log.d(TAG, "Navigating to NoInstallationsFragment")
                        navController.navigate(NavDirections.actionGlobalNoInstallationsFragment())
                    }
                }
            }
        }

        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                installationsController.currentInstallation.collect {
                    if (it != null
                        && previousInstallation?.id != it.id
                        && navController.currentDestination?.id != R.id.pinCodeFragment
                    ) {
                        onInstallationChange(it)
                    }
                }
            }
        }


    }

    override fun onStart() {
        super.onStart()
        findNavController(R.id.navRoot)
            .addOnDestinationChangedListener { _, destination, _ ->
                runOnUiThread {
                    when (destination.id) {
                        R.id.mainFragment -> {
                            binding.toolbar.visibility = View.VISIBLE
                            binding.drawerLayout.setDrawerLockMode(DrawerLayout.LOCK_MODE_UNLOCKED)
                        }
                        R.id.addWebasystFragment -> {
                            binding.toolbar.visibility = View.GONE
                            binding.drawerLayout.setDrawerLockMode(DrawerLayout.LOCK_MODE_LOCKED_CLOSED)
                            binding.toolbar.setTitle(R.string.add_webasyst)
                        }
                        R.id.noInstallationsFragment -> {
                            binding.toolbar.visibility = View.VISIBLE
                            binding.drawerLayout.setDrawerLockMode(DrawerLayout.LOCK_MODE_UNLOCKED)
                            binding.toolbar.setTitle(R.string.app_name)
                            lifecycleScope.launch {
                                val it = installationsController.currentInstallation.first()
                                if (it != null && previousInstallation?.id != it.id)
                                    onInstallationChange(it)
                            }
                        }
                        R.id.profileEditorFragment, R.id.pinCodeFragment -> {
                            binding.toolbar.visibility = View.GONE
                            binding.drawerLayout.setDrawerLockMode(DrawerLayout.LOCK_MODE_LOCKED_CLOSED)
                        }
                    }
                }
            }
    }

    private fun onInstallationChange(it: Installation){
        Log.d(TAG, "Navigating to ${it.domain}")
        navController.navigate(
            NavDirections.actionGlobalMainFragment(installation = it)
        )
        binding.drawerLayout.closeDrawers()

        updateToolbarIcon(it.icon)
        previousInstallation = it
    }

    override fun onResume() {
        super.onResume()
        stateStore.addObserver(this)

        if ((pinCodeStore.hasPinCodeWithTime()
                && navController.currentDestination?.id != R.id.pinCodeFragment) ||
            (pinCodeStore.hasPinCode() && isStartHappenedPin)
        ) {
            navController.navigate(NavDirections.actionGlobalPinCodeFragment())
        }
        isStartHappenedPin = false
    }

    override fun onPause() {
        super.onPause()
        stateStore.removeObserver(this)
    }

    override fun onStop() {
        super.onStop()
        pinCodeStore.setLastEnterTime()
    }

    override fun onAuthStateChange(state: AuthState) {
        viewModel.setAuthState(state)
    }

    @Deprecated("Deprecated in Java")
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
                    binding.toolbar.navigationIcon = resource
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

    override fun openProfileEditor() {
        if (navController.currentDestination?.id != R.id.profileEditorFragment)
            navController.navigate(
                NavDirections.actionGlobalProfileEditorFragment(),
                FragmentNavigator
                    .Extras
                    .Builder()
                    .build()
            )
    }

    override fun goToPinCode(forRemove: Boolean) {
        navController.navigate(
            NavDirections.actionGlobalPinCodeFragment(
                forRemove
            )
        )
    }

    override fun popBackStack() {
        navController.popBackStack()
    }

    companion object {
        const val TAG = "MainActivity"
    }
}
