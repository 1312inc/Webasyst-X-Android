package com.webasyst.x.add_wa

import android.app.Application
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.view.View
import androidx.appcompat.app.AlertDialog
import androidx.core.content.edit
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.webasyst.api.ApiClient
import com.webasyst.x.R
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class AddWebasystViewModel(app: Application) : AndroidViewModel(app) {
    private val apiClient by lazy {
        ApiClient.getInstance(getApplication())
    }
    private val preferences by lazy {
        app.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    }

    fun onAddWebasyst(view: View) {
        try {
            val authUrl = preferences.getString(AUTH_ENDPOINT_KEY, null)
            if (authUrl != null) {
                showAuthEndpointDialog(view, authUrl)
                return
            }

            view.isEnabled = false
            viewModelScope.launch {
                val cloudSignup = withContext(Dispatchers.IO) {
                    apiClient.postCloudSignUp()
                }
                cloudSignup
                    .onSuccess {
                        preferences.edit {
                            putString(AUTH_ENDPOINT_KEY, it.authEndpoint)
                        }
                        showAuthEndpointDialog(view, it.authEndpoint)
                    }
                    .onFailure {
                        AlertDialog
                            .Builder(view.context)
                            .setMessage(view.context.getString(R.string.add_webasyst_error, it.localizedMessage))
                            .setPositiveButton(R.string.btn_ok) { dialog, _ ->
                                dialog.dismiss()
                            }
                            .show()
                    }
            }
        } finally {
            view.isEnabled = true
        }
    }

    private fun showAuthEndpointDialog(view: View, url: String) {
        val context = view.context
        AlertDialog
            .Builder(context)
            .setMessage(context.getString(R.string.webasyst_added, url))
            .setPositiveButton(R.string.btn_open_wa_auth) { dialog, _ ->
                context.startActivity(Intent(Intent.ACTION_VIEW).also { it.data = Uri.parse(url) })
                dialog.dismiss()
            }
            .setNegativeButton(R.string.btn_ok) { dialog, _ ->
                dialog.dismiss()
            }
            .show()
    }

    companion object {
        const val PREFS_NAME = "add_wa_prefs"
        const val AUTH_ENDPOINT_KEY = "auth_endpoint"
    }
}
