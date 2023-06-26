package com.webasyst.x.userinfo

import android.content.SharedPreferences
import androidx.core.content.edit
import com.google.gson.Gson
import com.webasyst.waid.UserInfo
import com.webasyst.x.common.UserInfoStore
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class UserInfoStoreImpl private constructor(
    scope: CoroutineScope,
    private val prefs: SharedPreferences,
    private val gson: Gson
) : UserInfoStore {
    private val _userInfo = MutableStateFlow<UserInfo?>(null)
    override val userInfo: StateFlow<UserInfo?> = _userInfo

    init {
        scope.launch(Dispatchers.IO) {
            val userInfo = prefs.getString(KEY_USER_INFO, null) ?: return@launch
            _userInfo.value = gson.fromJson(userInfo, UserInfo::class.java)
        }
    }

    override suspend fun setUserInfo(userInfo: UserInfo) = withContext(Dispatchers.IO) {
        prefs.edit {
            putString(KEY_USER_INFO, gson.toJson(userInfo))
        }
        _userInfo.value = userInfo
    }

    override suspend fun sweepUserInfo() {
        prefs.edit {
            remove(KEY_USER_INFO)
        }
        _userInfo.value = null
    }

    companion object {
        private const val KEY_USER_INFO = "user_data"

        operator fun invoke(
            scope: CoroutineScope,
            prefs: SharedPreferences,
            gson: Gson
        ) : UserInfoStore = UserInfoStoreImpl(scope, prefs, gson)
    }
}
