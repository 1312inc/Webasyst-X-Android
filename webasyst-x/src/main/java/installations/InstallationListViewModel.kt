package com.webasyst.x.installations

import android.app.Application
import android.view.View
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MediatorLiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.webasyst.auth.WebasystAuthStateStore
import com.webasyst.x.R
import com.webasyst.x.api.ApiClient
import com.webasyst.x.cache.DataCache
import com.webasyst.x.main.MainFragmentDirections
import com.webasyst.x.util.findRootNavController
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import net.openid.appauth.AuthState

class InstallationListViewModel(app: Application) : AndroidViewModel(app), WebasystAuthStateStore.Observer {
    private val apiClient by lazy { ApiClient.getInstance(getApplication()) }
    private val authStateStore = WebasystAuthStateStore.getInstance(getApplication())
    private val cache by lazy { DataCache.getInstance(getApplication()) }

    private val mutableInstallations = MutableLiveData<List<Installation>>().apply {
        cache.readInstallationList()?.let { value = it.mapToVM() }
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
                .onSuccess {
                    cache.storeInstallationList(it)
                    mutableInstallations.postValue(it.mapToVM())
                }
                .onFailure {
                    // TODO
                }
            mutableLoadingData.postValue(false)
        }
    }

    private fun List<com.webasyst.x.api.Installation>.mapToVM() = this.map { Installation(it) }

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
