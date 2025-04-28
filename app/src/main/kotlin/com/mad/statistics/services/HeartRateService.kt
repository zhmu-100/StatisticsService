package com.mad.statistics.services

import com.mad.statistics.models.HeartRateData
import com.mad.statistics.repositories.HeartRateRepository
import com.mad.statistics.utils.ValidationUtils

class HeartRateService(private val heartRateRepository: HeartRateRepository) {
    
    fun saveHeartRateData(heartRateData: HeartRateData) {
        // Валидация данных
        ValidationUtils.validateExerciseMetadata(heartRateData.meta)
        ValidationUtils.validateHeartRate(heartRateData.bpm)
        
        heartRateRepository.saveHeartRateData(heartRateData)
    }
    
    fun getHeartRateDataByExerciseId(exerciseId: String): List<HeartRateData> {
        // Валидация ID упражнения
        ValidationUtils.validateExerciseId(exerciseId)
        
        return heartRateRepository.getHeartRateDataByExerciseId(exerciseId)
    }
}
