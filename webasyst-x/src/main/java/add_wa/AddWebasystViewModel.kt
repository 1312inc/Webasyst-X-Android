package com.webasyst.x.add_wa

import android.app.Application
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.text.Editable
import android.text.Spannable
import android.text.SpannableStringBuilder
import android.text.style.ForegroundColorSpan
import android.util.Log
import android.view.View
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.webasyst.api.WebasystException
import com.webasyst.auth.WebasystAuthHelper
import com.webasyst.x.R
import com.webasyst.x.barcode.QrHandlerInterface
import com.webasyst.x.common.XComponentProvider
import com.webasyst.x.common.getActivity
import com.webasyst.x.installations.Installation
import com.webasyst.x.installations.InstallationsController
import io.ktor.client.plugins.ClientRequestException
import io.ktor.http.HttpStatusCode
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlinx.coroutines.withTimeout
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine


class AddWebasystViewModel(app: Application) : AndroidViewModel(app), QrHandlerInterface {
    var showSignOut: Boolean = false
    val code = MutableLiveData("")
    val codeError_ = MutableLiveData<Int?>(null)
    val codeError: LiveData<Int?> = codeError_
    val companyName = MutableLiveData("")
    private val inProgress_ = MutableLiveData(false)
    val inProgress: LiveData<Boolean> = inProgress_
    private val codeInProgress_ = MutableLiveData(false)
    val codeInProgress: LiveData<Boolean> = codeInProgress_
    private val addInProgress_ = MutableLiveData(false)
    val addInProgress: LiveData<Boolean> = addInProgress_

    private val _konfetti = MutableStateFlow(false)
    val konfetti: MutableStateFlow<Boolean> = _konfetti
    private val _qrKonfetti = MutableStateFlow("")
    override val qrKonfetti: MutableStateFlow<String> = _qrKonfetti

    private val waidClient by lazy { (getApplication() as XComponentProvider).getWAIDClient() }
    private val installationsController = InstallationsController.instance(getApplication() as XComponentProvider)
    private val userInfoStore by lazy { (getApplication() as XComponentProvider).userInfoStore() }


    suspend fun getWaidContact(): String {
        val userInfo = userInfoStore
            .userInfo.firstOrNull()
        return if (userInfo != null) {
            userInfo.email.firstOrNull()?.value
                ?: (userInfo.phone.firstOrNull()?.value ?: "")
        } else ""
    }

    fun installationsEmpty(): Flow<Boolean> {
        return installationsController
            .installations
            .map { it?.isEmpty() ?: true }
    }

    fun emailNotBlank(): Flow<Boolean> {
        return userInfoStore
            .userInfo
            .filterNotNull()
            .map {
                it.email.isNotEmpty() && it.email.first().value.isNotBlank()
            }
    }

    private var addWebasystJob: Job? = null
    fun onAddWebasyst(view: View) {
        addWebasystJob?.cancel()
        addWebasystJob = viewModelScope.launch {
            withTimeout(15_000L) {
                try {
                    inProgress_.value = true
                    addInProgress_.value = true
                    val cloudSignup = withContext(Dispatchers.IO) {
                        waidClient.postCloudSignUp {
                            planId = "TRIAL"
                            bundle = "allwebasyst"
                            userdomain = (companyName.value ?: "").transliterateRussianToLatin()
                            accountName = companyName.value ?: ""
                        }
                    }
                    if (cloudSignup.isSuccess()) {
                        val result = cloudSignup.getSuccess()
                        delay(1000)
                        _konfetti.emit(true)
                        delay(360)
                        val dialog = MaterialAlertDialogBuilder(view.context)
                            .setMessage(view.context.getString(R.string.add_webasyst_new_acc_created_success))
                            .setCancelable(false)
                            .show()
                        suspendCoroutine<Unit> { continuation ->
                            installationsController.updateInstallations {
                                continuation.resume(Unit)
                                installationsController.setSelectedInstallation(result.id)
                                dialog.dismiss()
                            }
                        }
                    } else {
                        val it = cloudSignup.getFailureCause()
                        var msg = view.context.getString(
                            R.string.add_webasyst_error,
                            it.localizedMessage
                        )
                        if (it is ClientRequestException) {
                            val error = com.webasyst.api.WebasystError(it.response)
                            if (it.response.status == HttpStatusCode.Conflict){
                                if (error.code == "already_exists")
                                    msg = view.context.getString(R.string.add_webasyst_error_limit_exceeded)
                                else if (error.code == "domain_is_in_use")
                                    msg = view.context.getString(R.string.add_webasyst_error_domain_is_in_use)

                            } else if (it.response.status == HttpStatusCode.BadRequest
                                && error.code == "invalid_domain")
                                msg = view.context.getString(R.string.add_webasyst_error_invalid_domain)
                        }
                        MaterialAlertDialogBuilder(view.context)
                            .setMessage(msg)
                            .setPositiveButton(R.string.btn_ok) { dialog, _ ->
                                dialog.dismiss()
                            }
                            .show()
                    }
                } finally {
                    inProgress_.value = false
                    addInProgress_.value = false
                }
            }
        }
    }

