package com.mad.statistics.models.common

import kotlinx.datetime.Instant
import kotlinx.serialization.Serializable

@Serializable data class UserMetadata(val id: String, val userId: String, val timestamp: Instant)
