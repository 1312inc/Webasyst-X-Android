package com.webasyst.x.util

import android.app.Activity
import android.content.ContextWrapper
import android.view.View
import androidx.navigation.NavController
import androidx.navigation.findNavController
import com.webasyst.x.R

fun View.getActivity(): Activity? {
    var context = this.context
    while (context is ContextWrapper) {
        if (context is Activity) {
            return context
        }
        context = context.baseContext
    }
    return null
}

/**
 * To be used with views not in NavHostFragment's hierarchy (eg. Nav Drawer)
 */
fun View.findRootNavController(): NavController =
    getActivity()!!.findNavController(R.id.navRoot)
