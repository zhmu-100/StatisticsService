package com.mad.statistics.models

import com.mad.statistics.models.common.ExerciseMetadata
import com.mad.statistics.models.common.GPSPosition
import kotlinx.serialization.Serializable

@Serializable
data class GPSData(
    val meta: ExerciseMetadata,
    val positions: List<GPSPosition>
)
