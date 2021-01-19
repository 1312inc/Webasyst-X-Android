package com.webasyst.x.userinfo

import android.app.Application
import android.util.Log
import android.view.View
import androidx.appcompat.app.AlertDialog
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.webasyst.auth.WebasystAuthStateStore
import com.webasyst.waid.UserInfo
import com.webasyst.x.MainActivity
import com.webasyst.x.R
import com.webasyst.x.WebasystXApplication
import com.webasyst.x.util.getActivity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import net.openid.appauth.AuthState
import java.util.concurrent.atomic.AtomicLong

class UserInfoViewModel(val app: Application) :
    AndroidViewModel(app),
    WebasystAuthStateStore.Observer
{
    private var lastUpdate = AtomicLong(0)
    private val apiClient by lazy { getApplication<WebasystXApplication>().waidClient }
    private val stateStore by lazy(LazyThreadSafetyMode.NONE) {
        WebasystAuthStateStore.getInstance(getApplication())
    }
    private val cache by lazy { getApplication<WebasystXApplication>().dataCache }

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

    fun updateUserInfo() {
        if (lastUpdate.get() + MIN_USER_INFO_UPDATE_UNTERVAL > System.currentTimeMillis()) {
            Log.d(TAG, "Skipping user info update (debounce)")
            return
        }

        viewModelScope.launch(Dispatchers.Main) {
            withContext(Dispatchers.IO) { apiClient.getUserInfo() }
                .onSuccess {
                    cache.storeUserInfo(it)
                    setUserInfo(it)
                    lastUpdate.set(System.currentTimeMillis())
                }
                .onFailure {
                    Log.w(TAG, "Failed to fetch user info", it)
                }
        }
    }

    private fun setUserInfo(userInfo: UserInfo) {
        mutableUserName.postValue(userInfo.name)
        mutableUserEmail.postValue(userInfo.getEmail())
        mutableUserpicUrl.postValue(userInfo.userpic)
    }

    fun onSignOut(view: View) {
        val activity = view.getActivity() as MainActivity

        viewModelScope.launch {
            withContext(Dispatchers.IO) { apiClient.signOut() }
                .onSuccess {
                    Log.i(TAG, "Sign out successful")
                    activity.waSignOut()
                }
                .onFailure {
                    Log.w(TAG, "Failed to sign out on server", it)
                    AlertDialog
                        .Builder(activity)
                        .setMessage(activity.getString(R.string.sign_out_failed, it.localizedMessage))
                        .setPositiveButton(R.string.btn_ok) { dialog, _ ->
                            dialog.dismiss()
                        }
                        .show()
                }
        }
    }

    companion object {
        private const val TAG = "user_info"

        private const val MIN_USER_INFO_UPDATE_UNTERVAL = 1000 * 60 * 5 // 5 minutes
    }
}
