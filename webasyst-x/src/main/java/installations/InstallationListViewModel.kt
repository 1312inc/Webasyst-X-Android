package com.webasyst.x.installations

import android.app.Application
import android.view.View
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MediatorLiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.webasyst.x.api.ApiClient
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class InstallationListViewModel(app: Application) : AndroidViewModel(app) {
    private val apiClient by lazy { ApiClient.getInstance(getApplication()) }

    private val mutableInstallations = MutableLiveData<List<Installation>>()
    val installations: LiveData<List<Installation>> = mutableInstallations

    private val mutableLoadingData = MutableLiveData<Boolean>().apply { value = true }
    val spinnerVisibility = MediatorLiveData<Int>().apply {
        addSource(mutableLoadingData) { value = if (it) View.VISIBLE else View.GONE }
    }
    val emptyPlaceholderVisibility = MediatorLiveData<Int>().apply {
        fun get(): Int =
            if (mutableLoadingData.value == false && installations.value?.isNotEmpty() == true) {
                View.GONE
            } else {
                View.VISIBLE
            }
        addSource(mutableLoadingData) { value = if (it) View.GONE else get() }
        addSource(installations) { value = if (it.isNotEmpty()) View.VISIBLE else get() }
    }

    init {
        viewModelScope.launch(Dispatchers.IO) {
            apiClient.installationList()
                .onSuccess {
                    mutableInstallations.postValue(it)
                }
                .onFailure {
                    // TODO
                }
            mutableLoadingData.postValue(false)
        }
    }
}
