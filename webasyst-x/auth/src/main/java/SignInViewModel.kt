package com.webasyst.x.auth

import android.app.Activity
import android.app.Application
import android.content.Intent
import android.os.Build
import android.view.View
import android.view.inputmethod.InputMethodManager
import androidx.annotation.DrawableRes
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MediatorLiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.google.i18n.phonenumbers.NumberParseException
import com.google.i18n.phonenumbers.PhoneNumberUtil
import com.google.i18n.phonenumbers.Phonenumber
import com.webasyst.auth.WebasystAuthHelper
import com.webasyst.auth.WebasystAuthStateStore
import com.webasyst.waid.HeadlessCodeRequestResult
import com.webasyst.x.common.XComponentProvider
import com.webasyst.x.common.getActivity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class SignInViewModel(
    application: Application,
    private val navigator: Navigator,
) : AndroidViewModel(application) {
    private val xComponentProvider = application as XComponentProvider
    open val welcomeTitle: String = application.getString(R.string.intro_welcome_title)
    open val welcomeText: String = application.getString(R.string.intro_welcome_text)
    @DrawableRes
    open val appLogoRes = R.drawable.img_appicon_example

    private val phoneNumberUtil by lazy { PhoneNumberUtil.getInstance() }
    private val waidClient by lazy { (getApplication() as XComponentProvider).getWAIDClient() }
    private val authStateStore by lazy { WebasystAuthStateStore.getInstance(getApplication()) }

    val phone = MutableLiveData("+7")

    private val _phoneError = MutableLiveData<Int?>(null)
    val phoneError: LiveData<Int?> get() = _phoneError

    private val _submitEnabled = MutableLiveData(true)
    val submitEnabled: LiveData<Boolean> get() = _submitEnabled

    val code = MutableLiveData("")

    private val _codeError = MutableLiveData<Int?>(null)
    val codeError: LiveData<Int?> get() = _codeError

    private val submittingCode = MutableLiveData(false)
    val submitCodeEnabled: LiveData<Boolean> = MediatorLiveData<Boolean>().apply {
        var notBlank = false
        var submitting = false

        fun update() { value = notBlank && !submitting }

        addSource(code) { notBlank = it.isNotBlank(); update() }
        addSource(submittingCode) { submitting = it; update() }
    }

    private val codeResponse = MutableStateFlow<HeadlessCodeRequestResult?>(null)

    private var submitJob: Job? = null
    fun onSubmit(view: View) {
        submitJob?.cancel()
        submitJob = viewModelScope.launch(Dispatchers.Default) { submit(phone.value ?: "") }
    }
    suspend fun submit(phoneValue: String) {
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

            val application = getApplication<Application>()
            val response = withContext(Dispatchers.IO) {
                waidClient.postAuthCode(
                    clientId = xComponentProvider.clientId(),
                    scope = xComponentProvider.webasystScope(),
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
            startResendTimer(response.nextRequestAllowedAt * 1000L)

            withContext(Dispatchers.Main) {
                navigator.navigateFromPhoneInputToCodeInput()
            }
        } catch (e: Throwable) {
            _phoneError.postValue(R.string.generic_error_retry_later)
        } finally {
            _submitEnabled.postValue(true)
        }
    }

    var submitCodeJob: Job? = null
    fun onSubmitCode(view: View) {
        submitCodeJob?.cancel()
        submitCodeJob = viewModelScope.launch(Dispatchers.IO) { submitCode(view) }
    }
    private suspend fun submitCode(view: View) {
        try {
            submittingCode.postValue(true)
            _codeError.postValue(null)
            val cr = codeResponse.value ?: return
            val res = waidClient.postHeadlessToken(
                clientId = xComponentProvider.clientId(),
                codeVerifier = cr.codeChallenge.password,
                code = code.value ?: "",
            )

            val tokenResponse = waidClient.tokenResponseFromHeadlessRequest(res)

            val state = authStateStore.updateAfterTokenResponse(tokenResponse, null)

            if (state.isAuthorized) {
                view.getActivity()?.also { activity ->
                    (activity.getSystemService(Activity.INPUT_METHOD_SERVICE) as InputMethodManager)
                        .hideSoftInputFromWindow(view.windowToken, 0)
                }
            }
        } catch (e: Throwable) {
            _codeError.postValue(R.string.sign_in_err_code_invalid)
        } finally {
            submittingCode.postValue(false)
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
        var now = System.currentTimeMillis()
        withContext(Dispatchers.Main) {
            while (tt > now) {
                now = System.currentTimeMillis()
                _codeSendTime.postValue((tt - now) / 1000)
                delay(1000)
            }
            _codeSendTime.postValue(0)
        }
    }
    val resendButtonEnabled: LiveData<Boolean> = MediatorLiveData<Boolean>().apply {
        addSource(codeSendAgain) { this.value = it != 0L }
    }

    fun onPhoneSignIn(view: View) {
        val intent = Intent(view.context, SignInActivity::class.java)
        view.context.startActivity(intent)
    }

    fun onSignIn(view: View) {
        view.getActivity()?.let { activity ->
            activity.javaClass.let { activityClass ->
                val authHelper = WebasystAuthHelper(activity)
                authHelper.signIn(activityClass)
            }
        }
    }

    fun navigateBackFromPhoneInput(view: View) {
        view.getActivity()?.onBackPressed()
    }

    fun navigateBackFromCodeInput(view: View) {
        navigator.popBackStack()
    }

    interface Navigator {
        fun navigateFromPhoneInputToCodeInput()
        fun popBackStack()
    }

    class Factory(
        private val navigator: Navigator,
        private val application: Application,
    ) : ViewModelProvider.Factory {
        override fun <T : ViewModel?> create(modelClass: Class<T>): T {
            if (modelClass == SignInViewModel::class.java) {
                return SignInViewModel(application, navigator) as T
            } else {
                throw IllegalArgumentException()
            }
        }
    }
}
