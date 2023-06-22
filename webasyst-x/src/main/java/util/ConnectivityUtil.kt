package com.webasyst.x.util

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.net.ConnectivityManager
import android.net.Network
import android.net.NetworkRequest
import android.os.Build
import androidx.annotation.RequiresApi
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.channels.trySendBlocking
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.distinctUntilChanged

class ConnectivityUtil(private val context: Context) {
    private val connectivityManager by lazy {
        context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
    }

    fun connectivityFlow(): Flow<Int> =
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            connectivityFlowImpl()
        } else {
            connectivityFlowLegacy()
        }
            .distinctUntilChanged()

    @RequiresApi(Build.VERSION_CODES.LOLLIPOP)
    /**
     * Post-LOLLIPOP connectivityFlow implementation
     */
    private fun connectivityFlowImpl(): Flow<Int> = callbackFlow {
        val callback = object : ConnectivityManager.NetworkCallback() {
            override fun onAvailable(network: Network) {
                super.onAvailable(network)
                trySendBlocking(ONLINE)
            }

            override fun onLost(network: Network) {
                super.onLost(network)
                trySendBlocking(OFFLINE)
            }
        }

        connectivityManager.registerNetworkCallback(NetworkRequest.Builder().build(), callback)

        awaitClose {
            connectivityManager.unregisterNetworkCallback(callback)
        }
    }

    /**
     * Legacy (Pre-21) connectivityFlow implementation
     */
    private fun connectivityFlowLegacy(): Flow<Int> {

        return callbackFlow {
            val receiver = object : BroadcastReceiver() {
                override fun onReceive(context: Context, intent: Intent) {
                    if (intent.action == Intent.ACTION_AIRPLANE_MODE_CHANGED) {
                        intent.extras?.let { extras ->
                            if (extras.getBoolean("state", false)) {
                                trySendBlocking(OFFLINE)
                            } else {
                                trySendBlocking(ONLINE)
                            }
                        }
                    }
                }
            }

            val filter = IntentFilter(ConnectivityManager.EXTRA_NO_CONNECTIVITY).apply {
                addAction(Intent.ACTION_AIRPLANE_MODE_CHANGED)
            }
            context.registerReceiver(receiver, filter)

            awaitClose {
                context.unregisterReceiver(receiver)
            }
        }
    }

    companion object {
        const val OFFLINE = 0
        const val ONLINE = 1
    }
}
