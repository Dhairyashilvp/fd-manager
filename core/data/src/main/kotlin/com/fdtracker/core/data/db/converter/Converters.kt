package com.fdtracker.core.data.db.converter

import androidx.room.TypeConverter
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import java.math.BigDecimal
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneOffset

/**
 * Room converters kept for type-awareness and future entity evolution.
 *
 * Current entities mostly persist primitive SQL-friendly types (String/Long/Int),
 * while domain-layer transformations are intentionally centralized in mappers.
 */
class Converters {
    private val json = Json { ignoreUnknownKeys = true }

    @TypeConverter
    fun fromLocalDate(value: LocalDate?): Long? {
        return value?.atStartOfDay(ZoneOffset.UTC)?.toInstant()?.toEpochMilli()
    }

    @TypeConverter
    fun toLocalDate(value: Long?): LocalDate? {
        return value?.let { millis ->
            Instant.ofEpochMilli(millis).atZone(ZoneOffset.UTC).toLocalDate()
        }
    }

    @TypeConverter
    fun fromBigDecimal(value: BigDecimal?): String? {
        return value?.toPlainString()
    }

    @TypeConverter
    fun toBigDecimal(value: String?): BigDecimal? {
        return value?.let { runCatching { BigDecimal(it) }.getOrNull() }
    }

    @TypeConverter
    fun fromStringList(value: List<String>?): String? {
        return value?.let { json.encodeToString(it) }
    }

    @TypeConverter
    fun toStringList(value: String?): List<String>? {
        return value?.let { json.decodeFromString<List<String>>(it) }
    }
}
