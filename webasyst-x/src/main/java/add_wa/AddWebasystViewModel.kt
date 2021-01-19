package com.webasyst.x.add_wa

import android.app.Application
import android.view.View
import androidx.appcompat.app.AlertDialog
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.webasyst.waid.CloudSignup
import com.webasyst.waid.WAIDClient
import com.webasyst.x.R
import com.webasyst.x.WebasystXApplication
import com.webasyst.x.installations.InstallationListFragment
import com.webasyst.x.util.getActivity
import io.ktor.client.features.ClientRequestException
import io.ktor.http.HttpStatusCode
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class AddWebasystViewModel(app: Application) : AndroidViewModel(app) {
    private val mutableBusy = MutableLiveData<Boolean>().apply { value = false }
    val busy: LiveData<Boolean> = mutableBusy

    private val waidClient : WAIDClient = getApplication<WebasystXApplication>().waidClient

    fun onAddWebasyst(view: View) {
        viewModelScope.launch {
            try {
                mutableBusy.value = true
                val cloudSignup = withContext(Dispatchers.IO) {
                    waidClient.postCloudSignUp()
                }
                cloudSignup
                    .onSuccess {
                        postCreateHandler(view, it)
                    }
                    .onFailure {
                        val message = when {
                            it is ClientRequestException && it.response?.status == HttpStatusCode.Conflict ->
                                view.context.getString(R.string.add_webasyst_error_limit_exceeded)
                            else -> view.context.getString(R.string.add_webasyst_error, it.localizedMessage)
                        }
                        AlertDialog
                            .Builder(view.context)
                            .setMessage(message)
                            .setPositiveButton(R.string.btn_ok) { dialog, _ ->
                                dialog.dismiss()
                            }
                            .show()
                    }
            } finally {
                mutableBusy.value = false
            }
        }
    }

    private fun postCreateHandler(view: View, result: CloudSignup) {
        (view.getActivity() as InstallationListFragment.InstallationListView)
            .updateInstallations(result.id)
    }
}
