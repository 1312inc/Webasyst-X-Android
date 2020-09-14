package com.webasyst.x.installations

import android.app.Application
import android.view.View
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MediatorLiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.webasyst.api.ApiClient
import com.webasyst.api.webasyst.WebasystApiClient
import com.webasyst.auth.WebasystAuthStateStore
import com.webasyst.x.R
import com.webasyst.x.cache.DataCache
import com.webasyst.x.main.MainFragmentDirections
import com.webasyst.x.util.findRootNavController
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.launch
import net.openid.appauth.AuthState

class InstallationListViewModel(app: Application) : AndroidViewModel(app), WebasystAuthStateStore.Observer {
    private val apiClient by lazy { ApiClient.getInstance(getApplication()) }
    private val webasystApiClient by lazy { WebasystApiClient.getInstance(getApplication()) }
    private val authStateStore = WebasystAuthStateStore.getInstance(getApplication())
    private val cache by lazy { DataCache.getInstance(getApplication()) }

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

    private fun updateInstallationList() {
        viewModelScope.launch(Dispatchers.IO) {
            apiClient.getInstallationList()
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

    private suspend fun updateInstallationInfos(installations: List<Installation>) {
        val data = installations.map { installation ->
            installation to viewModelScope.async(Dispatchers.IO) {
                webasystApiClient.getInstalationInfo(installation.url, installation.id)
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
