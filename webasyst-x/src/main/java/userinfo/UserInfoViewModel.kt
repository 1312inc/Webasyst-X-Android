package com.webasyst.x.userinfo

import android.app.Application
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import androidx.appcompat.app.AlertDialog
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.webasyst.api.ApiClient
import com.webasyst.api.UserInfo
import com.webasyst.auth.WebasystAuthStateStore
import com.webasyst.x.MainActivity
import com.webasyst.x.R
import com.webasyst.x.cache.DataCache
import com.webasyst.x.util.getActivity
import kotlinx.android.synthetic.main.dialog_progress.view.messageView
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

        val dialog = AlertDialog
            .Builder(activity)
            .setView(LayoutInflater.from(activity).inflate(
                R.layout.dialog_progress,
                null,
                false
            ).also {
                it.messageView.setText(R.string.signing_out)
            })
            .show()

        viewModelScope.launch {
            withContext(Dispatchers.IO) { apiClient.signOut() }
                .onSuccess {
                    Log.i(TAG, "Sign out successful")
                    dialog.dismiss()
                    activity.waSignOut()
                }
                .onFailure {
                    Log.w(TAG, "Failed to sign out on server", it)
                    AlertDialog
                        .Builder(activity)
                        .setMessage(R.string.sign_out_failed)
                        .setPositiveButton(R.string.btn_ok) { dialog, _ ->
                            dialog.dismiss()
                        }
                }
        }
    }

    companion object {
        private const val TAG = "user_info"

        private const val MIN_USER_INFO_UPDATE_UNTERVAL = 1000 * 60 * 5 // 5 minutes
    }
}
