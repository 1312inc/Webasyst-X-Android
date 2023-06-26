package com.webasyst.x.profile_editor

import android.app.Application
import android.content.Context
import android.net.Uri
import android.telephony.PhoneNumberUtils
import android.view.View
import androidx.annotation.StringRes
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.webasyst.x.common.CustomPhoneNumberFormattingTextWatcher
import com.webasyst.x.common.resizeAndRotatePicture
import com.webasyst.waid.UpdateUserInfo
import com.webasyst.x.common.XComponentProvider
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlinx.coroutines.withTimeout
import java.io.FileInputStream
import java.util.*

class ProfileEditorViewModel(app: Application) : AndroidViewModel(app) {
    private val waidClient by lazy { (getApplication() as XComponentProvider).getWAIDClient() }
    private val userInfoStore by lazy { (getApplication() as XComponentProvider).userInfoStore() }

    var profileEditor: ProfileEditor? = null
    private var updateUserpicJob: Job? = null
    private var updateProfileJob: Job? = null

    private val _updatingUserpic = MutableStateFlow(false)
    val updatingUserpic: StateFlow<Boolean> get() = _updatingUserpic

    private val _updatingProfile = MutableStateFlow(false)
    val updatingProfile: StateFlow<Boolean> get() = _updatingProfile

    val userPic: Flow<String> = userInfoStore
        .userInfo
        .filterNotNull()
        .map { it.userpicOriginalCrop }

    val userpicSet: StateFlow<Boolean> = userInfoStore
        .userInfo
        .filterNotNull()
        .map { it.usrpicUploaded }
        .stateIn(viewModelScope, SharingStarted.Eagerly, true)

    val firstName = MutableStateFlow("")
    val lastName = MutableStateFlow("")
    val email = MutableStateFlow("")
    val emailError = MutableStateFlow<Int?>(null)
    val phone = MutableStateFlow("")

    var isEmptyUser: Boolean = false
    var emailList = mutableListOf<String>()
    var phoneList = mutableListOf<String>()

    init {
        viewModelScope.launch {
            userInfoStore
                .userInfo.first { it != null }.let{ userInfo ->

                    firstName.value = userInfo!!.firstName
                    lastName.value = userInfo.lastName

                    emailList = userInfo.email.map {it.value}.toMutableList()
                    email.value = userInfo.email.firstOrNull()?.value ?: ""

                    phoneList = userInfo.phone.map {it.value}.toMutableList()
                    userInfo.phone.firstOrNull()?.value?.let {
                        val phoneNum = if (!it.startsWith("+")) {
                            "+$it"
                        } else it
                        phone.value = PhoneNumberUtils.formatNumber(
                            phoneNum,
                            Locale.getDefault().isO3Country
                        ) ?: ""
                    }
                }
        }
    }

    fun clearEmailError(){
        emailError.value = null
    }

    fun onUserpicSelected(context: Context, uri: Uri, done: (suspend () -> Unit)?) {
        updateUserpicJob?.cancel()
        updateUserpicJob = viewModelScope.launch(Dispatchers.IO) {
            try {
                _updatingUserpic.value = true

                withTimeout(15000) {
                    val data = FileInputStream(
                        context
                            .contentResolver
                            .openFileDescriptor(uri, "r")!!
                            .fileDescriptor
                    )
                        .use {
                            resizeAndRotatePicture(it, IMAGE_SIZE_PX, IMAGE_SIZE_PX)
                        }

                    val result = waidClient.updateUserpic(data)
                    if (result.isSuccess()) {
                        val userInfo = userInfoStore.userInfo.value ?: return@withTimeout
                        userInfoStore.setUserInfo(
                            userInfo.copy(
                                userpic = result.getSuccess().userpic,
                                userpicOriginalCrop = result.getSuccess().userpicOriginalCrop
                            )
                        )
                    } else {
                        throw result.getFailureCause()
                    }
                }
                if (done != null) {
                    done()
                }
            } catch (e: Throwable) {
                profileEditor?.handleException(e)
            } finally {
                _updatingUserpic.value = false
            }
        }
    }

    fun onSetUserPic(view: View) {
        profileEditor?.onSetUserpic(view)
    }

    fun onDeleteUserPic(view: View) {
        updateUserpicJob?.cancel()
        updateUserpicJob = viewModelScope.launch(Dispatchers.IO) {
            _updatingUserpic.value = true

            try {
                withTimeout(15000) {
                    val deleteResult = waidClient.deleteUserpic()
                    if (deleteResult.isFailure()) {
                        throw deleteResult.getFailureCause()
                    }

                    val result = waidClient.getUserInfo()
                    if (result.isSuccess()) {
                        userInfoStore.setUserInfo(result.getSuccess())
                        profileEditor?.toast(R.string.profile_editor_profile_updated)
                    } else {
                        throw result.getFailureCause()
                    }
                }
            } catch (e: Throwable) {
                profileEditor?.handleException(e)
            } finally {
                _updatingUserpic.value = false
            }
        }
    }

    fun onSave(view: View) {
        if (!isEmptyUser && email.value.isNotBlank() && !isValidEmail(email.value)) {
            emailError.value = R.string.profile_editor_email_wrong
            return
        }
        onSave(UpdateUserInfo(
            firstName = firstName.value,
            lastName = lastName.value,
            email = if (isEmptyUser) null else emailList.apply{
                if (email.value.isNotBlank())
                    if (isEmpty()) add(email.value) else set(0, email.value)
                else if (isNotEmpty()) set(0, "")
            },
            phone = if (isEmptyUser) null else phoneList.apply{
                if (phone.value.isNotBlank())
                    if (isEmpty()) add(phone.value) else set(0, phone.value)
                else if (isNotEmpty()) set(0, "+")
            }
        ))
    }

    fun onSave(update: UpdateUserInfo) {
        updateProfileJob?.cancel()
        updateProfileJob = viewModelScope.launch(Dispatchers.Main) {
            try {
                _updatingProfile.value = true

                withTimeout(15000) {
                    val result = withContext(Dispatchers.IO) { waidClient.updateUserInfo(update) }
                    if (result.isSuccess()) {
                        userInfoStore.setUserInfo(result.getSuccess())
                        profileEditor?.toast(R.string.profile_editor_profile_updated)
                        profileEditor?.popBack()
                    } else {
                        throw result.getFailureCause()
                    }
                }
            } catch (e: Throwable) {
                profileEditor?.handleException(e)
            } finally {
                _updatingProfile.value = false
            }
        }
    }

    private fun isValidEmail(email: String?): Boolean{
        return email?.contains(
            Regex("^([A-Za-z0-9_-]+\\.)*[A-Za-z0-9_-]+@[A-Za-z0-9_-]+(\\.[A-Za-z0-9_-]+)*\\.[A-Za-z]{2,6}\$")
        ) ?: false
    }

    interface ProfileEditor {
        fun onSetUserpic(view: View)
        fun onUpdateUserInfo()
        fun handleException(e: Throwable)
        fun toast(@StringRes resId: Int)
        fun popBack()
    }

    class Factory(
        private val application: Application,
    ) : ViewModelProvider.Factory {
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            if (modelClass == ProfileEditorViewModel::class.java) {
                return ProfileEditorViewModel(application) as T
            } else {
                throw IllegalArgumentException()
            }
        }
    }

    companion object {
        @JvmStatic
        fun phoneWatcher() = CustomPhoneNumberFormattingTextWatcher()
    }
}

private const val IMAGE_SIZE_PX = 1312
