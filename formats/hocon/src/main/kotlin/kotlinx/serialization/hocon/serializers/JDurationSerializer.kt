@file:Suppress("JAVA_MODULE_DOES_NOT_READ_UNNAMED_MODULE")
package kotlinx.serialization.hocon.serializers

import com.typesafe.config.Config
import kotlinx.serialization.ExperimentalSerializationApi
import java.time.Duration as JDuration
import kotlinx.serialization.*
import kotlinx.serialization.descriptors.*
import kotlinx.serialization.encoding.*
import kotlinx.serialization.hocon.Hocon
import kotlinx.serialization.hocon.UnsupportedFormatException
import kotlinx.serialization.hocon.internal.*

import kotlin.time.*

/**
 * Serializer for [JDuration].
 * For decode using [Duration format][https://github.com/lightbend/config/blob/main/HOCON.md#duration-format].
 * For encode used time unit short names: d, h, m, s, ms, us, ns.
 * Encoding use the largest time unit.
 * Example:
 *      120.seconds -> 2 m
 *      121.seconds -> 121 s
 *      120.minutes -> 2 h
 *      122.minutes -> 122 m
 *      24.hours -> 1 d
 * When encoding, there is a conversion to [Duration].
 * All restrictions on the maximum and minimum duration are specified in [Duration].
 */
@ExperimentalSerializationApi
object JDurationSerializer : KSerializer<JDuration> {

    private val valueResolver: (Config, String) -> JDuration = { conf, path -> conf.decodeDuration(path) }

    override val descriptor: SerialDescriptor =
        PrimitiveSerialDescriptor("hocon.java.time.Duration", PrimitiveKind.STRING)

    override fun deserialize(decoder: Decoder): JDuration {
        return when (decoder) {
            is Hocon.ConfigReader -> decoder.getValueFromTaggedConfig(decoder.getCurrentTag(), valueResolver)
            is Hocon.ListConfigReader -> decoder.getValueFromTaggedConfig(decoder.getCurrentTag(), valueResolver)
            is Hocon.MapConfigReader -> decoder.getValueFromTaggedConfig(decoder.getCurrentTag(), valueResolver)
            else -> throw UnsupportedFormatException("JDurationSerializer")
        }
    }

    override fun serialize(encoder: Encoder, value: JDuration) {
        encoder.encodeString(encodeDuration(value.toKotlinDuration()))
    }
}
