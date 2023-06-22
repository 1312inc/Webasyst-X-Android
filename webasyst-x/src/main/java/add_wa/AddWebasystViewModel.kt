package com.webasyst.x.add_wa

import android.app.Application
import android.content.Intent
import android.net.Uri
import android.view.View
import androidx.appcompat.app.AlertDialog
import androidx.core.content.ContextCompat
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.webasyst.waid.CloudSignupResponse
import com.webasyst.waid.WAIDClient
import com.webasyst.x.NavDirections
import com.webasyst.x.R
import com.webasyst.x.WebasystXApplication
import com.webasyst.x.common.findRootNavController
import com.webasyst.x.common.getActivity
import com.webasyst.x.installations.Installation
import com.webasyst.x.installations.InstallationListFragment
import io.ktor.client.plugins.ClientRequestException
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
                            else -> view.context.getString(
                                R.string.add_webasyst_error,
                                it.localizedMessage
                            )
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

    fun onHelpClicked(view: View) {
        val browserIntent = Intent(Intent.ACTION_VIEW, Uri.parse(view.context.getString(R.string.add_webasyst_new_user_help_url)))
        ContextCompat.startActivity(view.context, browserIntent, null)
    }

    private fun postCreateHandler(view: View, result: CloudSignupResponse) {
        (view.getActivity() as InstallationListFragment.InstallationListView)
            .updateInstallations(result.id)

        val navController = view.findRootNavController()
        navController.navigate(NavDirections.actionGlobalMainFragment(
            installation = Installation(result),
            showAddWA = false,
        ))
    }
}
