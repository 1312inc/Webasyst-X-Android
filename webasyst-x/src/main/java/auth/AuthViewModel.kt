package com.webasyst.x.auth

import android.view.View
import androidx.lifecycle.LiveData
import androidx.lifecycle.MediatorLiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.webasyst.x.MainActivity
import com.webasyst.x.util.getActivity

class AuthViewModel : ViewModel() {
    val state = MutableLiveData<Int>().apply { value = STATE_IDLE }

    val signInVisible: LiveData<Boolean> = MediatorLiveData<Boolean>().apply {
        addSource(state) {
            value = (it == STATE_IDLE)
        }
    }

    val signinInVisible: LiveData<Boolean> = MediatorLiveData<Boolean>().apply {
        addSource(state) {
            value = (it == STATE_AUTHENTICATING)
        }
    }

    fun onSignIn(view: View) {
        (view.getActivity() as MainActivity).waSignIn()
    }

    companion object {
        const val STATE_IDLE = 0
        const val STATE_AUTHENTICATING = 1
    }
}
