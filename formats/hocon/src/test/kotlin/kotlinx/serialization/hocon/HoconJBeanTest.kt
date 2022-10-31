package kotlinx.serialization.hocon

import kotlinx.serialization.Contextual
import kotlinx.serialization.Serializable
import org.junit.Assert.assertEquals
import org.junit.Test

class HoconJBeanTest {

    @Serializable
    data class TestData(
        @Contextual
        @HoconJavaBean
        val d: TestJavaBean
    )

    @Test
    fun test() {
        val obj = deserializeConfig("""
            d: {
                name = Alex
                age = 27
            }
        """.trimIndent(), TestData.serializer())
        val bean = TestJavaBean().apply {
            name = "Alex"
            age = 27
        }
        assertEquals(TestData(bean), obj)
    }
}