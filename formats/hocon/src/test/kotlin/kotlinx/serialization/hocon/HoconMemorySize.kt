package kotlinx.serialization.hocon

import com.typesafe.config.ConfigMemorySize
import com.typesafe.config.ConfigMemorySize.ofBytes
import java.time.Duration
import kotlinx.serialization.Contextual
import kotlinx.serialization.Serializable
import org.junit.Assert
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class HoconMemorySize {

    @Serializable
    data class Simple(@Contextual val size: ConfigMemorySize)

    @Serializable
    data class Nullable(@Contextual val size: ConfigMemorySize?)

    @Serializable
    data class ConfigList(val l: List<@Contextual ConfigMemorySize>)

    @Serializable
    data class ConfigMap(val mp: Map<String, @Contextual ConfigMemorySize>)

    @Serializable
    data class ConfigMapDurationKey(val mp: Map<@Contextual ConfigMemorySize, @Contextual ConfigMemorySize>)

    @Serializable
    data class Complex(
        val i: Int,
        val s: Simple,
        val n: Nullable,
        val l: List<Simple>,
        val ln: List<Nullable>,
        val f: Boolean,
        val ld: List<@Contextual ConfigMemorySize>,
        val mp: Map<String, @Contextual ConfigMemorySize>,
        val mpp: Map<@Contextual ConfigMemorySize, @Contextual ConfigMemorySize>
    )

    @Test
    fun testDeserializeMemorySize() {
        var obj = deserializeConfig("size = 1 Ki", Simple.serializer())
        assertEquals(ofBytes(1024), obj.size)
        obj = deserializeConfig("size = 1 MB", Simple.serializer())
        assertEquals(ofBytes(1_000_000), obj.size)
        obj = deserializeConfig("size = 1 byte", Simple.serializer())
        assertEquals(ofBytes(1), obj.size)
    }

    @Test
    fun testDeserializeNullableMemorySize() {
        var obj = deserializeConfig("size = null", Nullable.serializer())
        assertNull(obj.size)
        obj = deserializeConfig("size = 5 byte", Nullable.serializer())
        assertEquals(ofBytes(5), obj.size)
    }

    @Test
    fun testDeserializeListOfMemorySize() {
        val obj = deserializeConfig("l: [ 1b, 1MB, 1Ki ]", ConfigList.serializer())
        assertEquals(listOf(ofBytes(1), ofBytes(1_000_000), ofBytes(1024)), obj.l)
    }

    @Test
    fun testDeserializeMapOfMemorySize() {
        val obj = deserializeConfig("""
             mp: { one = 2kB, two = 5 MB }
        """.trimIndent(), ConfigMap.serializer())
        assertEquals(mapOf("one" to ofBytes(2000), "two" to ofBytes(5_000_000)), obj.mp)

        val objDurationKey = deserializeConfig("""
             mp: { 1024b = 1Ki }
        """.trimIndent(),ConfigMapDurationKey.serializer())
        assertEquals(mapOf(ofBytes(1024) to ofBytes(1024)), objDurationKey.mp)
    }

    @Test
    fun testDeserializeComplexMemorySize() {
        val obj = deserializeConfig("""
            i = 6
            s: { size = 5 MB }
            n: { size = null }
            l: [ { size = 1 kB }, { size = 2b } ]
            ln: [ { size = null }, { size = 1 Mi } ]
            f = true
            ld: [ 1 kB, 1 m]
            mp: { one = 2kB, two = 5 MB }
            mpp: { 1024b = 1Ki }
        """.trimIndent(), Complex.serializer())
        assertEquals(ofBytes(5_000_000), obj.s.size)
        assertNull(obj.n.size)
        assertEquals(listOf(Simple(ofBytes(1000)), Simple(ofBytes(2))), obj.l)
        assertEquals(listOf(Nullable(null), Nullable(ofBytes(1024 * 1024))), obj.ln)
        assertEquals(6, obj.i)
        assertTrue(obj.f)
        assertEquals(listOf(ofBytes(1000), ofBytes(1048576)), obj.ld)
        assertEquals(mapOf("one" to ofBytes(2000), "two" to ofBytes(5_000_000)), obj.mp)
        assertEquals(mapOf(ofBytes(1024) to ofBytes(1024)), obj.mpp)
    }
}