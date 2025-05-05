package com.mad.statistics.models

import com.mad.statistics.models.common.UserMetadata
import kotlinx.serialization.Serializable

@Serializable data class CaloriesData(val meta: UserMetadata, val calories: Double)
