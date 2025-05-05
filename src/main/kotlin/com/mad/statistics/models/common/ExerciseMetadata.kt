package com.mad.statistics.models.common

import kotlinx.datetime.Instant
import kotlinx.serialization.Serializable

@Serializable
data class ExerciseMetadata(val id: String, val exerciseId: String, val timestamp: Instant)
