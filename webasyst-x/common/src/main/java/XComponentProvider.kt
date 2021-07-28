package com.webasyst.x.common

import com.webasyst.api.ApiClient
import com.webasyst.waid.WAIDClient

interface XComponentProvider {
    fun getInstallationListStore(): InstallationListStore
    fun getApiClient(): ApiClient
    fun getWAIDClient(): WAIDClient
}
