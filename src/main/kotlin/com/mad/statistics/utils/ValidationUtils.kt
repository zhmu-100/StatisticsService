package com.mad.statistics.utils

import com.mad.statistics.models.common.ExerciseMetadata
import com.mad.statistics.models.common.GPSPosition
import com.mad.statistics.models.common.UserMetadata

object ValidationUtils {

  fun validateExerciseMetadata(metadata: ExerciseMetadata) {
    require(metadata.id.isNotBlank()) { "Metadata ID cannot be empty" }
    require(metadata.exerciseId.isNotBlank()) { "Exercise ID cannot be empty" }
  }

  fun validateUserMetadata(metadata: UserMetadata) {
    require(metadata.id.isNotBlank()) { "Metadata ID cannot be empty" }
    require(metadata.userId.isNotBlank()) { "User ID cannot be empty" }
  }

  fun validateGPSPositions(positions: List<GPSPosition>) {
    require(positions.isNotEmpty()) { "GPS positions list cannot be empty" }

    positions.forEach { position ->
      require(position.latitude >= -90 && position.latitude <= 90) {
        "Latitude must be between -90 and 90"
      }
      require(position.longitude >= -180 && position.longitude <= 180) {
        "Longitude must be between -180 and 180"
      }
      require(position.accuracy > 0) { "Accuracy must be positive" }
    }
  }

  fun validateHeartRate(bpm: Int) {
    require(bpm > 0) { "Heart rate (BPM) must be positive" }
    require(bpm < 300) { "Heart rate (BPM) is unrealistically high" }
  }

  fun validateCalories(calories: Double) {
    require(calories >= 0) { "Calories must be non-negative" }
  }

  fun validateExerciseId(exerciseId: String) {
    require(exerciseId.isNotBlank()) { "Exercise ID cannot be empty" }
  }

  fun validateUserId(userId: String) {
    require(userId.isNotBlank()) { "User ID cannot be empty" }
  }
}
