package com.scorealarm.doorapp.rest.converter

import com.google.gson.*
import org.joda.time.DateTime
import org.joda.time.DateTimeZone
import org.joda.time.format.DateTimeFormat
import java.lang.reflect.Type

class DateTimeConverter : JsonDeserializer<DateTime>, JsonSerializer<DateTime> {

    val TAG = DateTimeConverter::class.java.canonicalName

    private val DATE_TIME_FORMATTER = DateTimeFormat.forPattern("yyyy-MM-dd HH:mm:ss")
    private val DATE_TIME_UTC_FORMATTER =
        DateTimeFormat.forPattern("yyyy-MM-dd'T'HH:mm:ss'Z'").withZoneUTC()

    override fun deserialize(
        json: JsonElement?,
        typeOfT: Type?,
        context: JsonDeserializationContext?
    ): DateTime {
        var dateTime: DateTime? = null
        var isValid = true
        if (json?.asString.isNullOrBlank().not()) {
            if (json?.asString?.contains("Z") == true) {
                try {
                    dateTime = DATE_TIME_UTC_FORMATTER.parseDateTime(json.getAsString()).withZone(
                        DateTimeZone.UTC
                    )
                    return dateTime
                } catch (ex: Exception) {
                    isValid = false
                }
            } else {
                try {
                    dateTime = DATE_TIME_FORMATTER.parseDateTime(json?.getAsString())
                    return dateTime
                } catch (ex: Exception) {
                    isValid = false
                }
            }
        }
        if (!isValid || dateTime == null) throw JsonParseException("Invalid")


        return dateTime
    }

    override fun serialize(
        src: DateTime?,
        typeOfSrc: Type?,
        context: JsonSerializationContext?
    ): JsonElement =
        JsonPrimitive(if (src == null) "" else DATE_TIME_FORMATTER.print(src))

}