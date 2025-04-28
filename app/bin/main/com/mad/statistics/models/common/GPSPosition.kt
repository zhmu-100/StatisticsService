package com.mad.statistics.models.common

import kotlinx.datetime.Instant
import kotlinx.serialization.Serializable

@Serializable
data class GPSPosition(
    val timestamp: Instant,
    val latitude: Double,
    val longitude: Double,
    val altitude: Double,
    val speed: Double,
    val accuracy: Double
)
