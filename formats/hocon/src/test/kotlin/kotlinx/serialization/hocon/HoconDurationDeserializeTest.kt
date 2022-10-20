package kotlinx.serialization.hocon

import kotlinx.serialization.Serializable
import kotlinx.serialization.SerializationException
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertThrows
import org.junit.Test
import kotlin.time.Duration
import kotlin.time.Duration.Companion.days
import kotlin.time.Duration.Companion.hours
import kotlin.time.Duration.Companion.milliseconds
import kotlin.time.Duration.Companion.minutes
import kotlin.time.Duration.Companion.seconds

class HoconDurationDeserializeTest {

    @Serializable
    data class Simple(val d: Duration)

    @Serializable
    data class Nullable(val d: Duration?)

    @Serializable
    data class Complex(val s: Simple, val n: Nullable, val l: List<Simple>, val ln: List<Nullable>)

    @Test
    fun `deserialize config with duration in ISO-8601-2 format`() {
        val obj = deserializeConfig("d = PT1M", Simple.serializer())
        assertEquals(1.minutes, obj.d)
    }

    @Test
    fun `deserialize config with nullable duration in ISO-8601-2 format`() {
        var obj = deserializeConfig("d = null", Nullable.serializer())
        assertNull(obj.d)

        obj = deserializeConfig("d = PT5M", Nullable.serializer())
        assertEquals(5.minutes, obj.d!!)
    }

    @Test
    fun `deserialize config with duration in HOCON format`() {
        var obj = deserializeConfig("d = 10s", Simple.serializer(), useDurationParser = true)
        assertEquals(10.seconds, obj.d)
        obj = deserializeConfig("d = 10 hours", Simple.serializer(), useDurationParser = true)
        assertEquals(10.hours, obj.d)
        obj = deserializeConfig("d = 5 ms", Simple.serializer(), useDurationParser = true)
        assertEquals(5.milliseconds, obj.d)
    }

    @Test
    fun `deserialize config with nullable duration in HOCON format`() {
        var obj = deserializeConfig("d = null", Nullable.serializer(), useDurationParser = true)
        assertNull(obj.d)

        obj = deserializeConfig("d = 5 days", Nullable.serializer(), useDurationParser = true)
        assertEquals(5.days, obj.d!!)
    }

    @Test
    fun `deserialize complex config with duration in ISO-8601-2 format`() {
        val obj = deserializeConfig("""
            s: { d = PT5M }
            n: { d = null }
            l: [ { d = PT1M }, { d = PT2S } ]
            ln: [ { d = null }, { d = PT6H } ]
        """.trimIndent(), Complex.serializer())
        assertEquals(5.minutes, obj.s.d)
        assertNull(obj.n.d)
        assertEquals(2, obj.l.size)
        assertEquals(1.minutes, obj.l[0].d)
        assertEquals(2.seconds, obj.l[1].d)
        assertEquals(2, obj.ln.size)
        assertNull(obj.ln[0].d)
        assertEquals(6.hours, obj.ln[1].d!!)
    }

    @Test
    fun `deserialize complex config with duration in HOCON format`() {
        val obj = deserializeConfig("""
            s: { d = 5m }
            n: { d = null }
            l: [ { d = 1m }, { d = 2s } ]
            ln: [ { d = null }, { d = 6h } ]
        """.trimIndent(), Complex.serializer(), useDurationParser = true)
        assertEquals(5.minutes, obj.s.d)
        assertNull(obj.n.d)
        assertEquals(2, obj.l.size)
        assertEquals(1.minutes, obj.l[0].d)
        assertEquals(2.seconds, obj.l[1].d)
        assertEquals(2, obj.ln.size)
        assertNull(obj.ln[0].d)
        assertEquals(6.hours, obj.ln[1].d!!)
    }

    @Test
    fun `throws SerializationException when not parse time unit in HOCON format`() {
        assertThrows(SerializationException::class.java) {
            deserializeConfig("d = 10 unknown", Simple.serializer(), useDurationParser = true)
        }
    }
}
