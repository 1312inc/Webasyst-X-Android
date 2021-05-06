package com.webasyst.x.installations

import android.util.Log
import com.webasyst.api.webasyst.WebasystApiClient
import com.webasyst.api.webasyst.WebasystApiClientFactory
import com.webasyst.waid.WAIDClient
import com.webasyst.x.WebasystXApplication
import com.webasyst.x.cache.DataCache
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

object InstallationsController {
    private val waidClient: WAIDClient = WebasystXApplication.instance.waidClient
    private val apiClient = WebasystXApplication.instance.apiClient
    private val webasystApiClientFactory = (apiClient.getFactory(WebasystApiClient::class.java) as WebasystApiClientFactory)
    private val dataCache: DataCache = WebasystXApplication.instance.dataCache

    private val mutableInstallations = MutableStateFlow(dataCache.readInstallationList()?.also {
        Log.d(TAG, "Loaded ${it.size} installations from local storage")
    } ?: emptyList<Installation>().also { Log.d(TAG, "Did not load any installations from local storage") })
    val installations: StateFlow<List<Installation>>
        get() = mutableInstallations

    fun clearInstallations() {
        mutableInstallations.value = emptyList()
        dataCache.clearInstallationList()
    }

    fun updateInstallations() {
        GlobalScope.launch(Dispatchers.IO) {
            Log.d(TAG, "Updating installations...")
            val installationsResponse = waidClient.getInstallationList()
            if (installationsResponse.isFailure()) {
                Log.d(TAG, "Failed to update installations", installationsResponse.getFailureCause())
                return@launch
            }
            val installations = installationsResponse.getSuccess().map {
                Installation(it)
            }
            Log.d(TAG, "Loaded ${installations.size} installations from WAID")
            mutableInstallations.value = installations
            dataCache.storeInstallationList(installations)
            Log.d(TAG, "Saved ${installations.size} installations to local storage")
            val namedInstallations = installations
                .associateWith { installation ->
                    async {
                        webasystApiClientFactory
                            .instanceForInstallation(installation)
                            .getInstallationInfo()
                    }
                }
                .map { (installation, asyncInfo) ->
                    val infoResponse = asyncInfo.await()
                    if (infoResponse.isSuccess()) {
                        val info = infoResponse.getSuccess()
                        installation.copy(
                            name = info.name,
                            icon = Installation.Icon(info),
                        )
                    } else {
                        installation
                    }
                }
            dataCache.storeInstallationList(namedInstallations)
            Log.d(TAG, "Saved ${installations.size} augmented installations to local storage")
            mutableInstallations.value = namedInstallations
        }
    }

    const val TAG = "InstallationsController"
}
