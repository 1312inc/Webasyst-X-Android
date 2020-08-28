package com.webasyst.x

import android.app.Application
import androidx.annotation.MainThread
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import net.openid.appauth.AuthState

class MainActivityViewModel(app: Application) : AndroidViewModel(app) {
    // Placeholder data
    val userName = "Firstname Lastname"
    val userEmail = "email@example.com"

    private val mutableAuthState = MutableLiveData<AuthState>()
    val authState: LiveData<AuthState> = mutableAuthState
    @MainThread
    fun setAuthState(state: AuthState) {
        mutableAuthState.value = state
    }
}
