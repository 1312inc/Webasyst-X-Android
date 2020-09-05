package com.webasyst.x.site.domainlist

import android.app.Application
import android.view.View
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MediatorLiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.webasyst.api.site.SiteApiClient
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class DomainListViewModel(
    app: Application,
    private val installationId: String?,
    private val installationUrl: String?
) : AndroidViewModel(app) {
    private val mutableState = MutableLiveData<Int>().apply { value = STATE_LOADING_DATA }

    val spinnerVisibility: LiveData<Int> = MediatorLiveData<Int>().apply {
        addSource(mutableState) { value = if (it == STATE_LOADING_DATA) View.VISIBLE else View.GONE }
    }
    val listVisibility: LiveData<Int> = MediatorLiveData<Int>().apply {
        addSource(mutableState) { value = if (it == STATE_DATA_READY) View.VISIBLE else View.GONE}
    }
    val errorVisibility: LiveData<Int> = MediatorLiveData<Int>().apply {
        addSource(mutableState) { value = if (it == STATE_ERROR) View.VISIBLE else View.GONE }
    }

    private val mutableDomainList = MutableLiveData<List<Domain>>()
    val domainList: LiveData<List<Domain>> = mutableDomainList

    init {
        viewModelScope.launch(Dispatchers.IO) {
            updateData()
        }
    }

    private suspend fun updateData() {
        if (installationId == null || installationUrl == null) {
            mutableState.postValue(STATE_ERROR)
            return
        }
        val siteApiClient = SiteApiClient.getInstance(getApplication())
        siteApiClient
            .domainGetList(installationUrl, installationId)
            .onSuccess {
                mutableState.postValue(STATE_DATA_READY)
                mutableDomainList.postValue(it.domains.map { domain -> Domain(domain) })
            }
            .onFailure {
                mutableState.postValue(STATE_ERROR)
            }
    }

    class Factory(
        private val application: Application,
        private val installationId: String?,
        private val installationUrl: String?
    ) : ViewModelProvider.Factory {
        override fun <T : ViewModel?> create(modelClass: Class<T>): T =
            DomainListViewModel(application, installationId, installationUrl) as T
    }

    companion object {
        const val STATE_LOADING_DATA = 1
        const val STATE_DATA_READY = 2
        const val STATE_ERROR = 3
    }
}
