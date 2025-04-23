package com.mad.statistics.utils

import kotlinx.datetime.Instant
import java.time.ZoneOffset

fun Instant.toJavaInstant(): java.time.Instant {
    return java.time.Instant.ofEpochSecond(epochSeconds, nanosecondsOfSecond.toLong())
}
