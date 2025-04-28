package com.mad.statistics.utils

import com.mad.statistics.utils.toJavaInstant
import com.mad.statistics.utils.toClickHouseDateTime
import com.mad.statistics.utils.parseClickHouseDateTime
import kotlinx.datetime.Instant
import org.junit.jupiter.api.Test
import java.time.ZoneOffset
import java.time.format.DateTimeFormatter
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class DateTimeUtilsTest {

    @Test
    fun `toJavaInstant converts correctly`() {
        val kotlinInstant = Instant.fromEpochMilliseconds(1_500_000_000_123)
        val javaInstant = kotlinInstant.toJavaInstant()
        assertEquals(kotlinInstant.epochSeconds, javaInstant.epochSecond)
        assertEquals(kotlinInstant.nanosecondsOfSecond.toLong(), javaInstant.nano.toLong())
    }

    @Test
    fun `toClickHouseDateTime formats epoch as zero`() {
        val kotlinInstant = Instant.fromEpochMilliseconds(0)
        val formatted = kotlinInstant.toClickHouseDateTime()
        assertEquals("1970-01-01 00:00:00.000", formatted)
    }

    @Test
    fun `parseClickHouseDateTime parses ISO8601 Z`() {
        val iso = "2021-05-20T12:34:56Z"
        val inst = parseClickHouseDateTime(iso)
        assertEquals(Instant.parse(iso), inst)
    }

    @Test
    fun `parseClickHouseDateTime parses ISO8601 with offset`() {
        val iso = "2021-05-20T14:34:56+02:00"
        val inst = parseClickHouseDateTime(iso)
        assertEquals(Instant.parse(iso), inst)
    }

    @Test
    fun `parseClickHouseDateTime parses ClickHouse with milliseconds`() {
        val ch = "2021-05-20 12:34:56.789"
        val inst = parseClickHouseDateTime(ch)
        val expectedJava = java.time.LocalDateTime.parse(ch, DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss[.SSS]"))
            .toInstant(ZoneOffset.UTC)
        val expected = Instant.fromEpochMilliseconds(expectedJava.toEpochMilli())
        assertEquals(expected, inst)
    }

    @Test
    fun `parseClickHouseDateTime parses ClickHouse without milliseconds`() {
        val ch = "2021-05-20 12:34:56"
        val inst = parseClickHouseDateTime(ch)
        val expectedJava = java.time.LocalDateTime.parse(ch, DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss[.SSS]"))
            .toInstant(ZoneOffset.UTC)
        val expected = Instant.fromEpochMilliseconds(expectedJava.toEpochMilli())
        assertEquals(expected, inst)
    }

    @Test
    fun `parseClickHouseDateTime returns now on invalid input`() {
        val start = System.currentTimeMillis()
        val inst = parseClickHouseDateTime("not a date")
        val ms = inst.toEpochMilliseconds()
        val end = System.currentTimeMillis()
        assertTrue(ms in start..end, "Expected timestamp within [$start, $end], but was $ms")
    }
}
