package com.webasyst.x.barcode.barcode

import android.app.Application
import android.util.Log
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.core.content.ContextCompat
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.google.mlkit.vision.barcode.common.Barcode
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.util.concurrent.ExecutionException

class BarcodeViewModel(application: Application): AndroidViewModel(application) {

    private var _cameraProviderStateFlow = MutableStateFlow<ProcessCameraProvider?>(null)
    val cameraProviderStateFlow: StateFlow<ProcessCameraProvider?>
        get() {
            if (_cameraProviderStateFlow.value == null) {
                val cameraProviderFuture = ProcessCameraProvider.getInstance(getApplication())
                cameraProviderFuture.addListener(
                    {
                        try {
                            _cameraProviderStateFlow.value = cameraProviderFuture.get()
                        } catch (e: ExecutionException) {
                            Log.e(TAG, "Unhandled exception", e)
                        } catch (e: InterruptedException) {
                            Log.e(TAG, "Unhandled exception", e)
                        }
                    },
                    ContextCompat.getMainExecutor(getApplication())
                )
            }
            return _cameraProviderStateFlow.asStateFlow()
    }

    val workflowState = MutableLiveData<WorkflowState>()
    val detectedBarcode = MutableLiveData<Barcode>()
    var isCameraLive = false

    //State set of the application workflow
    enum class WorkflowState {
        DETECTING,
        DETECTED,
        CONFIRMING
    }

    class Factory(
        private val application: Application
    ) : ViewModelProvider.Factory {
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            if (modelClass == BarcodeViewModel::class.java) {
                return BarcodeViewModel(application) as T
            } else {
                throw IllegalArgumentException()
            }
        }
    }

    companion object{
        const val TAG = "BARCD"
    }
}
