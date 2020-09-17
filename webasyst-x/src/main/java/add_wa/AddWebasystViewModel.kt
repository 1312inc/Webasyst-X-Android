package com.webasyst.x.add_wa

import android.app.Application
import android.view.View
import androidx.appcompat.app.AlertDialog
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.webasyst.api.ApiClient
import com.webasyst.api.CloudSignup
import com.webasyst.x.R
import com.webasyst.x.installations.InstallationListFragment
import com.webasyst.x.util.getActivity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class AddWebasystViewModel(app: Application) : AndroidViewModel(app) {
    private val apiClient by lazy {
        ApiClient.getInstance(getApplication())
    }

    fun onAddWebasyst(view: View) {
        try {
            view.isEnabled = false
            viewModelScope.launch {
                val cloudSignup = withContext(Dispatchers.IO) {
                    apiClient.postCloudSignUp()
                }
                cloudSignup
                    .onSuccess {
                        postCreateHandler(view, it)
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

    private fun postCreateHandler(view: View, result: CloudSignup) {
        (view.getActivity() as InstallationListFragment.InstallationListView)
            .updateInstallations(result.id)
    }
}
