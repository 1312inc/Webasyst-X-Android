package com.webasyst.x.site.domainlist

import android.app.Application
import android.content.Context
import android.view.View
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MediatorLiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.webasyst.api.Installation
import com.webasyst.api.site.SiteApiClient
import com.webasyst.api.site.SiteApiClientFactory
import com.webasyst.x.R
import com.webasyst.x.WebasystXApplication
import com.webasyst.x.util.ConnectivityUtil
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch

class DomainListViewModel(
    app: Application,
    private val installationId: String?,
    private val installationUrl: String?
) : AndroidViewModel(app) {
    init {
        val connectivityUtil = ConnectivityUtil(getApplication())
        viewModelScope.launch(Dispatchers.Default) {
            connectivityUtil.connectivityFlow()
                .collect {
                    if (it == ConnectivityUtil.ONLINE) {
                        updateData(getApplication())
                    }
                }
        }
    }

    val appName = getApplication<Application>().getString(R.string.app_site)
    val apiName = "site.domain.getList"

    private val mutableState = MutableLiveData<Int>().apply { value = STATE_UNKNOWN }
    val state: LiveData<Int> = mutableState

    val spinnerVisibility: LiveData<Int> = MediatorLiveData<Int>().apply {
        addSource(mutableState) { value = if (it == STATE_LOADING_DATA) View.VISIBLE else View.GONE }
    }
    val listVisibility: LiveData<Int> = MediatorLiveData<Int>().apply {
        addSource(mutableState) { value = if (it == STATE_DATA_READY) View.VISIBLE else View.GONE}
    }
    val errorVisibility: LiveData<Int> = MediatorLiveData<Int>().apply {
        addSource(mutableState) { value = if (it == STATE_ERROR) View.VISIBLE else View.GONE }
    }
    private val _error = MutableLiveData<Throwable?>(null)
    val error: LiveData<Throwable?> get() = _error

    private val mutableDomainList = MutableLiveData<List<Domain>>()
    val domainList: LiveData<List<Domain>> = mutableDomainList

    suspend fun updateData(context: Context) {
        if (mutableState.value == STATE_LOADING_DATA) {
            return
        }
        mutableState.postValue(STATE_LOADING_DATA)
        if (installationId == null || installationUrl == null) {
            return
        }
        val siteApiClient = (getApplication<WebasystXApplication>()
            .getApiClient()
            .getFactory(SiteApiClient::class.java) as SiteApiClientFactory)
            .instanceForInstallation(Installation(installationId, installationUrl))
        siteApiClient
            .getDomainList()
            .onSuccess {
                _error.postValue(null)
                mutableState.postValue(if (it.domains.isEmpty()) STATE_DATA_EMPTY else STATE_DATA_READY)
                mutableDomainList.postValue(it.domains.map { domain -> Domain(domain) })
            }
            .onFailure {
                _error.postValue(it)
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
        private const val TAG = "domain_list"
        const val STATE_UNKNOWN = 0
        const val STATE_LOADING_DATA = 1
        const val STATE_DATA_READY = 2
        const val STATE_DATA_EMPTY = 3
        const val STATE_ERROR = 4
    }
}
