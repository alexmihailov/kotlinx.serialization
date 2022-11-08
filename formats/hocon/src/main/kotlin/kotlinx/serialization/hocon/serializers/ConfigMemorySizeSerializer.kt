@file:Suppress("JAVA_MODULE_DOES_NOT_READ_UNNAMED_MODULE")
package kotlinx.serialization.hocon.serializers

import com.typesafe.config.*
import java.math.BigInteger
import kotlinx.serialization.*
import kotlinx.serialization.descriptors.*
import kotlinx.serialization.encoding.*
import kotlinx.serialization.hocon.Hocon

@ExperimentalSerializationApi
object ConfigMemorySizeSerializer : KSerializer<ConfigMemorySize> {

    // For powers of two.
    private val memoryUnitFormats = listOf("byte", "KiB", "MiB", "GiB", "TiB", "PiB", "EiB", "ZiB", "YiB")

    override val descriptor: SerialDescriptor =
        PrimitiveSerialDescriptor("hocon.com.typesafe.config.ConfigMemorySize", PrimitiveKind.STRING)

    override fun deserialize(decoder: Decoder): ConfigMemorySize {
        return when (decoder) {
            is Hocon.ConfigReader -> decoder.getValueFromTaggedConfig(decoder.getCurrentTag()) { conf, path -> conf.decodeMemorySize(path) }
            is Hocon.ListConfigReader -> decoder.getValueFromTaggedConfig(decoder.getCurrentTag()) { conf, path -> conf.decodeMemorySize(path) }
            is Hocon.MapConfigReader -> decoder.getValueFromTaggedConfig(decoder.getCurrentTag()) { conf, path -> conf.decodeMemorySize(path) }
            else -> throw SerializationException() // TODO message
        }
    }

    override fun serialize(encoder: Encoder, value: ConfigMemorySize) {
        // We determine that it is divisible by 1024 (2^10).
        // And if it is divisible, then the number itself is shifted to the right by 10.
        // And so on until we find one that is no longer divisible by 1024.
        // ((n & ((1 << m) - 1)) == 0)
        val andVal = BigInteger.valueOf(1023) // ((2^10) - 1) = 0x3ff = 1023
        var bytes = value.toBytesBigInteger()
        var unitIndex = 0
        while (bytes.and(andVal) == BigInteger.ZERO) { // n & 0x3ff == 0
            if (unitIndex < memoryUnitFormats.lastIndex) {
                bytes = bytes.shiftRight(10)
                unitIndex++
            } else break
        }
        encoder.encodeString("$bytes ${memoryUnitFormats[unitIndex]}")
    }

    private fun Config.decodeMemorySize(path: String): ConfigMemorySize = try {
        getMemorySize(path)
    } catch (e: ConfigException) {
        throw SerializationException("Value at $path cannot be read as ConfigMemorySize because it is not a valid HOCON Size value", e)
    }
}
