package com.webasyst.x.cache

import android.content.Context
import android.content.SharedPreferences
import android.util.Log
import androidx.core.content.edit
import com.google.gson.GsonBuilder
import com.google.gson.reflect.TypeToken
import com.google.gson.typeadapters.RuntimeTypeAdapterFactory
import com.webasyst.waid.UserInfo
import com.webasyst.x.common.InstallationInterface
import com.webasyst.x.common.InstallationListStore
import com.webasyst.x.installations.Installation
import kotlin.reflect.KProperty

class DataCache(context: Context) : InstallationListStore {
    private val prefs = context
        .applicationContext
        .getSharedPreferences(PREFERENCES_STORE, Context.MODE_PRIVATE)
    var selectedInstallationId by prefs.stringPreference(SELECTED_INSTALLATION)
    private val gson = gson()

    override suspend fun getSelectedInstallationId(): String {
        return selectedInstallationId
    }

    override suspend fun setSelectedInstallationId(id: String) {
        selectedInstallationId = id
    }

    override suspend fun setInstallationList(installations: List<InstallationInterface>) {
        prefs.edit {
            putString(KEY_INSTALLATION_LIST, gson.toJson(installations))
        }
    }

    override suspend fun getInstallations(): List<InstallationInterface> {
        try{
            val installationList = prefs.getString(KEY_INSTALLATION_LIST, null) ?: return emptyList()
            return gson.fromJson(installationList, object : TypeToken<List<Installation>>() {}.type)
        } catch (e: Throwable) {
            Log.w(TAG, "Caught an exception while loading cached installations", e)
            return emptyList()
        }
    }

    override suspend fun clearInstallations() {
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
            remove(KEY_INSTALLATION_LIST)
            remove(SELECTED_INSTALLATION)
        }
    }

    fun SharedPreferences.stringPreference(key: String, default: String = ""): StringPreferenceDelegate {
        return StringPreferenceDelegate(this, key, default)
    }

    class StringPreferenceDelegate(
        private val prefs: SharedPreferences,
        private val key: String,
        private val default: String
    ) {
        operator fun getValue(thisRef: Any?, property: KProperty<*>): String =
            prefs.getString(key, default)!!

        operator fun setValue(thisRef: Any?, property: KProperty<*>, value: String) =
            prefs.edit {
                putString(key, value)
            }
    }

    companion object {
        private const val TAG = "DataCache"
        private const val PREFERENCES_STORE = "data_cache"
        private const val KEY_INSTALLATION_LIST = "installation_list"
        private const val KEY_USER_INFO = "user_data"
        private const val SELECTED_INSTALLATION = "selected_installation_id"

        fun gson() = GsonBuilder()
            .registerTypeAdapterFactory(
                RuntimeTypeAdapterFactory
                    .of(Installation.Icon::class.java)
                    .registerSubtype(Installation.Icon.AutoIcon::class.java, "auto")
                    .registerSubtype(Installation.Icon.GradientIcon::class.java, "gradient")
                    .registerSubtype(Installation.Icon.ImageIcon::class.java, "image")
            )
            .create()

    }
}
