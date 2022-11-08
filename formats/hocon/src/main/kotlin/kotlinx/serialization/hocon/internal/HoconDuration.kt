@file:Suppress("JAVA_MODULE_DOES_NOT_READ_UNNAMED_MODULE")
package kotlinx.serialization.hocon.internal

import com.typesafe.config.Config
import com.typesafe.config.ConfigException
import kotlinx.serialization.SerializationException
import kotlin.time.Duration

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

internal fun Config.decodeDuration(path: String): java.time.Duration = try {
    getDuration(path)
} catch (e: ConfigException) {
    throw SerializationException("Value at $path cannot be read as Duration because it is not a valid HOCON duration value", e)
}
