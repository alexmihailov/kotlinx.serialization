package kotlinx.serialization.hocon

import kotlin.time.*
import kotlin.time.Duration.Companion.days
import kotlin.time.Duration.Companion.hours
import kotlin.time.Duration.Companion.milliseconds
import kotlin.time.Duration.Companion.minutes
import kotlin.time.Duration.Companion.seconds
import kotlinx.serialization.*
import org.junit.Assert.*
import org.junit.Test

class HoconDurationSerializerTest {

    @Serializable
    data class Simple(@DurationUnitFormat(value = DurationUnit.MINUTES) val d: Duration)

    @Serializable
    data class Nullable(@DurationUnitFormat(value = DurationUnit.SECONDS) val d: Duration?)

    @Serializable
    data class Complex(val i: Int, val s: Simple, val n: Nullable, val l: List<Simple>, val ln: List<Nullable>, val f: Boolean)

    @Test
    fun testSerializeDurationInHoconFormat() {
        var s = Simple(10.minutes)
        var config = Hocon.encodeToConfig(s)
        config.assertContains("d = 10m")

        s = Simple(1.hours)
        config = Hocon.encodeToConfig(s)
        config.assertContains("d = 60m")

        s = Simple(1.seconds)
        config = Hocon.encodeToConfig(s)
        config.assertContains("d = 0m")
    }

    @Test
    fun testSerializeNullableDurationInHoconFormat() {
        var s = Nullable(null)
        var config = Hocon.encodeToConfig(s)
        config.assertContains("d = null")

        s = Nullable(1.hours)
        config = Hocon.encodeToConfig(s)
        config.assertContains("d = 3600s")
    }

    @Test
    fun testSerializeComplexDurationInHoconFormat() {
        val obj = Complex(6, Simple(5.minutes), Nullable(null), listOf(Simple(1.minutes), Simple(2.seconds)),
            listOf(Nullable(null), Nullable(6.hours)), false
        )
        val config = Hocon.encodeToConfig(obj)
        config.assertContains("""
            i = 6
            s: { d = 5m }
            n: { d = null }
            l: [ { d = 1m }, { d = 0m } ]
            ln: [ { d = null }, { d = ${6 * 3600}s } ]
            f = false
        """.trimIndent())
    }

    @Test
    fun testDeserializeDurationInHoconFormat() {
        var obj = deserializeConfig("d = 10s", Simple.serializer())
        assertEquals(10.seconds, obj.d)
        obj = deserializeConfig("d = 10 hours", Simple.serializer())
        assertEquals(10.hours, obj.d)
        obj = deserializeConfig("d = 5 ms", Simple.serializer())
        assertEquals(5.milliseconds, obj.d)
    }

    @Test
    fun testDeserializeNullableDurationInHoconFormat() {
        var obj = deserializeConfig("d = null", Nullable.serializer())
        assertNull(obj.d)

        obj = deserializeConfig("d = 5 days", Nullable.serializer())
        assertEquals(5.days, obj.d!!)
    }

    @Test
    fun testDeserializeComplexDurationInHoconFormat() {
        val obj = deserializeConfig("""
            i = 6
            s: { d = 5m }
            n: { d = null }
            l: [ { d = 1m }, { d = 2s } ]
            ln: [ { d = null }, { d = 6h } ]
            f = true
        """.trimIndent(), Complex.serializer())
        assertEquals(5.minutes, obj.s.d)
        assertNull(obj.n.d)
        assertEquals(2, obj.l.size)
        assertEquals(1.minutes, obj.l[0].d)
        assertEquals(2.seconds, obj.l[1].d)
        assertEquals(2, obj.ln.size)
        assertNull(obj.ln[0].d)
        assertEquals(6.hours, obj.ln[1].d!!)
        assertEquals(6, obj.i)
        assertTrue(obj.f)
    }

    @Test
    fun testThrowsWhenNotTimeUnitHocon() {
        assertThrows(SerializationException::class.java) {
            deserializeConfig("d = 10 unknown", Simple.serializer())
        }
    }
}
