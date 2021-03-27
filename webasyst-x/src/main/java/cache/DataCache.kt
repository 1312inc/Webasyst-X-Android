package com.webasyst.x.cache

import android.content.Context
import androidx.core.content.edit
import com.google.gson.GsonBuilder
import com.google.gson.reflect.TypeToken
import com.google.gson.typeadapters.RuntimeTypeAdapterFactory
import com.webasyst.waid.UserInfo
import com.webasyst.x.installations.Installation

class DataCache(context: Context) {
    private val prefs = context
        .applicationContext
        .getSharedPreferences(PREFERENCES_STORE, Context.MODE_PRIVATE)
    private val gson = GsonBuilder()
        .registerTypeAdapterFactory(
            RuntimeTypeAdapterFactory
                .of(Installation.Icon::class.java)
                .registerSubtype(Installation.Icon.AutoIcon::class.java, "auto")
                .registerSubtype(Installation.Icon.GradientIcon::class.java, "gradient")
        )
        .create()

    fun storeInstallationList(installations: List<Installation>) {
        prefs.edit {
            putString(KEY_INSTALLATION_LIST, gson.toJson(installations))
        }
    }

    fun readInstallationList(): List<Installation>? {
        try{
            val installationList = prefs.getString(KEY_INSTALLATION_LIST, null) ?: return null
            return gson.fromJson(installationList, object : TypeToken<List<Installation>>() {}.type)
        } catch (e: Throwable) {
            return null
        }
    }

    fun clearInstallationList() {
        prefs.edit {
            remove(KEY_INSTALLATION_LIST)
        }
    }

    fun storeUserInfo(userInfo: UserInfo) {
        prefs.edit {
            putString(KEY_USER_INFO, gson.toJson(userInfo))
        }
    }

    fun readUserInfo(): UserInfo? {
        val userInfo = prefs.getString(KEY_USER_INFO, null) ?: return null
        return gson.fromJson(userInfo, UserInfo::class.java)
    }

    fun clearUserInfo() {
        prefs.edit {
            remove(KEY_USER_INFO)
        }
    }

    companion object {
        private const val PREFERENCES_STORE = "data_cache"
        private const val KEY_INSTALLATION_LIST = "installation_list"
        private const val KEY_USER_INFO = "user_data"
    }
}
