package com.webasyst.x.shop.orders

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.webasyst.api.shop.ShopApiClient
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class OrderListViewModel(
    application: Application,
    private val installationId: String?,
    private val installationUrl: String?
) : AndroidViewModel(application) {
    private val shopApiClient by lazy {
        ShopApiClient.getInstance(getApplication())
    }

    private val mutableOrderList = MutableLiveData<List<Order>>()
    val orderList: LiveData<List<Order>> = mutableOrderList

    private val mutableState = MutableLiveData<Int>().apply { value = STATE_LOADING }
    val state: LiveData<Int> = mutableState

    private val mutableErrorText = MutableLiveData<String>()
    val errorText: LiveData<String> = mutableErrorText

    init {
        viewModelScope.launch(Dispatchers.IO) {
            updateData()
        }
    }

    private suspend fun updateData() {
        if (installationId == null || installationUrl == null) {
            mutableState.postValue(STATE_ERROR)
            return
        }
        shopApiClient.getOrders(installationUrl, installationId)
            .onSuccess { orders ->
                mutableErrorText.postValue("")
                mutableOrderList.postValue(orders.orders.map { Order(it) })
                mutableState.postValue(if (orders.orders.isEmpty()) {
                    STATE_LOADED_EMPTY
                } else {
                    STATE_LOADED
                })
            }
            .onFailure {
                mutableState.postValue(STATE_ERROR)
                mutableErrorText.postValue(it.localizedMessage)
            }
    }

    class Factory(
        private val application: Application,
        private val installationId: String?,
        private val installationUrl: String?
    ) : ViewModelProvider.Factory {
        override fun <T : ViewModel?> create(modelClass: Class<T>): T =
            OrderListViewModel(application, installationId, installationUrl) as T
    }

    companion object {
        const val STATE_LOADING = 0
        const val STATE_LOADED = 1
        const val STATE_LOADED_EMPTY = 2
        const val STATE_ERROR = 3
    }
}
