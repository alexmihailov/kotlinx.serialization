@file:Suppress("JAVA_MODULE_DOES_NOT_READ_UNNAMED_MODULE")
package kotlinx.serialization.hocon

import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.SerialInfo
import kotlinx.serialization.descriptors.SerialDescriptor

@SerialInfo
@Target(AnnotationTarget.PROPERTY)
@ExperimentalSerializationApi
annotation class HoconJavaBean

@OptIn(ExperimentalSerializationApi::class)
internal fun SerialDescriptor.getHoconJavaBean(index: Int): Boolean =
    getElementAnnotations(index).filterIsInstance(HoconJavaBean::class.java).isNotEmpty()
