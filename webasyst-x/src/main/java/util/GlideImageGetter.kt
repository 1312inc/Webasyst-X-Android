package com.webasyst.x.util

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Drawable
import android.text.Html.ImageGetter
import android.widget.TextView
import com.bumptech.glide.Glide
import com.bumptech.glide.request.Request
import com.bumptech.glide.request.target.SizeReadyCallback
import com.bumptech.glide.request.target.Target
import com.bumptech.glide.request.transition.Transition

class GlideImageGetter(private val context: Context, private val textView: TextView) : ImageGetter {
    override fun getDrawable(url: String): Drawable {
        val drawable = BitmapDrawablePlaceholder()
        Glide.with(context)
            .asBitmap()
            .load(url)
            .into(drawable)
        return drawable
    }

    private inner class BitmapDrawablePlaceholder : BitmapDrawable(
        context.resources, Bitmap.createBitmap(1, 1, Bitmap.Config.ARGB_8888)
    ), Target<Bitmap> {
        private var drawable: Drawable? = null
        override fun draw(canvas: Canvas) {
            if (drawable != null) {
                drawable!!.draw(canvas)
            }
        }

        private fun setDrawable(drawable: Drawable?) {
            if (null == drawable) return

            this.drawable = drawable
            val drawableWidth = drawable.intrinsicWidth
            val drawableHeight = drawable.intrinsicHeight
            val maxWidth = textView.measuredWidth
            if (drawableWidth > maxWidth) {
                val calculatedHeight = maxWidth * drawableHeight / drawableWidth
                drawable.setBounds(0, 0, maxWidth, calculatedHeight)
                setBounds(0, 0, maxWidth, calculatedHeight)
            } else {
                drawable.setBounds(0, 0, drawableWidth, drawableHeight)
                setBounds(0, 0, drawableWidth, drawableHeight)
            }
            textView.text = textView.text
        }

        override fun onLoadStarted(placeholderDrawable: Drawable?) = setDrawable(placeholderDrawable)

        override fun onLoadFailed(errorDrawable: Drawable?) = setDrawable(errorDrawable)

        override fun onLoadCleared(placeholderDrawable: Drawable?) = setDrawable(placeholderDrawable)

        override fun onResourceReady(bitmap: Bitmap, transition: Transition<in Bitmap>?) =
            setDrawable(BitmapDrawable(context.resources, bitmap))

        override fun getSize(cb: SizeReadyCallback) {
            textView.post { cb.onSizeReady(textView.width, textView.height) }
        }

        override fun removeCallback(cb: SizeReadyCallback) = Unit
        override fun setRequest(request: Request?) = Unit
        override fun getRequest(): Request? = null

        override fun onStart() = Unit
        override fun onStop() = Unit
        override fun onDestroy() = Unit
    }
}
