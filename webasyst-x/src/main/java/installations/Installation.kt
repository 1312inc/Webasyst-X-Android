package com.webasyst.x.installations

import com.google.gson.JsonDeserializationContext
import com.google.gson.JsonDeserializer
import com.google.gson.JsonElement
import com.google.gson.JsonObject
import com.google.gson.JsonPrimitive
import com.google.gson.JsonSerializationContext
import com.google.gson.JsonSerializer
import com.google.gson.annotations.JsonAdapter
import com.webasyst.api.webasyst.InstallationInfo
import com.webasyst.waid.CloudSignupResponse
import java.io.Serializable
import java.lang.reflect.Type
import java.util.Comparator

data class Installation(
    override val id: String,
    val name: String,
    val domain: String,
    /** Full installation URL */
    val rawUrl: String,
    val icon: Icon,
) : com.webasyst.api.Installation, Serializable {
    override val urlBase
        get() = rawUrl
    /** Human-readable installation URL (without schema) */
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
            @JsonAdapter(ResolutionKey.MapAdapter::class)
            private val thumbs = thumbs.toSortedMap()
            override val text = ""
            override val twoLine = false

            fun getThumb(resolution: Int): String {
                return thumbs.getOrDefault(thumbs.keys.lastOrNull {
                    it.resolution <= resolution
                } ?: ResolutionKey(0, 0), thumbs.values.firstOrNull()) ?: ""
            }

            override fun equals(other: Any?): Boolean =
                if (other is ImageIcon) {
                    this.thumbs == other.thumbs
                } else {
                    false
                }

            @JsonAdapter(ResolutionKey.Adapter::class)
            data class ResolutionKey(val resolution: Int, val scale: Int) : Comparable<ResolutionKey> {
                private val physicalResolution
                    get() = resolution * scale

                override fun compareTo(other: ResolutionKey): Int =
                    compare(this, other)

                override fun toString() = "${resolution}x${resolution}@${scale}x"

                class MapAdapter : JsonDeserializer<Map<ResolutionKey, String>>, JsonSerializer<Map<ResolutionKey, String>> {
                    override fun deserialize(
                        json: JsonElement,
                        typeOfT: Type,
                        context: JsonDeserializationContext
                    ): Map<ResolutionKey, String> {
                        return if (json.isJsonObject) {
                            val obj = json.asJsonObject
                            json.asJsonObject.keySet()
                                .associate { k ->
                                    ResolutionKey(k) to obj.get(k).asString
                                }
                                .toSortedMap()
                        } else {
                            emptyMap()
                        }
                    }

                    override fun serialize(
                        src: Map<ResolutionKey, String>,
                        typeOfSrc: Type,
                        context: JsonSerializationContext
                    ): JsonElement =
                        JsonObject().apply {
                            src.forEach { (k, v) ->
                                addProperty(k.toString(), v)
                            }
                        }
                }

                class Adapter : JsonDeserializer<ResolutionKey>, JsonSerializer<ResolutionKey> {
                    override fun deserialize(
                        json: JsonElement,
                        typeOfT: Type?,
                        context: JsonDeserializationContext?
                    ): ResolutionKey =
                        ResolutionKey(json.asString)

                    override fun serialize(
                        src: ResolutionKey,
                        typeOfSrc: Type?,
                        context: JsonSerializationContext?
                    ): JsonElement =
                        JsonPrimitive(src.toString())
                }

                companion object : Comparator<ResolutionKey> by compareBy( { it.physicalResolution }, { it.scale } ) {
                    private const val RE = """(\d+)x\d+(?:@(\d+)x)?"""
                    private val regex = Regex(RE)

                    operator fun invoke(str: String): ResolutionKey {
                        val g = regex.find(str)!!.groupValues
                        return ResolutionKey(g[1].toInt(), g.elementAtOrNull(2)?.toIntOrNull() ?: 1)
                    }
                }
            }

            companion object {



                operator fun invoke(image: InstallationInfo.Logo.Image): ImageIcon {
                    return ImageIcon(
                        image
                            .thumbs
                            .map { (k, v) ->
                                ResolutionKey(k) to v.url
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
                        if (logo.text.value.length <= 2) false else logo.twoLines,
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
