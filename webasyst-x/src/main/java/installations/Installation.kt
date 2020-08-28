package com.webasyst.x.installations

import android.view.View
import androidx.drawerlayout.widget.DrawerLayout
import com.webasyst.x.R
import com.webasyst.x.api.Installation

data class Installation(
    val id: String,
    val domain: String,
    val url: String
) {
    constructor(installation: Installation) : this(
        installation.id,
        installation.domain,
        installation.url
    )

    fun onClick(view: View) {
        view.rootView.findViewById<DrawerLayout>(R.id.drawerLayout)?.closeDrawers()
    }
}
