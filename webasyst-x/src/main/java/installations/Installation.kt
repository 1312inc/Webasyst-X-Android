package com.webasyst.x.installations

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
}
