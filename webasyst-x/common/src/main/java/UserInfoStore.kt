package com.webasyst.x.common

import com.webasyst.waid.UserInfo
import kotlinx.coroutines.flow.StateFlow

interface UserInfoStore {
    val userInfo: StateFlow<UserInfo?>
    suspend fun setUserInfo(userInfo: UserInfo)
    suspend fun sweepUserInfo()
}
