package com.webasyst.x.main

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class MainViewModel : ViewModel() {
    val showAddWA = MutableLiveData<Boolean>().apply { value = true }
}
