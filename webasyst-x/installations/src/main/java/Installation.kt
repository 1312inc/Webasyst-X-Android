package com.webasyst.x.installations

import android.util.Log
import com.webasyst.api.webasyst.InstallationInfo
import com.webasyst.waid.CloudSignupResponse
import com.webasyst.x.common.InstallationInterface
import java.io.Serializable
import java.util.Calendar
import java.util.Comparator
import java.util.SortedMap

data class Installation(
    override val id: String,
    override val name: String,
    override val domain: String,
    /** Full installation URL */
    override val url: String,
    val icon: Icon,
    override val cloudExpireDate: Calendar?,
) : com.webasyst.api.Installation, Serializable, InstallationInterface {
    override val urlBase
        get() = url
    /** Human-readable installation URL (without schema) */
    val displayUrl = url.replace(Regex("^https?://"), "")
    val isInsecure = url.startsWith("http://")

    override val logo: InstallationInfo.Logo?
        get() = icon.toApi()

    constructor(installation: com.webasyst.waid.Installation) : this(
        id = installation.id,
        name = installation.domain,
        domain = installation.domain,
        url = installation.url,
        icon = Icon.AutoIcon(installation.domain.firstOrNull()?.toString() ?: " "),
        cloudExpireDate = installation.cloudExpireDate,
    )

    constructor(cloudSignupResponse: CloudSignupResponse) : this(
        id = cloudSignupResponse.id,
        name = cloudSignupResponse.domain,
        domain = cloudSignupResponse.domain,
        url = cloudSignupResponse.url,
        icon = Icon.AutoIcon(cloudSignupResponse.domain.firstOrNull()?.toString() ?: " "),
        cloudExpireDate = null,
    )

    constructor(installation: InstallationInterface) : this(
        id = installation.id,
        name = installation.name,
        domain = installation.domain,
        url = installation.url,
        icon = Icon(installation),
        cloudExpireDate = installation.cloudExpireDate,
    )

    sealed class Icon {
        abstract val text: String
        abstract val twoLine: Boolean

        data class AutoIcon(override val text: String) : Icon() {
            override val twoLine: Boolean = text.length > 3
        }

        data class GradientIcon(
            override val text: String,
            override val twoLine: Boolean,
            val textColor: String,
            val from: String,
            val to: String,
            val angle: Int,
        ) : Icon()

        data class ImageIcon(
            internal val thumbs: SortedMap<ResolutionKey, String>
        ) : Icon() {
            override val text = ""
            override val twoLine = false

            fun getThumb(resolution: Int): String {
                val key = thumbs.keys.firstOrNull()
//                    thumbs.keys.lastOrNull {
//                        it.resolution <= resolution
//                    } ?: return ""
                return thumbs[key] ?: ""
            }

            data class ResolutionKey(val resolution: Int, val scale: Int) : Comparable<ResolutionKey> {
                private val physicalResolution
                    get() = resolution * scale

                override fun compareTo(other: ResolutionKey): Int =
                    compare(this, other)

                override fun toString() = "${resolution}x${resolution}@${scale}x"

                companion object : Comparator<ResolutionKey> by compareBy( { it.physicalResolution }, { it.scale } )
            }

            companion object {
                private const val RE = """(\d+)x\d+(?:@(\d+)x)?"""
                val regex = Regex(RE)

                operator fun invoke(image: InstallationInfo.Logo.Image): ImageIcon {
                    return ImageIcon(
                        image
                            .thumbs
                            .mapNotNull { (k, v) ->
                                try {
                                    val g = regex.find(k)!!.groupValues
                                    ResolutionKey(
                                        g[1].toInt(),
                                        g.elementAtOrNull(2)?.toIntOrNull() ?: 1
                                    ) to v.url
                                } catch (e: Throwable) {
                                    Log.w("installation_api", "Caught an exception while parsing $k", e)
                                    null
                                }
                            }
                            .toMap()
                            .toSortedMap()
                    )
                }
            }
        }

        fun toApi(): InstallationInfo.Logo? =
            when (this) {
                is AutoIcon -> null
                is GradientIcon -> InstallationInfo.Logo(
                    mode = InstallationInfo.Logo.LOGO_MODE_GRADIENT,
                    text = InstallationInfo.Logo.Text(
                        value = this.text,
                        color = this.textColor,
                        defaultValue = "",
                        defaultColor = "",
                        formattedValue = "",
                    ),
                    twoLines = this.twoLine,
                    gradient = InstallationInfo.Logo.Gradient(
                        angle = this.angle.toString(),
                        from = this.from,
                        to = this.to,
                    ),
                    image = null,
                )
                is ImageIcon -> InstallationInfo.Logo(
                    mode = InstallationInfo.Logo.LOGO_MODE_IMAGE,
                    text = InstallationInfo.Logo.Text(value = this.text, "", "", "", ""),
                    twoLines = this.twoLine,
                    gradient = InstallationInfo.Logo.Gradient(
                        angle = "0",
                        from = "#000000",
                        to = "#000000",
                    ),
                    image = InstallationInfo.Logo.Image(
                        original = null,
                        thumbs = this.thumbs
                            .map { (k, v) ->
                                k.toString() to InstallationInfo.Logo.Image.Thumb(
                                    path = v, url = v, ts = 0
                                )
                            }
                            .toMap()
                    )
                )
            }

        companion object {
            operator fun invoke(info: InstallationInterface): Icon {
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
