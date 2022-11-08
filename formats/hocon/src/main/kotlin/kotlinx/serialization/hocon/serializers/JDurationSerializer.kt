@file:Suppress("JAVA_MODULE_DOES_NOT_READ_UNNAMED_MODULE")
package kotlinx.serialization.hocon.serializers

import kotlinx.serialization.ExperimentalSerializationApi
import java.time.Duration as JDuration
import kotlinx.serialization.KSerializer
import kotlinx.serialization.SerializationException
import kotlinx.serialization.descriptors.*
import kotlinx.serialization.encoding.*
import kotlinx.serialization.hocon.Hocon
import kotlinx.serialization.hocon.internal.decodeDuration
import kotlinx.serialization.hocon.internal.encodeDuration
import kotlin.time.toKotlinDuration

@ExperimentalSerializationApi
object JDurationSerializer : KSerializer<JDuration> {
    override val descriptor: SerialDescriptor =
        PrimitiveSerialDescriptor("hocon.java.time.Duration", PrimitiveKind.STRING)

    override fun deserialize(decoder: Decoder): JDuration {
        return when (decoder) {
            is Hocon.ConfigReader -> decoder.getValueFromTaggedConfig(decoder.getCurrentTag()) { conf, path -> conf.decodeDuration(path) }
            is Hocon.ListConfigReader -> decoder.getValueFromTaggedConfig(decoder.getCurrentTag()) { conf, path -> conf.decodeDuration(path) }
            is Hocon.MapConfigReader -> decoder.getValueFromTaggedConfig(decoder.getCurrentTag()) { conf, path -> conf.decodeDuration(path) }
            else -> throw SerializationException() // TODO message
        }
    }

    override fun serialize(encoder: Encoder, value: JDuration) {
        encoder.encodeString(encodeDuration(value.toKotlinDuration()))
    }
}
