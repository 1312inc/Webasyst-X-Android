package com.webasyst.x.barcode

import android.content.Context
import androidx.lifecycle.LiveData
import kotlinx.coroutines.flow.MutableStateFlow

interface QrHandlerInterface{
    val qrCodeSuccess: LiveData<Boolean>
    val qrCodeHint: LiveData<String>
    val qrKonfetti: MutableStateFlow<String>

    fun handleBarcode(barcode: String, context: Context): Boolean
    suspend fun onInvalidCode(context: Context)
    fun onSetInitHint()
    fun onSetCameraPermissionDeniedHint()
}
