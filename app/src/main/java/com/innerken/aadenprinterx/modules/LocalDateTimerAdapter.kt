package com.innerken.aadenprinterx.modules

import com.google.gson.TypeAdapter
import com.google.gson.stream.JsonReader
import com.google.gson.stream.JsonWriter
import kotlinx.datetime.Instant
import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toInstant
import kotlinx.datetime.toLocalDateTime
import java.time.format.DateTimeFormatter

class LocalDateTimeAdapter : TypeAdapter<LocalDateTime?>() {
    private val formatter = DateTimeFormatter.ISO_LOCAL_DATE_TIME

    override fun write(out: JsonWriter, value: LocalDateTime?) {
        if (value == null) {
            out.nullValue()
        } else {
            out.value(formatter.format(value.toJavaLocalDateTime())) // Use Java's LocalDateTime for formatting
        }
    }

    override fun read(input: JsonReader): LocalDateTime? {
        return if (input.peek() == com.google.gson.stream.JsonToken.NULL) {
            input.nextNull()
            null
        } else {
            val dateString = input.nextString()
            java.time.LocalDateTime.parse(dateString, formatter).toKotlinLocalDateTime()
        }
    }

    // Extension functions to convert between kotlinx.datetime and java.time
    private fun LocalDateTime.toJavaLocalDateTime(): java.time.LocalDateTime =
        java.time.LocalDateTime.ofInstant(
            this.toInstant(TimeZone.UTC).toJavaInstant(), java.time.ZoneOffset.UTC
        )

    private fun java.time.LocalDateTime.toKotlinLocalDateTime(): LocalDateTime =
        this.toInstant(java.time.ZoneOffset.UTC).toKotlinInstant().toLocalDateTime(TimeZone.UTC)

    private fun Instant.toJavaInstant(): java.time.Instant =
        java.time.Instant.ofEpochSecond(this.epochSeconds)

    private fun java.time.Instant.toKotlinInstant(): Instant =
        Instant.fromEpochSeconds(this.epochSecond, this.nano)

}

// Adapter for LocalDate (similar to LocalDateTimeAdapter)
class LocalDateAdapter : TypeAdapter<LocalDate?>() {
    private val formatter = DateTimeFormatter.ISO_LOCAL_DATE

    override fun write(out: JsonWriter, value: LocalDate?) {
        if (value == null) {
            out.nullValue()
        } else {
            out.value(formatter.format(value.toJavaLocalDate()))
        }
    }

    override fun read(input: JsonReader): LocalDate? {
        return if (input.peek() == com.google.gson.stream.JsonToken.NULL) {
            input.nextNull()
            null
        } else {
            val dateString = input.nextString()
            java.time.LocalDate.parse(dateString, formatter).toKotlinLocalDate()
        }
    }

    private fun LocalDate.toJavaLocalDate(): java.time.LocalDate =
        java.time.LocalDate.of(this.year, this.monthNumber, this.dayOfMonth)

    private fun java.time.LocalDate.toKotlinLocalDate(): LocalDate =
        LocalDate(this.year, this.monthValue, this.dayOfMonth)
}