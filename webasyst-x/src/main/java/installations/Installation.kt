package com.webasyst.x.installations

data class Installation(
    override val id: String,
    val name: String,
    val domain: String,
    val rawUrl: String,
) : com.webasyst.api.Installation {
    override val urlBase
        get() = rawUrl
    val url = rawUrl.replace(Regex("^https?://"), "")
    val isInsecure = rawUrl.startsWith("http://")

    constructor(installation: com.webasyst.waid.Installation) : this(
        id = installation.id,
        name = installation.domain,
        domain = installation.domain,
        rawUrl = installation.url,
    )
}
