package com.webasyst.x.userinfo

import android.app.Application
import android.os.Handler
import android.util.Log
import android.view.View
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.webasyst.api.ApiClient
import com.webasyst.api.UserInfo
import com.webasyst.auth.WebasystAuthStateStore
import com.webasyst.x.MainActivity
import com.webasyst.x.cache.DataCache
import com.webasyst.x.util.getActivity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import net.openid.appauth.AuthState

class UserInfoViewModel(val app: Application) :
    AndroidViewModel(app),
    WebasystAuthStateStore.Observer
{
    private val apiClient by lazy { ApiClient.getInstance(getApplication()) }
    private val stateStore by lazy(LazyThreadSafetyMode.NONE) {
        WebasystAuthStateStore.getInstance(getApplication())
    }
    private val cache by lazy { DataCache.getInstance(getApplication()) }

    private val mutableUserName = MutableLiveData<String>()
    val userName: LiveData<String> = mutableUserName

    private val mutableUserEmail = MutableLiveData<String>()
    val userEmail: LiveData<String> = mutableUserEmail

    private val mutableUserpicUrl = MutableLiveData<String>()
    val userpicUrl: LiveData<String> = mutableUserpicUrl

    override fun onAuthStateChange(state: AuthState?) {
        if (state?.isAuthorized == true) {
            updateUserInfo()
        } else {
            cache.clearUserInfo()
        }
    }

    init {
        cache.readUserInfo()?.let {
            setUserInfo(it)
        }
        updateUserInfo()
        stateStore.addObserver(this)
    }

    override fun onCleared() {
        super.onCleared()
        stateStore.removeObserver(this)
    }

    private fun updateUserInfo() {
        viewModelScope.launch(Dispatchers.Main) {
            withContext(Dispatchers.IO) { apiClient.getUserInfo() }
                .onSuccess {
                    cache.storeUserInfo(it)
                    setUserInfo(it)
                }
                .onFailure { println(it)/* TODO */ }
        }
    }

    private fun setUserInfo(userInfo: UserInfo) {
        mutableUserName.postValue(userInfo.name)
        mutableUserEmail.postValue(userInfo.getEmail())
        mutableUserpicUrl.postValue(userInfo.userpic)
    }

    fun onSignOut(view: View) {
        val activity = view.getActivity() as MainActivity
        GlobalScope.launch(Dispatchers.IO) {
            apiClient
                .signOut { Handler(view.context.mainLooper).post { activity.waSignOut() } }
                .onSuccess {
                    Log.i(TAG, "Sign out successful")
                }
                .onFailure {
                    Log.w(TAG, "Failed to sign out on server", it)
                }
        }
    }

    companion object {
        private const val TAG = "user_info"
    }
}
