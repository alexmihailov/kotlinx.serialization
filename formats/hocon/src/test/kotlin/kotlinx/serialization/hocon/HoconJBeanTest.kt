package kotlinx.serialization.hocon

import com.typesafe.config.ConfigFactory
import kotlinx.serialization.*
import kotlinx.serialization.descriptors.*
import kotlinx.serialization.encoding.*
import kotlinx.serialization.modules.*
import org.junit.Assert.*
import org.junit.Test
import kotlin.test.assertFailsWith

class HoconJBeanTest {

    @Serializable
    data class TestData(@Contextual val d: TestJavaBean)
    @Serializable
    data class TestNullableData(@Contextual val d: TestJavaBean?)
    @Serializable
    data class ConfigList(val ld: List<@Contextual TestJavaBean>)
    @Serializable
    data class ConfigMap(val mp: Map<String, @Contextual TestJavaBean>)

    @Serializable
    data class Complex(
        val i: Int,
        val s: TestData,
        val n: TestNullableData,
        val l: List<TestData>,
        val ln: List<TestNullableData>,
        val f: Boolean,
        val ld: List<@Contextual TestJavaBean>,
        val mp: Map<String, @Contextual TestJavaBean>
    )

    private val strConfig = """
        d: {
            name = Alex
            age = 27
        }
        """.trimIndent()
    private val bean = TestJavaBean().apply {
        name = "Alex"
        age = 27
    }

    @Test
    fun testDeserializeJBean() {
        val obj = deserializeConfig(strConfig, TestData.serializer())
        assertEquals(TestData(bean), obj)
    }

    @Test
    fun testDeserializeNullableJBean() {
        var obj = deserializeConfig("d: null", TestNullableData.serializer())
        assertNull(obj.d)
        obj = deserializeConfig(strConfig, TestNullableData.serializer())
        assertEquals(TestNullableData(bean), obj)
    }

    @Test
    fun testDeserializeListOfJBean() {
        val obj = deserializeConfig("""
            ld: [
                { name = Alex, age = 27 },
                { name = Alex, age = 27 }
            ]
        """.trimIndent(), ConfigList.serializer())
        assertEquals(listOf(bean, bean), obj.ld)
    }

    @Test
    fun testDeserializeMapOfJBean() {
        val obj = deserializeConfig("""
            mp: { first = { name = Alex, age = 27 }, second = { name = Alex, age = 27 } }
        """.trimIndent(), ConfigMap.serializer())
        assertEquals(mapOf("first" to bean, "second" to bean), obj.mp)
    }

    @Test
    fun testDeserializeComplexJBean() {
        val obj = deserializeConfig("""
            i = 6
            s: { d: { name = Alex, age = 27 } }
            n: { d: null }
            l: [ { d: { name = Alex, age = 27 } }, { d: { name = Alex, age = 27 } } ]
            ln: [ { d: null }, { d: { name = Alex, age = 27 } } ]
            f = true
            ld: [ { name = Alex, age = 27 }, { name = Alex, age = 27 } ]
            mp: { first = { name = Alex, age = 27 } }
        """.trimIndent(), Complex.serializer())
        assertEquals(bean, obj.s.d)
        assertNull(obj.n.d)
        assertEquals(listOf(TestData(bean), TestData(bean)), obj.l)
        assertEquals(listOf(TestNullableData(null), TestNullableData(bean)), obj.ln)
        assertTrue(obj.f)
        assertEquals(listOf(bean, bean), obj.ld)
        assertEquals(mapOf("first" to bean), obj.mp)
    }

    @Test
    fun testUseCustomContextual() {
        val serializer = object : KSerializer<TestJavaBean> {
            override val descriptor: SerialDescriptor = PrimitiveSerialDescriptor("test", PrimitiveKind.STRING)
            override fun deserialize(decoder: Decoder): TestJavaBean = throw UnsupportedOperationException("Custom deserialize")
            override fun serialize(encoder: Encoder, value: TestJavaBean) = throw UnsupportedOperationException("Custom serialize")
        }
        val hocon = Hocon { serializersModule = SerializersModule { contextual(TestJavaBean::class, serializer) } }
        assertFailsWith<UnsupportedOperationException>("Custom deserialize") {
            hocon.decodeFromConfig<TestData>( ConfigFactory.parseString("d: { name = Alex, age = 27 }"))
        }
        // TODO на сериализацией надо подумать!
        assertFailsWith<UnsupportedOperationException>("Custom serialize") {
            hocon.encodeToConfig(TestData(bean))
        }
    }
}
