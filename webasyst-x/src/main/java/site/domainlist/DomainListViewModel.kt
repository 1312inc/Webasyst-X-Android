package com.webasyst.x.site.domainlist

import android.app.Application
import android.content.Context
import android.util.Log
import android.view.View
import androidx.appcompat.app.AlertDialog
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MediatorLiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.webasyst.api.ApiException
import com.webasyst.api.Installation
import com.webasyst.api.site.SiteApiClient
import com.webasyst.api.site.SiteApiClientFactory
import com.webasyst.x.R
import com.webasyst.x.WebasystXApplication
import kotlinx.coroutines.CancellationException

class DomainListViewModel(
    app: Application,
    private val installationId: String?,
    private val installationUrl: String?
) : AndroidViewModel(app) {
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
    private val mutableErrorText = MutableLiveData<String>()
    val errorText: LiveData<String> = mutableErrorText

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
            .apiClient
            .getFactory(SiteApiClient::class.java) as SiteApiClientFactory)
            .instanceForInstallation(Installation(installationId, installationUrl))
        siteApiClient
            .getDomainList()
            .onSuccess {
                mutableErrorText.postValue("")
                mutableState.postValue(if (it.domains.isEmpty()) STATE_DATA_EMPTY else STATE_DATA_READY)
                mutableDomainList.postValue(it.domains.map { domain -> Domain(domain) })
            }
            .onFailure {
                Log.e(TAG, "failed to fetch domain list: $it", it)
                if (it is ApiException && it.cause !is CancellationException) {
                    AlertDialog
                        .Builder(context)
                        .setMessage(context.getString(R.string.waid_error, it.localizedMessage))
                        .setPositiveButton(R.string.btn_ok) { dialog, _ -> dialog.dismiss() }
                        .show()
                }
                mutableErrorText.postValue(it.localizedMessage)
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
