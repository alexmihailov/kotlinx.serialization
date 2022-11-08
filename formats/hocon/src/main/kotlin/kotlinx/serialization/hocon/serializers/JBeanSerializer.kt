@file:Suppress("JAVA_MODULE_DOES_NOT_READ_UNNAMED_MODULE")
package kotlinx.serialization.hocon.serializers

import com.typesafe.config.*
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.*
import kotlinx.serialization.descriptors.*
import kotlinx.serialization.encoding.*
import kotlinx.serialization.hocon.*

/**
 * Serializer for Java Bean objects. Supports only decoding of objects from [Config].
 * [JBeanSerializer] using [com.typesafe.config.ConfigBeanFactory.create] to decode Java Bean objects.
 * To create a [JBeanSerializer], use the method [jBeanSerializer].
 *
 */
@ExperimentalSerializationApi
class JBeanSerializer<T> @PublishedApi internal constructor(private val clazz: Class<T>): KSerializer<T> {

    override val descriptor: SerialDescriptor = PrimitiveSerialDescriptor("hocon.java.bean", PrimitiveKind.STRING)

    private fun valueResolver(conf: Config, path: String): T = try {
        ConfigBeanFactory.create(conf.getConfig(path), clazz)
    } catch (e: ConfigException) {
        throw SerializationException("Error while deserializing java bean class ${clazz.simpleName}.", e)
    }

    override fun deserialize(decoder: Decoder): T {
        return when (decoder) {
            is Hocon.ConfigReader -> decoder.getValueFromTaggedConfig(decoder.getCurrentTag(), this::valueResolver)
            is Hocon.ListConfigReader -> decoder.getValueFromTaggedConfig(decoder.getCurrentTag(), this::valueResolver)
            is Hocon.MapConfigReader -> decoder.getValueFromTaggedConfig(decoder.getCurrentTag(), this::valueResolver)
            else -> throw UnsupportedFormatException("JBeanSerializer")
        }
    }

    override fun serialize(encoder: Encoder, value: T) {
        throw SerializationException("JBeanSerializer is only used for deserialization.")
    }

    companion object {

        /**
         * Creates a [JBeanSerializer] using a Java Bean type.
         * @return [JBeanSerializer]
         */
        @ExperimentalSerializationApi
        inline fun <reified T> jBeanSerializer(): JBeanSerializer<T> = JBeanSerializer(T::class.java)
    }
}