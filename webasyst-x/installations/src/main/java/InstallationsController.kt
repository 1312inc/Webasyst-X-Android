package com.webasyst.x.installations

import android.util.Log
import com.webasyst.api.ApiClient
import com.webasyst.api.webasyst.WebasystApiClient
import com.webasyst.api.webasyst.WebasystApiClientFactory
import com.webasyst.waid.WAIDClient
import com.webasyst.x.common.InstallationListStore
import com.webasyst.x.common.SingletonHolder
import com.webasyst.x.common.XComponentProvider
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking

class InstallationsController private constructor(componentProvider: XComponentProvider) {
    private val waidClient: WAIDClient = componentProvider.getWAIDClient()
    private val apiClient: ApiClient = componentProvider.getApiClient()
    private val webasystApiClientFactory = (apiClient.getFactory(WebasystApiClient::class.java) as WebasystApiClientFactory)
    private val dataCache: InstallationListStore = componentProvider.getInstallationListStore()

    private val mutableInstallations = MutableStateFlow(runBlocking { dataCache.getInstallations() }
        .map { Installation(it) }
        .also {
            Log.d(TAG, "Loaded ${it.size} installations from local storage")
        } ?: null.also { Log.d(TAG, "Did not load any installations from local storage") })
    val installations: StateFlow<List<Installation>?>
        get() = mutableInstallations

    private val mutableCurrentInstallation = MutableStateFlow(installations.value?.firstOrNull())
    val currentInstallation: StateFlow<Installation?>
        get() = mutableCurrentInstallation

    fun clearInstallations() {
        mutableInstallations.value = null
        runBlocking { dataCache.clearInstallations() }
        mutableCurrentInstallation.value = null
    }

    fun updateInstallations(callback: () -> Unit) {
        GlobalScope.launch(Dispatchers.IO) {
            var selectedInstallation = currentInstallation.value?.id
            Log.d(TAG, "Updating installations...")
            val installationsResponse = waidClient.getInstallationList()
            if (installationsResponse.isFailure()) {
                Log.d(TAG, "Failed to update installations", installationsResponse.getFailureCause())
                return@launch
            }
            val rawInstallations = installationsResponse.getSuccess().map {
                Installation(it)
            }
            Log.d(TAG, "Loaded ${rawInstallations.size} installations from WAID")
            if (rawInstallations.isNotEmpty() && selectedInstallation == null) {
                selectedInstallation = rawInstallations.first().id
            }
            val existingInstallations = dataCache.getInstallations()
            val installations = rawInstallations.map { installation ->
                val existing = existingInstallations.firstOrNull { it.id == installation.id }
                if (null != existing) {
                    installation.copy(
                        name = existing.name,
                        icon = Installation.Icon(existing)
                    )
                } else {
                    installation
                }
            }
//            mutableInstallations.value = installations
            dataCache.setInstallationList(installations)
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
            restoreSelection(namedInstallations, selectedInstallation)
            dataCache.setInstallationList(namedInstallations)
            Log.d(TAG, "Saved ${installations.size} augmented installations to local storage")
            mutableInstallations.value = namedInstallations

            callback()
        }
    }

    fun setSelectedInstallation(id: String?) {
        restoreSelection(installations?.value, id)
    }

    fun setSelectedInstallation(installation: Installation) {
        mutableCurrentInstallation.value = installations.value?.firstOrNull {
            it.id == installation.id
        }
    }

    private fun restoreSelection(installations: List<Installation>?, selected: String?) {
        mutableCurrentInstallation.value = if (selected == null) {
            null
        } else {
            installations?.firstOrNull {
                it.id == selected
            } ?: installations?.firstOrNull()
        }
    }

    companion object : SingletonHolder<XComponentProvider, InstallationsController>(::InstallationsController) {
        const val TAG = "InstallationsController"
    }
}
