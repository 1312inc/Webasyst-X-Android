package com.webasyst.x.barcode.barcode

import android.animation.ValueAnimator
import android.util.Log
import androidx.camera.core.ImageProxy
import com.google.mlkit.common.MlKitException
import com.google.mlkit.vision.barcode.BarcodeScanner
import com.google.mlkit.vision.barcode.BarcodeScannerOptions
import com.google.mlkit.vision.barcode.BarcodeScanning
import com.google.mlkit.vision.barcode.common.Barcode
import com.google.mlkit.vision.common.InputImage
import com.webasyst.x.barcode.barcode.BarcodeViewModel.WorkflowState.*

class BarcodeScannerProcessor(
    private val graphicOverlay: GraphicOverlay,
    private val workflowModel: BarcodeViewModel
) {
    private var barcodeScanner: BarcodeScanner = BarcodeScanning.getClient(
        BarcodeScannerOptions.Builder()
        .setBarcodeFormats(Barcode.FORMAT_QR_CODE)
        .build()
    )
    private val cameraReticleAnimator: CameraReticleAnimator = CameraReticleAnimator(graphicOverlay)

    fun stop() {
        barcodeScanner.close()
    }

    @androidx.camera.core.ExperimentalGetImage
    fun processImageProxy(imageProxy: ImageProxy) {

        val mediaImage = imageProxy.image
        if (mediaImage != null) {
            val inputImage =
                InputImage.fromMediaImage(mediaImage, imageProxy.imageInfo.rotationDegrees)
            try {
                barcodeScanner.process(inputImage)
                    .addOnSuccessListener { barcodes ->
                        onSuccess(barcodes, graphicOverlay, inputImage)
                    }
                    .addOnFailureListener {
                        Log.e(TAG, "Barcode scanner failure ${it.message}")
                    }.addOnCompleteListener {
                        imageProxy.close()
                    }
            } catch (e: MlKitException){
                Log.e(BarcodeViewModel.TAG, "Barcode scanner error ${e.message}")
            }
        }
    }

    private fun onSuccess(
        results: List<Barcode>,
        graphicOverlay: GraphicOverlay,
        inputImage: InputImage
    ) {
        if (!workflowModel.isCameraLive) return
        // Picks the barcode, if exists, that covers the center of graphic overlay.
        graphicOverlay.setPreviewSize(inputImage.width, inputImage.height)

        val barcodeInCenter = results.firstOrNull { barcode ->
            val boundingBox = barcode.boundingBox ?: return@firstOrNull false
            val box = graphicOverlay.translateRect(boundingBox)
            box.contains(graphicOverlay.width / 2f, graphicOverlay.height / 2f)
        }

        graphicOverlay.clear()
        if (barcodeInCenter == null) {
            cameraReticleAnimator.start()
            graphicOverlay.add(BarcodeReticleGraphic(graphicOverlay, cameraReticleAnimator))
            workflowModel.workflowState.value = DETECTING
        } else {
            cameraReticleAnimator.cancel()
            val sizeProgress = BarcodePref.getProgressToMeetBarcodeSizeRequirement(
                graphicOverlay,
                barcodeInCenter
            )
            if (sizeProgress < 1) {
                // Barcode in the camera view is too small, so prompt user to move camera closer.
                graphicOverlay.add(BarcodeConfirmingGraphic(graphicOverlay, barcodeInCenter))
                workflowModel.workflowState.value = CONFIRMING
            } else {
                val loadingAnimator = createLoadingAnimator(graphicOverlay, barcodeInCenter)
                loadingAnimator.start()
                graphicOverlay.add(BarcodeLoadingGraphic(graphicOverlay, loadingAnimator))
                workflowModel.detectedBarcode.value = barcodeInCenter
                workflowModel.workflowState.value = DETECTED
            }
        }
        graphicOverlay.invalidate()
    }

    private fun createLoadingAnimator(graphicOverlay: GraphicOverlay, barcode: Barcode): ValueAnimator {
        val endProgress = 1.1f
        return ValueAnimator.ofFloat(0f, endProgress).apply {
            repeatCount = ValueAnimator.INFINITE
            duration = 3500
            addUpdateListener {
                graphicOverlay.invalidate()
            }
        }
    }

    companion object{
        const val TAG = "BARCD"
    }
}
