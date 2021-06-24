package com.webasyst.x.signin

import android.app.Application
import android.os.Build
import android.view.View
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MediatorLiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import androidx.navigation.NavController
import androidx.navigation.findNavController
import com.google.i18n.phonenumbers.NumberParseException
import com.google.i18n.phonenumbers.PhoneNumberUtil
import com.google.i18n.phonenumbers.Phonenumber
import com.webasyst.waid.HeadlessCodeRequestResult
import com.webasyst.x.BuildConfig
import com.webasyst.x.R
import com.webasyst.x.WebasystXApplication
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class SignInViewModel(application: Application) : AndroidViewModel(application) {
    private val phoneNumberUtil by lazy { PhoneNumberUtil.getInstance() }
    private val waidClient by lazy { (getApplication() as WebasystXApplication).waidClient }

    val phone = MutableLiveData("+7")

    private val _phoneError = MutableLiveData<Int?>(null)
    val phoneError: LiveData<Int?> get() = _phoneError

    private val _submitEnabled = MutableLiveData(true)
    val submitEnabled: LiveData<Boolean> get() = _submitEnabled

    val code = MutableLiveData("")

    val submitCodeEnabled: LiveData<Boolean> = MediatorLiveData<Boolean>().apply {
        this.addSource(code) {
            value = it.isNotBlank()
        }
    }

    private val codeResponse = MutableStateFlow<HeadlessCodeRequestResult?>(null)

    private var submitJob: Job? = null
    fun onSubmit(view: View) {
        submitJob?.cancel()
        submitJob = viewModelScope.launch(Dispatchers.Default) { submit(phone.value ?: "", view.findNavController()) }
    }
    suspend fun submit(phoneValue: String, navController: NavController) {
        try {
            _submitEnabled.postValue(false)
            var phoneNumber: Phonenumber.PhoneNumber? = null
            val isValid = try {
                phoneNumber = phoneNumberUtil.parse(phoneValue, "ZZ")
                phoneNumberUtil.isValidNumber(phoneNumber)
            } catch (e: NumberParseException) {
                false
            }

            if (!isValid) {
                _phoneError.postValue(R.string.sign_in_err_number_format)
                return
            }

            val application = getApplication<WebasystXApplication>()
            val response = withContext(Dispatchers.IO) {
                waidClient.postAuthCode(
                    clientId = BuildConfig.CLIENT_ID,
                    scope = application.webasystScope,
                    locale = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                        application.applicationContext.resources.configuration.locales[0].toString()
                    } else {
                        application.applicationContext.resources.configuration.locale.toString()
                    },
                    email = null,
                    phone = phoneNumberUtil.format(
                        phoneNumber,
                        PhoneNumberUtil.PhoneNumberFormat.E164
                    )
                )
            }
            codeResponse.value = response
            startResendTimer(response.nextRequestAllowedAt)

            withContext(Dispatchers.Main) {
                navController.navigate(PhoneInputFragmentDirections.actionPhoneInputFragmentToCodeInputFragment())
            }
        } catch (e: Throwable) {
            _phoneError.postValue(R.string.generic_error_retry_later)
        } finally {
            _submitEnabled.postValue(true)
        }
    }

    var submitCodeJob: Job? = null
    fun onSubmitCode(view: View) {
        println(codeResponse)
        submitCodeJob?.cancel()
        submitCodeJob = viewModelScope.launch(Dispatchers.IO) {
            val cr = codeResponse.value ?: return@launch
            try {
                val res = waidClient.postHeadlessToken(
                    clientId = BuildConfig.CLIENT_ID,
                    codeVerifier = cr.codeChallenge.password,
                    code = code.value ?: "",
                )
                println(res)
            } catch (e: Throwable) {
                println(e)
            }
        }
    }

    private val _codeSendTime = MutableLiveData(0L)
    val codeSendAgain: LiveData<Long> get() = _codeSendTime
    fun startResendTimer(tt: Long) {
        resendTimer?.cancel()
        resendTimer = viewModelScope.launch { resendTimer(tt) }
    }
    private var resendTimer: Job? = null
    private suspend fun resendTimer(tt: Long) {
        var t = tt - System.currentTimeMillis()
        withContext(Dispatchers.Main) {
            while (t > 0) {
                _codeSendTime.postValue(t)
                t--
                delay(1000)
            }
            _codeSendTime.postValue(0)
        }
    }
    val resendButtonEnabled: LiveData<Boolean> = MediatorLiveData<Boolean>().apply {
        addSource(codeSendAgain) { this.value = it != 0L }
    }
    init {
        startResendTimer(System.currentTimeMillis() + 10)
    }

    fun navigateBack(view: View) {
        view.findNavController().popBackStack()
    }
}
