package com.webasyst.x.common

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Matrix
import android.media.ExifInterface
import android.net.Uri
import android.os.Build
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import java.io.IOException
import java.io.InputStream
import kotlin.math.max


@Throws(IOException::class)
fun openAndRotateBitmap(
    context: Context,
    imageUri: Uri?
): ByteArray? {
    if (imageUri == null) return null
    return context.contentResolver.openInputStream(imageUri)?.use { it ->
        val imageByteArray = it.readBytes()
        val ei = if (Build.VERSION.SDK_INT > 23) ExifInterface(ByteArrayInputStream(imageByteArray))
        else if (imageUri.path != null) ExifInterface(imageUri.path!!)
        else return@use null
        val eiOrientAttr = ei.getAttributeInt(
            ExifInterface.TAG_ORIENTATION,
            ExifInterface.ORIENTATION_NORMAL
        )
        if (eiOrientAttr == ExifInterface.ORIENTATION_NORMAL) return@use imageByteArray

        BitmapFactory.decodeStream(
            ByteArrayInputStream(imageByteArray),
            null,
            BitmapFactory.Options()
        )?.let {
            val bitmap = when (eiOrientAttr) {
                ExifInterface.ORIENTATION_ROTATE_90 -> rotateImage(it,90)
                ExifInterface.ORIENTATION_ROTATE_180 -> rotateImage(it,180)
                ExifInterface.ORIENTATION_ROTATE_270 -> rotateImage(it,270)
                else -> it
            }
            val outputStream = ByteArrayOutputStream()
            if (bitmap.compress(Bitmap.CompressFormat.JPEG, 85, outputStream)){
                bitmap.recycle()
                val byteArr = outputStream.toByteArray()
                byteArr
            } else {
                bitmap.recycle()
                imageByteArray
            }
        }
    }
}

private fun rotateImage(img: Bitmap, degree: Int): Bitmap {
    val matrix = Matrix()
    matrix.postRotate(degree.toFloat())
    val rotatedImg = Bitmap.createBitmap(img, 0, 0, img.width, img.height, matrix, true)
    img.recycle()
    return rotatedImg
}


@Throws(IOException::class)
fun resizePicture(
    imageInputStream: InputStream,
    newWidth: Int,
    newHeight: Int
): ByteArray {
    val imageByteArray = imageInputStream.readBytes()
    BitmapFactory.decodeStream(
        ByteArrayInputStream(imageByteArray),
        null,
        BitmapFactory.Options()
    )?.let {
        val (result, bitmap) = getResizedBitmapWhenBigger(it, newWidth, newHeight)
        if (result) {
            val outputStream = ByteArrayOutputStream()
            if (bitmap.compress(Bitmap.CompressFormat.JPEG, 85, outputStream)) {
                bitmap.recycle()
                val byteArr = outputStream.toByteArray()
                return byteArr
            } else {
                bitmap.recycle()
            }
        }
    }
    return ByteArrayInputStream(imageByteArray).readBytes()
}

@Throws(IOException::class)
fun resizeAndRotatePicture(
    imageInputStream: InputStream,
    newWidth: Int,
    newHeight: Int
): ByteArray {
    val imageByteArray = imageInputStream.readBytes()

    val ei = ExifInterface(ByteArrayInputStream(imageByteArray))
    val eiOrientAttr = ei.getAttributeInt(
        ExifInterface.TAG_ORIENTATION,
        ExifInterface.ORIENTATION_NORMAL
    )

    BitmapFactory.decodeStream(
        ByteArrayInputStream(imageByteArray),
        null,
        BitmapFactory.Options()
    )?.let {
        val (resultResize, bitmap) = getResizedBitmapWhenBigger(it, newWidth, newHeight)
        val (resultRotate, rotatedBitmap) = if (eiOrientAttr != ExifInterface.ORIENTATION_NORMAL)
            when (eiOrientAttr) {
                ExifInterface.ORIENTATION_ROTATE_90 -> true to rotateImage(bitmap,90)
                ExifInterface.ORIENTATION_ROTATE_180 -> true to rotateImage(bitmap,180)
                ExifInterface.ORIENTATION_ROTATE_270 -> true to rotateImage(bitmap,270)
                else -> false to bitmap
            }
        else false to bitmap

        if (resultResize || resultRotate) {
            if (resultRotate) bitmap.recycle()
            val outputStream = ByteArrayOutputStream()
            if (rotatedBitmap.compress(Bitmap.CompressFormat.JPEG, 85, outputStream)) {
                rotatedBitmap.recycle()
                return outputStream.toByteArray()
            } else {
                rotatedBitmap.recycle()
            }
        } else bitmap.recycle()
    }
    return ByteArrayInputStream(imageByteArray).readBytes()
}

private fun getResizedBitmapWhenBigger(bm: Bitmap, newWidth: Int, newHeight: Int)
: Pair<Boolean, Bitmap> {
    val width = bm.width
    val height = bm.height
    if (width <= newWidth && height <= newHeight) return false to bm
    val scaleWidth = newWidth.toFloat() / width
    val scaleHeight = newHeight.toFloat() / height
    val scale = max(scaleWidth, scaleHeight)
    val matrix = Matrix()
    matrix.postScale(scale, scale)
    val resizedBitmap = Bitmap.createBitmap(bm, 0, 0, width, height, matrix, false)
    bm.recycle()
    return true to resizedBitmap
}
