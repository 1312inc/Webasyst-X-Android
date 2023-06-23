package com.webasyst.x.barcode

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.content.res.ColorStateList
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.View
import android.view.ViewGroup
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.widget.AppCompatImageButton
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.children
import androidx.core.view.updateLayoutParams
import androidx.fragment.app.Fragment
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.webasyst.x.barcode.barcode.BarcodeScannerProcessor
import com.webasyst.x.barcode.barcode.BarcodeViewModel
import com.webasyst.x.barcode.barcode.BarcodeViewModel.WorkflowState.DETECTED
import com.webasyst.x.barcode.barcode.BarcodeViewModel.WorkflowState.DETECTING
import com.webasyst.x.barcode.databinding.FragSignInQrBinding
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import nl.dionsegijn.konfetti.core.Party
import nl.dionsegijn.konfetti.core.Position
import nl.dionsegijn.konfetti.core.emitter.Emitter
import org.koin.android.ext.android.get
import org.koin.core.error.NoBeanDefFoundException
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit
import kotlin.coroutines.Continuation
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine

class QRCodeFragment : Fragment(R.layout.frag_sign_in_qr) {

    private lateinit var binding: FragSignInQrBinding
    private var viewModel: QrHandlerInterface? = null

    private val barcodeViewModel: BarcodeViewModel by lazy(LazyThreadSafetyMode.NONE) {
        ViewModelProvider(
            requireActivity(),
            BarcodeViewModel.Factory(requireActivity().application)
        )[BarcodeViewModel::class.java]
    }
    private var barcodeProcessor: BarcodeScannerProcessor? = null

    @androidx.camera.core.ExperimentalGetImage
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding = FragSignInQrBinding.bind(view)
        binding.lifecycleOwner = this
        try {
            viewModel = get() as QrHandlerInterface
            binding.viewModel = viewModel

        } catch (e: NoBeanDefFoundException) {
            Log.e(BarcodeViewModel.TAG, "NoBeanDef for QrHandlerInterface")
        }

        binding.toolbar.setNavigationOnClickListener {
            activity?.onBackPressed()
            binding.toolbar.setNavigationOnClickListener(null)
        }

        ViewCompat.setOnApplyWindowInsetsListener(binding.layout) { v, windowInsets ->
            v.updateLayoutParams<ViewGroup.MarginLayoutParams> {
                val insets = windowInsets.getInsets(WindowInsetsCompat.Type.systemBars())
                bottomMargin = insets.bottom
                topMargin = insets.top
            }
            WindowInsetsCompat.CONSUMED
        }
        ViewCompat.setOnApplyWindowInsetsListener(binding.previewView) { swipeRefresh, windowInsets ->
            val insets =
                windowInsets.getInsets(WindowInsetsCompat.Type.ime() or WindowInsetsCompat.Type.systemBars())
            swipeRefresh.updateLayoutParams<ViewGroup.MarginLayoutParams> {
                bottomMargin = insets.bottom
            }
            WindowInsetsCompat.CONSUMED
        }
        if (Build.VERSION.SDK_INT >= 29) {
            binding.graphicOverlay.isForceDarkAllowed = false
            binding.toolbar.isForceDarkAllowed = false
            binding.chip.isForceDarkAllowed = false
        }
        binding.toolbar.children.forEach {
            (it as? AppCompatImageButton)?.imageTintList =
                ColorStateList.valueOf(resources.getColor(R.color.toolbar_color))
            it.refreshDrawableState()
        }

        barcodeViewModel.workflowState.observe(viewLifecycleOwner) { workflowState ->
            when (workflowState) {
                DETECTED -> {
                    stopCamera()
                    barcodeViewModel.detectedBarcode.value?.rawValue?.let { barcode ->
                        activity?.let { activity ->
                            if (viewModel?.handleBarcode(barcode, activity) == false) {
                                viewLifecycleOwner.lifecycleScope.launchWhenResumed {
                                    viewModel?.onInvalidCode(activity)
                                }
                            }
                        }
                    }
                }

                else -> {}
            }
        }
        viewModel?.qrCodeSuccess?.observe(viewLifecycleOwner) {
            if (!it && barcodeViewModel.workflowState.value == DETECTED) {
                barcodeViewModel.workflowState.value = DETECTING
                startCamera()
            }
        }

