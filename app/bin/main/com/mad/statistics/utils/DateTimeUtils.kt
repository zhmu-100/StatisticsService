package com.mad.statistics.utils

import kotlinx.datetime.Instant
import java.time.ZoneOffset
import java.time.format.DateTimeFormatter

fun Instant.toJavaInstant(): java.time.Instant {
    return java.time.Instant.ofEpochSecond(epochSeconds, nanosecondsOfSecond.toLong())
}

fun Instant.toClickHouseDateTime(): String {
    // Преобразуем Instant в LocalDateTime в UTC
    val localDateTime = java.time.Instant.ofEpochSecond(epochSeconds, nanosecondsOfSecond.toLong())
        .atOffset(ZoneOffset.UTC)
        .toLocalDateTime()
    
    // Форматируем дату в формат, который понимает ClickHouse
    return DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss.SSS").format(localDateTime)
}

fun parseClickHouseDateTime(dateTimeStr: String): Instant {
    return try {
        // Если строка уже в формате ISO 8601
        if (dateTimeStr.endsWith("Z") || dateTimeStr.contains("+")) {
            Instant.parse(dateTimeStr)
        } else {
            // Если строка в формате ClickHouse
            val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss[.SSS]")
            val localDateTime = java.time.LocalDateTime.parse(dateTimeStr, formatter)
            val instant = localDateTime.toInstant(ZoneOffset.UTC)
            Instant.fromEpochMilliseconds(instant.toEpochMilli())
        }
    } catch (e: Exception) {
        // В случае ошибки парсинга, возвращаем текущее время
        Instant.fromEpochMilliseconds(System.currentTimeMillis())
    }
}