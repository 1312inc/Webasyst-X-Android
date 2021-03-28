package com.webasyst.x.shop.orders

import android.app.Application
import android.content.Context
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.webasyst.api.ApiError
import com.webasyst.api.Installation
import com.webasyst.api.shop.ShopApiClient
import com.webasyst.api.shop.ShopApiClientFactory
import com.webasyst.x.R
import com.webasyst.x.WebasystXApplication

class OrderListViewModel(
    application: Application,
    private val installationId: String?,
    private val installationUrl: String?
) : AndroidViewModel(application) {
    private val shopApiClient by lazy {
        (getApplication<WebasystXApplication>()
            .apiClient
            .getFactory(ShopApiClient::class.java)
            as ShopApiClientFactory)
            .instanceForInstallation(Installation(installationId ?: "", installationUrl ?: ""))
    }

    val appName = application.getString(R.string.app_shop)
    val apiName = "shop.order.search"

    private val mutableOrderList = MutableLiveData<List<Order>>()
    val orderList: LiveData<List<Order>> = mutableOrderList

    private val mutableState = MutableLiveData<Int>().apply { value = STATE_UNKNOWN }
    val state: LiveData<Int> = mutableState

    private val mutableErrorText = MutableLiveData<String>()
    val errorText: LiveData<String> = mutableErrorText

    suspend fun updateData(context: Context) {
        if (mutableState.value == STATE_LOADING) {
            return
        }
        mutableState.postValue(STATE_LOADING)
        if (installationId == null || installationUrl == null) {
            return
        }
        shopApiClient.getOrders()
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
                val errorMessage = when {
                    it is ApiError && it.error == ApiError.APP_NOT_INSTALLED ->
                        context.getString(R.string.err_app_not_installed, it.app, installationUrl)
                    else -> it.localizedMessage
                }
                mutableState.postValue(STATE_ERROR)
                mutableErrorText.postValue(errorMessage)
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
        const val STATE_UNKNOWN = 0
        const val STATE_LOADING = 1
        const val STATE_LOADED = 2
        const val STATE_LOADED_EMPTY = 3
        const val STATE_ERROR = 4
    }
}
