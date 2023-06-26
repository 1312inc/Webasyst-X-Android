package com.webasyst.x.common

import android.app.Activity
import androidx.fragment.app.Fragment
import com.webasyst.api.ApiClient
import com.webasyst.waid.WAIDClient

interface XComponentProvider {
    fun getInstallationListStore(): InstallationListStore
    fun getApiClient(): ApiClient
    fun getWAIDClient(): WAIDClient
    fun mainActivityClass(): Class<out Activity>
    fun introSlides(): List<Fragment>
    fun webasystScope(): String
    fun clientId(): String
    fun userInfoStore(): UserInfoStore
}
