package com.webasyst.x.common

import android.app.Activity
import android.content.ContextWrapper
import android.view.View
import androidx.navigation.NavController
import androidx.navigation.findNavController
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout

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

/**
 * Configures [SwipeRefreshLayout] to use Webasyst brand colors
 */
fun SwipeRefreshLayout.setWebasystBrandColors() = setColorSchemeResources(
    R.color.webasyst_logo_a,
    R.color.webasyst_logo_s1,
    R.color.webasyst_logo_y,
    R.color.webasyst_logo_s2,
    R.color.webasyst_logo_t,
)
