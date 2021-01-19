package com.webasyst.x.installations

import android.app.Application
import android.view.View
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MediatorLiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import androidx.navigation.NavController
import com.webasyst.api.webasyst.WebasystApiClient
import com.webasyst.api.webasyst.WebasystApiClientFactory
import com.webasyst.auth.WebasystAuthStateStore
import com.webasyst.x.R
import com.webasyst.x.WebasystXApplication
import com.webasyst.x.auth.AuthFragmentDirections
import com.webasyst.x.main.MainFragmentDirections
import com.webasyst.x.util.findRootNavController
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.launch
import net.openid.appauth.AuthState

class InstallationListViewModel(app: Application) : AndroidViewModel(app), WebasystAuthStateStore.Observer {
    private val waidClient = getApplication<WebasystXApplication>().waidClient
    private val apiClient = getApplication<WebasystXApplication>().apiClient
    private val webasystApiClientFactory = (apiClient.getFactory(WebasystApiClient::class.java) as WebasystApiClientFactory)
    private val authStateStore = WebasystAuthStateStore.getInstance(getApplication())
    private val cache = getApplication<WebasystXApplication>().dataCache
    var navController: NavController? = null

    private val mutableInstallations = MutableLiveData<List<Installation>>().apply {
        cache.readInstallationList()?.let { value = it }
    }
    val installations: LiveData<List<Installation>> = mutableInstallations

    private val mutableLoadingData = MutableLiveData<Boolean>().apply { value = true }
    val spinnerVisibility = MediatorLiveData<Int>().apply {
        addSource(mutableLoadingData) { value = if (it) View.VISIBLE else View.GONE }
    }

    init {
        updateInstallationList()
    }

    fun updateInstallationList(callback: Runnable? = null) {
        viewModelScope.launch(Dispatchers.IO) {
            waidClient.getInstallationList()
                .onSuccess { installations ->
                    viewModelScope.launch(Dispatchers.IO) {
                        updateInstallationInfos(installations)
                    }
                }
                .onFailure {
                    // TODO
                }
            mutableLoadingData.postValue(false)
        }
    }

    private suspend fun updateInstallationInfos(apiInstallations: List<com.webasyst.waid.Installation>) {
        val installations = apiInstallations.map { Installation(it) }
        val data = installations.map { installation ->
            installation to viewModelScope.async(Dispatchers.IO) {
                webasystApiClientFactory
                    .instanceForInstallation(installation)
                    .getInstallationInfo()
            }
        }.toMap()
        data.values.awaitAll()
        val namedInstallations = data.map { (installation, info) ->
            if (info.await().isSuccess()) {
                installation.copy(domain = info.await().getSuccess().name)
            } else {
                installation
            }
        }
        cache.storeInstallationList(namedInstallations)
        mutableInstallations.postValue(namedInstallations)

        if (namedInstallations.isEmpty()) {
            navController?.let { navController ->
                when (navController.currentDestination?.id ?: Int.MIN_VALUE) {
                    R.id.mainFragment ->
                        navController.navigate(
                            MainFragmentDirections.actionMainFragmentSelf(
                                showAddWA = true,
                                installationId = null,
                                installationUrl = null
                            )
                        )
                    R.id.authFragment ->
                        navController.navigate(
                            AuthFragmentDirections.actionAuthFragmentToMainFragment(
                                showAddWA = true,
                                installationId = null,
                                installationUrl = null
                            )
                        )
                }
            }
        }
    }

    init {
        authStateStore.addObserver(this)
    }

    override fun onCleared() {
        super.onCleared()
        authStateStore.removeObserver(this)
    }

    override fun onAuthStateChange(state: AuthState?) {
        if (state?.isAuthorized == true) updateInstallationList()

        if (state?.isAuthorized == false) {
            cache.clearInstallationList()
            mutableInstallations.value = emptyList()
        }
    }

    fun onAddWebasystClicked(view: View) {
        val navController = view.findRootNavController()
        if (navController.currentDestination?.id == R.id.mainFragment) {
            view.findRootNavController().navigate(
                MainFragmentDirections.actionMainFragmentToAddWebasystFragment()
            )
        }
    }
}
