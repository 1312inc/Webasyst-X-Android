package installations

import com.webasyst.x.cache.DataCache
import com.webasyst.x.installations.Installation
import kotlin.random.Random
import kotlin.test.Test
import kotlin.test.assertEquals

class TestInstallationSerialization {
    private val gson = DataCache.gson()
    private val random = Random.Default

    private fun randomResolutionKey() =
        Installation.Icon.ImageIcon.ResolutionKey(random.nextInt(1, 512), random.nextInt(1, 4))

    @Test
    fun testImageIconResolutionKeyDeserialization() {
        repeat(10) {
            val resolutionKey = randomResolutionKey()
            val case = Case(resolutionKey)
            val serialized = gson.toJson(case, Case::class.java)
            val deserializedCase = gson.fromJson(serialized, Case::class.java)
            assertEquals(resolutionKey, deserializedCase.resolutionKey)
        }
    }

    @Test
    fun `test image icon resolution key as map key`() {
        repeat(10) {
            val case = MapCase((List(10) { randomResolutionKey() to random.nextLong().toString() }).toMap())
            val serialized = gson.toJson(case, MapCase::class.java)
            val deserializedCase = gson.fromJson(serialized, MapCase::class.java)
            assertEquals(case, deserializedCase)
        }
    }

    @Test
    fun `test actual Installation serialization`() {
        val installation = Installation(
            id = "id",
            name = "name",
            domain = "domain",
            url = "url",
            icon = Installation.Icon.ImageIcon(
                thumbs = mapOf(
                    Installation.Icon.ImageIcon.ResolutionKey(1, 1) to "a",
                    Installation.Icon.ImageIcon.ResolutionKey(2, 2) to "b",
                    Installation.Icon.ImageIcon.ResolutionKey(3, 3) to "c",
                ).toSortedMap()
            ),
            cloudExpireDate = null,
        )
        val serialized = gson.toJson(installation)
        val deserialized = gson.fromJson(serialized, Installation::class.java)
        assertEquals(installation, deserialized)
    }

    data class Case(val resolutionKey: Installation.Icon.ImageIcon.ResolutionKey)
    data class MapCase(val data: Map<Installation.Icon.ImageIcon.ResolutionKey, String>)
}
