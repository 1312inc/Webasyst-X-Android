package com.webasyst.x.util

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.view.View
import java.io.File

fun calculateInSampleSize(options: BitmapFactory.Options, reqWidth: Int, reqHeight: Int): Int {
    val height = options.outHeight
    val width = options.outWidth
    var inSampleSize = 1

    if (height > reqHeight || width > reqWidth) {
        val halfHeight = height / 2
        val halfWidth = width / 2

        while (halfHeight / inSampleSize >= reqHeight &&
            halfWidth / inSampleSize >= reqWidth) {
            inSampleSize *= 2
        }
    }

    return inSampleSize
}

fun File.decodeBitmap(targetView: View): Bitmap =
    decodeBitmap(targetView.width, targetView.height)

fun File.decodeBitmap(reqWidth: Int, reqHeight: Int): Bitmap = inputStream().use {
    val options = BitmapFactory.Options()
    options.inJustDecodeBounds = true
    BitmapFactory.decodeFile(this.path, options)
    options.inSampleSize = calculateInSampleSize(options, reqWidth, reqHeight)
    options.inJustDecodeBounds = false
    return BitmapFactory.decodeFile(this.path, options)
}
