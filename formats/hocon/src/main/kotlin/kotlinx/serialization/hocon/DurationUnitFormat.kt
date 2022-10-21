package kotlinx.serialization.hocon

import kotlin.time.*
import kotlinx.serialization.*

/**
 * Specifies which units of [Duration] will be used for serialization.
 * [DurationUnit] is used to specify units. The following mapping will be applied:
 * [DurationUnit.NANOSECONDS] -> ns;
 * [DurationUnit.MICROSECONDS] -> us;
 * [DurationUnit.MILLISECONDS] -> ms;
 * [DurationUnit.SECONDS] -> s;
 * [DurationUnit.MINUTES] -> m;
 * [DurationUnit.HOURS] -> h;
 * [DurationUnit.DAYS] -> d.
 * By default will be use [DurationUnit.NANOSECONDS].
 * If the specified units are greater than the actual value, then the result will be zero.
 *
 * Example usage:
 *
 * ```
 * @Serializable
 * data class Example(
 *      val d: Duration, // serialized in nanoseconds
 *
 *      @DurationUnitFormat(value = DurationUnit.MINUTES)
 *      val d1: Duration // serialized in minutes
 * )
 *
 * val example = Example(1.minutes, 1.hours)
 * val conf = Hocon.encodeToConfig(example) // {"d":"60000000000ns","d1":"60m"}
 * ```
 * @property value [DurationUnit] is used to specify units
 */
@SerialInfo
@Target(AnnotationTarget.PROPERTY)
@ExperimentalSerializationApi
public annotation class DurationUnitFormat(val value: DurationUnit)
