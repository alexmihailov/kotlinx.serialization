@file:Suppress("JAVA_MODULE_DOES_NOT_READ_UNNAMED_MODULE")
package kotlinx.serialization.hocon.internal

import com.typesafe.config.*
import kotlinx.serialization.*
import kotlin.time.Duration
import java.time.Duration as JDuration

/**
 * Encode [Duration] objects using time unit short names: d, h, m, s, ms, us, ns.
 * Example:
 *     120.seconds -> 2 m
 *     121.seconds -> 121 s
 *     120.minutes -> 2 h
 *     122.minutes -> 122 m
 *     24.hours -> 1 d
 * Encoding use the largest time unit.
 * All restrictions on the maximum and minimum duration are specified in [Duration].
 * @param value [Duration]
 * @return encoded value
 */
internal fun encodeDuration(value: Duration): String = value.toComponents { seconds, nanoseconds ->
    when {
        nanoseconds == 0 -> {
            if (seconds % 60 == 0L) { // minutes
                if (seconds % 3600 == 0L) { // hours
                    if (seconds % 86400 == 0L) { // days
                        "${seconds / 86400} d"
                    } else {
                        "${seconds / 3600} h"
                    }
                } else {
                    "${seconds / 60} m"
                }
            } else {
                "$seconds s"
            }
        }
        nanoseconds % 1_000_000 == 0 -> "${seconds * 1_000 + nanoseconds / 1_000_000} ms"
        nanoseconds % 1_000 == 0 -> "${seconds * 1_000_000 + nanoseconds / 1_000} us"
        else -> "${value.inWholeNanoseconds} ns"
    }
}

/**
 * Decode [JDuration] from [Config].
 * See https://github.com/lightbend/config/blob/main/HOCON.md#duration-format
 *
 * @receiver [Config]
 * @param path in config
 * @return [JDuration]
 */
internal fun Config.decodeDuration(path: String): JDuration = try {
    getDuration(path)
} catch (e: ConfigException) {
    throw SerializationException("Value at $path cannot be read as Duration because it is not a valid HOCON duration value", e)
}