        lifecycleScope.launch {
            var dialog: AlertDialog? = null
            viewModel?.qrKonfetti?.collectLatest {
                if (it.isNotEmpty()) {
                    binding.konfetti.start(party)
                    dialog = MaterialAlertDialogBuilder(requireContext())
                        .setMessage(
                            it
                            //getString(R.string.add_webasyst_new_acc_connected_success,it)
                        )
                        .setCancelable(false).show()
                } else dialog?.dismiss()
            }
        }
    }

    @androidx.camera.core.ExperimentalGetImage
    override fun onResume() {
        super.onResume()
        startCamera()
    }

    @androidx.camera.core.ExperimentalGetImage
    private fun startCamera() {
        lifecycleScope.launch {
            activity?.run {
                if (tryRequestCameraPermission)
                    if (requestPermissionCameraForResult()) {
                        viewModel?.onSetInitHint()
                        barcodeViewModel.cameraProviderStateFlow.first { it != null }
                            ?.let { processCameraProvider ->
                                bindCameraUseCases(this@QRCodeFragment, processCameraProvider)
                            }
                    } else viewModel?.onSetCameraPermissionDeniedHint()
            }
        }
    }

    private fun stopCamera() {
        lifecycleScope.launch {
            activity?.run {
                barcodeViewModel.cameraProviderStateFlow.first { it != null }?.unbindAll()
                barcodeViewModel.isCameraLive = false
                barcodeProcessor?.stop()
            }
        }
    }

    override fun onPause() {
        super.onPause()
        barcodeViewModel.isCameraLive = false
        barcodeProcessor?.stop()
    }

    override fun onStop() {
        super.onStop()
        tryRequestCameraPermission = true
    }

    @androidx.camera.core.ExperimentalGetImage
    private fun bindCameraUseCases(
        lifecycleOwner: LifecycleOwner,
        cameraProvider: ProcessCameraProvider
    ) {
        val previewUseCase = Preview.Builder()
            .setTargetRotation(binding.previewView.display.rotation)
            .build()
            .also {
                it.setSurfaceProvider(binding.previewView.surfaceProvider)
            }

        barcodeProcessor?.stop()
        barcodeProcessor = BarcodeScannerProcessor(binding.graphicOverlay, barcodeViewModel)

        val analysisUseCase = ImageAnalysis.Builder()
            .setTargetRotation(binding.previewView.display.rotation)
            .build()
            .also {
                it.setAnalyzer(
                    Executors.newSingleThreadExecutor()//ContextCompat.getMainExecutor(this)//
                ) { imageProxy ->
                    barcodeProcessor?.processImageProxy(imageProxy)
                }
            }

        val cameraSelector = CameraSelector.DEFAULT_BACK_CAMERA

        try {
            // Unbind use cases before rebinding
            cameraProvider.unbindAll()
            // Bind use cases to camera
            barcodeViewModel.isCameraLive = true
            cameraProvider.bindToLifecycle(
                lifecycleOwner,
                cameraSelector,
                previewUseCase,
                analysisUseCase
            )
        } catch (e: Exception) {
            Log.e(BarcodeViewModel.TAG, "Use case binding failed", e)
        }
    }

    private var mCont: Continuation<Boolean>? = null
    private var tryRequestCameraPermission = true

    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted: Boolean ->
        tryRequestCameraPermission = isGranted
        mCont?.resume(isGranted)
    }

    private suspend fun Context.requestPermissionCameraForResult(): Boolean =
        suspendCoroutine { cont ->
            mCont = cont
            if (ContextCompat.checkSelfPermission(
                    this,
                    Manifest.permission.CAMERA
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                requestPermissionLauncher.launch(Manifest.permission.CAMERA)
            } else {
                cont.resume(true)
            }
        }

    companion object {
        const val TAG = "QRCodeFragment"
        val party = Party(
            speed = 0f,
            maxSpeed = 30f,
            damping = 0.9f,
            spread = 360,
            colors = listOf(0xfce18a, 0xff726d, 0xf4306d, 0xb48def),
            emitter = Emitter(duration = 100, TimeUnit.MILLISECONDS).max(100),
            position = Position.Relative(0.5, 0.3)
        )
    }
}