    fun onSignOut(view: View) {
        view.getActivity()?.let { activity ->
            val authHelper = WebasystAuthHelper(activity)
            authHelper.signOut()
        }
    }

    fun onJoinAccountClicked(view: View) {
        viewModelScope.launch {
            val response = withContext(Dispatchers.IO) { waidClient.requestMergeCode() }
            if (response.isFailure()) {
                val cause = response.getFailureCause()
                val msg = when ((cause as? WebasystException)?.webasystCode){
                    "not_eligible" -> view.context.getString(R.string.add_webasyst_error_merge_not_eligible)
                    else -> cause.message ?: cause.localizedMessage
                }
                MaterialAlertDialogBuilder(view.context)
                    .setMessage(msg)
                    .setPositiveButton(R.string.btn_ok) { dialog, _ ->
                        dialog.dismiss()
                    }
                    .show()

                inProgress_.value = false
                return@launch
            }

            val mergeCode = response.getSuccess()

            try {
                view.getActivity()?.let { activity ->
                    activity.javaClass.let { activityClass ->
                        val authHelper = WebasystAuthHelper(activity)
                        authHelper.signIn(
                            activityClass,
                            mapOf(
                                "change_user" to "1",
                                "mergecode" to mergeCode.code,
                            )
                        )
                    }
                }
            } catch (e: Throwable) {
                val msg = view.context.getString(R.string.add_webasyst_error_merge_failed)
                MaterialAlertDialogBuilder(view.context)
                    .setMessage(msg)
                    .setPositiveButton(R.string.btn_ok) { dialog, _ ->
                        dialog.dismiss()
                    }
                    .show()
            }
        }
    }

    fun aboutWebasystClicked(view: View) {
        val browserIntent = Intent(Intent.ACTION_VIEW, Uri.parse(view.context.getString(R.string.add_webasyst_new_user_help_url)))
        ContextCompat.startActivity(view.context, browserIntent, null)
    }

