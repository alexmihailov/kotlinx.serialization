@file:Suppress("JAVA_MODULE_DOES_NOT_READ_UNNAMED_MODULE")
package kotlinx.serialization.hocon.internal

import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.builtins.serializer
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlin.time.Duration

internal val SerialDescriptor.isDuration: Boolean
    get() = this == Duration.serializer().descriptor

@OptIn(ExperimentalSerializationApi::class)
internal val SerialDescriptor.isContextual: Boolean
    get() = serialName.contains("kotlinx.serialization.ContextualSerializer")
