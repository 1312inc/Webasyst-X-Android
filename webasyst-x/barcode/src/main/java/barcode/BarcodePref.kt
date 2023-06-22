package com.webasyst.x.barcode.barcode

import android.content.Context
import android.content.res.Configuration
import android.graphics.RectF
import com.google.mlkit.vision.barcode.common.Barcode

object BarcodePref {
    const val BOX_WIDTH_PERCENT_OF_SCREEN = 75
    const val BOX_HEIGHT_PERCENT_OF_SCREEN = 35
    const val CODE_WIDTH_PERCENT_OF_BOX = 40

    fun isPortraitMode(context: Context): Boolean =
        context.resources.configuration.orientation == Configuration.ORIENTATION_PORTRAIT

    fun getBarcodeReticleBox(overlay: GraphicOverlay): RectF {
        val overlayWidth = overlay.width.toFloat()
        val overlayHeight = overlay.height.toFloat()
        val boxWidth = overlayWidth * BOX_WIDTH_PERCENT_OF_SCREEN / 100
        val boxHeight = overlayHeight * BOX_HEIGHT_PERCENT_OF_SCREEN / 100
        val cx = overlayWidth / 2
        val cy = overlayHeight / 2
        return RectF(cx - boxWidth / 2, cy - boxHeight / 2, cx + boxWidth / 2, cy + boxHeight / 2)
    }

    fun getProgressToMeetBarcodeSizeRequirement(
        overlay: GraphicOverlay,
        barcode: Barcode
    ): Float {
        val reticleBoxWidth = getBarcodeReticleBox(overlay).width()
        val barcodeWidth = overlay.translateX(barcode.boundingBox?.width()?.toFloat() ?: 0f)
        val requiredWidth = reticleBoxWidth * CODE_WIDTH_PERCENT_OF_BOX / 100
        return (barcodeWidth / requiredWidth).coerceAtMost(1f)
    }
}
