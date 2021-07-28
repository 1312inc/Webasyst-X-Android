package com.webasyst.x.installations

import android.app.Application
import android.view.View
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.asLiveData
import androidx.navigation.NavController
import com.webasyst.api.webasyst.WebasystApiClient
import com.webasyst.api.webasyst.WebasystApiClientFactory
import com.webasyst.auth.WebasystAuthStateStore
import com.webasyst.x.common.XComponentProvider
import com.webasyst.x.common.findRootNavController
import net.openid.appauth.AuthState

class InstallationListViewModel(app: Application) : AndroidViewModel(app), WebasystAuthStateStore.Observer {
    private val waidClient = (getApplication() as XComponentProvider).getWAIDClient()
    private val apiClient = (getApplication() as XComponentProvider).getApiClient()
    private val webasystApiClientFactory = (apiClient.getFactory(WebasystApiClient::class.java) as WebasystApiClientFactory)
    private val authStateStore = WebasystAuthStateStore.getInstance(getApplication())
    private val cache = (getApplication() as XComponentProvider).getInstallationListStore()
    var navController: NavController? = null

    private val installationsController = InstallationsController.instance(app as XComponentProvider)
    val installations = installationsController.installations.asLiveData()

    private val _state = MutableLiveData<Int>().apply { value = STATE_LOADING }
    val state: LiveData<Int>
        get() = _state

    init {
        installationsController.updateInstallations()
        if (installations.value?.isEmpty() == true) {
            _state.value = STATE_EMPTY
        } else {
            _state.value = STATE_READY
        }
    }

    init {
        authStateStore.addObserver(this)
    }

    override fun onCleared() {
        super.onCleared()
        authStateStore.removeObserver(this)
    }

    private var wasAuthorized: Boolean? = null
    override fun onAuthStateChange(state: AuthState) {
        if (state.isAuthorized != wasAuthorized) {
            if (wasAuthorized == true) {
                installationsController.updateInstallations()
            } else if (wasAuthorized == false) {
                installationsController.clearInstallations()
            }
        }
        wasAuthorized = state.isAuthorized
    }

    fun onAddWebasystClicked(view: View) {
        val navController = view.findRootNavController()
        if (navController.currentDestination?.id == R.id.mainFragment) {
            view.findRootNavController().navigate(
                R.id.action_mainFragment_to_addWebasystFragment
            )
        }
    }

    companion object {
        const val TAG = "frag_loading"
        const val STATE_LOADING = 0
        const val STATE_EMPTY = 1
        const val STATE_READY = 2
    }
}
