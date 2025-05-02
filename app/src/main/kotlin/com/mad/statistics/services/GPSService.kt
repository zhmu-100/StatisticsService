package com.mad.statistics.services

import com.mad.statistics.models.GPSData
import com.mad.statistics.repositories.GPSRepository
import com.mad.statistics.utils.ValidationUtils

class GPSService(private val gpsRepository: GPSRepository) {

  fun saveGPSData(gpsData: GPSData) {
    // Валидация данных
    ValidationUtils.validateExerciseMetadata(gpsData.meta)
    ValidationUtils.validateGPSPositions(gpsData.positions)

    gpsRepository.saveGPSData(gpsData)
  }

  fun getGPSDataByExerciseId(exerciseId: String): List<GPSData> {
    // Валидация ID упражнения
    ValidationUtils.validateExerciseId(exerciseId)

    return gpsRepository.getGPSDataByExerciseId(exerciseId)
  }
}
