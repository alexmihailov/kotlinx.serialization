@file:Suppress("JAVA_MODULE_DOES_NOT_READ_UNNAMED_MODULE")
package kotlinx.serialization.hocon.internal

import kotlinx.serialization.builtins.serializer
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlin.time.Duration

/**
 * Returns `true` if this descriptor is equals to descriptor in [kotlinx.serialization.internal.DurationSerializer].
 */
internal val SerialDescriptor.isDuration: Boolean
    get() = this == Duration.serializer().descriptor