    fun onCompanyNameChanged(view: View){
        (view as? TextView)?.apply{
            val txt = resources.getString(
                R.string.add_webasyst_add_shop_company_example,
                (companyName.value ?: "").transliterateRussianToLatin()
            )
            val ssb = SpannableStringBuilder(txt)
            val pos = txt.indexOf(".")
            if (pos > -1) {
                ssb.setSpan(
                    ForegroundColorSpan(
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M)
                            resources.getColor(R.color.secondary, null)
                        else resources.getColor(R.color.secondary)
                    ),
                    0,
                    pos,
                    Spannable.SPAN_INCLUSIVE_INCLUSIVE
                )
                setText(ssb, TextView.BufferType.SPANNABLE)
            }
        }
    }

    fun onInstallationConnectByCode(s: Editable, view: View) {
        if (s.length < 8) return
        viewModelScope.launch {
            inProgress_.value = true
            codeInProgress_.value = true
            val response = withContext(Dispatchers.IO) {
                waidClient.connectInstallation(s.toString())
            }
            if (response.isSuccess()) {
                val result = response.getSuccess()
                _konfetti.emit(true)
                delay(360)
                val dialog = MaterialAlertDialogBuilder(view.context)
                    .setMessage(
                        view.context.getString(
                            R.string.add_webasyst_new_acc_connected_success,
                            result.domain
                        )
                    )
                    .setCancelable(false)
                    .show()
                installationsController.updateInstallations {
                    installationsController.setSelectedInstallation(result.id)
                    dialog.dismiss()
                }
            } else {
                MaterialAlertDialogBuilder(view.context)
                    .setMessage(
                        view.context.getString(R.string.add_webasyst_connect_installation_error)
                    )
                    .setPositiveButton(R.string.btn_ok) { dialog, _ ->
                        dialog.dismiss()
                    }
                .show()
                code.value = ""
                Log.e(TAG, "connectInstallation for code=$s failed: ${response.getFailureCause().message ?: response.getFailureCause().localizedMessage}")
            }
            inProgress_.value = false
            codeInProgress_.value = false
        }
    }

    private val _qrCodeSuccess = MutableLiveData(false)
    private val _qrCodeHint = MutableLiveData((getApplication() as Application)
        .resources.getString(R.string.auth_qr_hint))

    override val qrCodeSuccess = _qrCodeSuccess
    override val qrCodeHint = _qrCodeHint

    override fun handleBarcode(barcode: String, context: Context): Boolean {
        Log.d(TAG, "qr-code $barcode")
        if (barcode.indexOf(WEBASYSTID_ADDACCOUNT) > 0){
            onSubmitQrCode(barcode, context)
            return true
        } else if (barcode.indexOf(WEBASYSTID_SIGNIN) > 0){
            _qrCodeSuccess.value = true
            MaterialAlertDialogBuilder(context)
                .setMessage(context.getString(R.string.auth_qr_err_code_improper))
                .setPositiveButton(R.string.btn_ok) { dialog, _ ->
                    _qrCodeSuccess.value = false
                    _qrCodeHint.value = (getApplication() as Application)
                        .resources.getString(R.string.auth_qr_hint)
                    dialog.dismiss()
                }
                .show()
            return true
        }
        return false
    }

    override suspend fun onInvalidCode(context: Context){
        _qrCodeSuccess.postValue(true)
        _qrCodeHint.postValue(context.resources.getString(R.string.auth_qr_err_code_irrelevant))
        delay(3000)
        _qrCodeSuccess.postValue(false)
        _qrCodeHint.postValue(context.resources.getString(R.string.auth_qr_hint))
    }

    override fun onSetInitHint(){
        _qrCodeHint.value = (getApplication() as Application)
            .resources.getString(R.string.auth_qr_hint)
    }

    override fun onSetCameraPermissionDeniedHint(){
        _qrCodeHint.value = (getApplication() as Application)
            .resources.getString(R.string.auth_qr_permission)
    }

    var submitQrCodeJob: Job? = null
    private fun onSubmitQrCode(qrCode: String, context: Context) {
        submitQrCodeJob?.cancel()
        submitQrCodeJob = viewModelScope.launch(Dispatchers.IO) {
            submitQrCode(qrCode, context)
        }
    }

    private suspend fun submitQrCode(qrCode: String, context: Context) {
        _qrCodeSuccess.postValue(true)
        _qrCodeHint.postValue(
            (getApplication() as Application)
                .resources.getString(R.string.auth_qr_adding_wa)
        )
        val response = withContext(Dispatchers.IO) {
            waidClient.connectInstallation(qrCode)
        }
        if (response.isSuccess()) {
            val result = response.getSuccess()
            _qrKonfetti.emit(result.domain)
            suspendCoroutine { cont ->
                installationsController.updateInstallations {
                    cont.resume(Unit)
                    installationsController.setSelectedInstallation(result.id)
                }
            }
            _qrKonfetti.emit("")
        } else {
            Log.e(TAG,
                "connectInstallation for code=$qrCode failed: ${response.getFailureCause().message ?: response.getFailureCause().localizedMessage}")
            _qrCodeHint.postValue((getApplication() as Application)
                .resources.getString(R.string.auth_qr_err_code_invalid))
            delay(3000)
        }
        _qrCodeSuccess.postValue(false)
        _qrCodeHint.postValue((getApplication() as Application)
            .resources.getString(R.string.auth_qr_hint))
    }

    companion object {
        const val TAG = "WADD"

        const val WEBASYSTID_ADDACCOUNT = "WEBASYSTID-ADDACCOUNT"
        const val WEBASYSTID_SIGNIN = "WEBASYSTID-SIGNIN"
    }
}

private fun String.transliterateRussianToLatin(): String {
    val abcRus = charArrayOf(
        'а', 'б', 'в', 'г', 'д', 'е', 'ё', 'ж', 'з', 'и', 'й', 'к', 'л', 'м', 'н', 'о', 'п', 'р',
        'с', 'т', 'у', 'ф', 'х', 'ц', 'ч', 'ш', 'щ', 'ъ', 'ы', 'ь', 'э', 'ю', 'я',
        'А', 'Б', 'В', 'Г', 'Д', 'E', 'Ё', 'Ж', 'З', 'И', 'Й', 'К', 'Л', 'М', 'Н', 'О', 'П', 'Р',
        'С', 'Т', 'У', 'Ф', 'Х', 'Ц', 'Ч', 'Ш', 'Щ', 'Ъ', 'Ы', 'Ь', 'Э', 'Ю', 'Я', '-'
    )
    val abcLat = arrayOf(
        "a", "b", "v", "g", "d", "e", "yë", "zh", "z", "i", "í", "k", "l", "m", "n", "o", "p", "r",
        "s", "t", "u", "f", "kh", "ts", "ch", "sh", "shch", "ʺ", "y", "ʹ", "e", "yu", "ya",
        "A", "B", "V", "G", "D", "E", "YË", "ZH", "Z", "I", "Í", "K", "L", "M", "N", "O", "P", "R",
        "S", "T", "U", "F", "KH", "TS", "CH", "SH", "SHCH", "ʺ", "Y", "ʹ", "E", "YU", "YA", "-"
    )
    val builder = StringBuilder()
    for (i in 0 until this.length) {
        val src = this[i]
        val pos = abcRus.indexOf(src)
        val dst = if (pos > -1) abcLat[pos]
            else if (src in 'a'..'z' || src in 'A'..'Z'  || src in '0'..'9') src
            else '-'
        builder.append(dst)
    }
    return builder.toString()
}
