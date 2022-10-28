package kotlinx.serialization.hocon

import java.time.Duration
import kotlinx.serialization.*
import kotlinx.serialization.descriptors.*
import kotlinx.serialization.encoding.*
import kotlinx.serialization.hocon.internal.SuppressAnimalSniffer

/**
 * Serializer for support [Duration] serialization in HOCON.
 * Applies to HOCON only, throws [UnsupportedOperationException] in other formats.
 * Example:
 *      @Serializable
 *      data class Simple(
 *          @Serializable(JDurationSerializer::class)
 *          val d: java.time.Duration
 *      )
 * or use [UseSerializers] for all file.
 */
@SuppressAnimalSniffer
@ExperimentalSerializationApi
public object JDurationSerializer : KSerializer<Duration> {

    override val descriptor: SerialDescriptor = PrimitiveSerialDescriptor("hocon.java.time.Duration", PrimitiveKind.STRING)

    private fun unsupported(): SerializationException =
        SerializationException("JDurationSerializer only supports HOCON and should not be used outside of it.")

    override fun deserialize(decoder: Decoder): Duration = throw unsupported()

    override fun serialize(encoder: Encoder, value: Duration) = throw unsupported()
}
