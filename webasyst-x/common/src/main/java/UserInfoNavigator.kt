package com.webasyst.x.common

interface UserInfoNavigator {
    fun openProfileEditor()
    fun goToPinCode(forRemove: Boolean = false)
    fun popBackStack()
}
