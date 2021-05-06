package com.webasyst.x.installations

import com.webasyst.api.webasyst.InstallationInfo
import com.webasyst.waid.CloudSignupResponse
import java.io.Serializable
import java.util.Comparator

data class Installation(
    override val id: String,
    val name: String,
    val domain: String,
    val rawUrl: String,
    val icon: Icon,
) : com.webasyst.api.Installation, Serializable {
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

    constructor(cloudSignupResponse: CloudSignupResponse) : this(
        id = cloudSignupResponse.id,
        name = cloudSignupResponse.domain,
        domain = cloudSignupResponse.domain,
        rawUrl = cloudSignupResponse.url,
        icon = Icon.AutoIcon(cloudSignupResponse.domain.firstOrNull()?.toString() ?: " ")
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

        class ImageIcon(
            thumbs: Map<ResolutionKey, String>
        ) : Icon() {
            private val thumbs = thumbs.toSortedMap()
            override val text = ""
            override val twoLine = false

            fun getThumb(resolution: Int): String {
                return thumbs.getOrDefault(thumbs.keys.lastOrNull {
                    it.resolution <= resolution
                } ?: ResolutionKey(0, 0), thumbs.values.firstOrNull()) ?: ""
            }

            data class ResolutionKey(val resolution: Int, val scale: Int) : Comparable<ResolutionKey> {
                private val physicalResolution
                    get() = resolution * scale

                override fun compareTo(other: ResolutionKey): Int =
                    compare(this, other)

                companion object : Comparator<ResolutionKey> by compareBy( { it.physicalResolution }, { it.scale } )
            }

            companion object {
                private const val RE = """(\d+)x\d+(?:@(\d+)x)?"""
                val regex = Regex(RE)

                operator fun invoke(image: InstallationInfo.Logo.Image): ImageIcon {
                    return ImageIcon(
                        image
                            .thumbs
                            .map { (k, v) ->
                                val g = regex.find(k)!!.groupValues
                                ResolutionKey(g[1].toInt(), g.elementAtOrNull(2)?.toIntOrNull() ?: 1) to v.url
                            }
                            .toMap()
                    )
                }
            }
        }

        companion object {
            operator fun invoke(info: InstallationInfo): Icon {
                val logo = info.logo
                return if (logo == null) {
                    AutoIcon(info.name.split(" ")
                        .map { it.first().toUpperCase() }
                        .filterIndexed { index, _ -> index < 4 }
                        .joinToString(separator = "")
                    )
                } else if (logo.mode == InstallationInfo.Logo.LOGO_MODE_IMAGE && logo.image != null) {
                    ImageIcon(logo.image!!)
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
