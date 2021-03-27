package com.webasyst.x.installations

import com.webasyst.api.webasyst.InstallationInfo

data class Installation(
    override val id: String,
    val name: String,
    val domain: String,
    val rawUrl: String,
    val icon: Icon,
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
        icon = Icon.AutoIcon(installation.domain.firstOrNull()?.toString() ?: " "),
    )

    sealed class Icon {
        abstract val text: String
        abstract val twoLine: Boolean

        class AutoIcon(override val text: String) : Icon() {
            override val twoLine: Boolean = text.length > 3
        }

        class GradientIcon(
            override val text: String,
            override val twoLine: Boolean,
            val textColor: String,
            val from: String,
            val to: String,
            val angle: Int,
            ) : Icon()

        companion object {
            operator fun invoke(info: InstallationInfo): Icon {
                val logo = info.logo
                return if (logo == null) {
                    AutoIcon(info.name.split(" ")
                        .map { it.first().toUpperCase() }
                        .filterIndexed { index, _ -> index < 4 }
                        .joinToString(separator = "")
                    )
                } else {
                    GradientIcon(
                        logo.text.value,
                        logo.twoLines,
                        logo.text.color,
                        logo.gradient.from,
                        logo.gradient.to,
                        logo.gradient.angle.toIntOrNull() ?: 0
                    )
                }
            }
        }
    }
}
