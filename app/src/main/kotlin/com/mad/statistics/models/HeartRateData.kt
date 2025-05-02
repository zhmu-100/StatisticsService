package com.mad.statistics.models

import com.mad.statistics.models.common.ExerciseMetadata
import kotlinx.serialization.Serializable

@Serializable data class HeartRateData(val meta: ExerciseMetadata, val bpm: Int)
