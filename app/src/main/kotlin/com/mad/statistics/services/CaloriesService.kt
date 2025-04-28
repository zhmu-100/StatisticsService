package com.mad.statistics.services

import com.mad.statistics.models.CaloriesData
import com.mad.statistics.repositories.CaloriesRepository
import com.mad.statistics.utils.ValidationUtils

class CaloriesService(private val caloriesRepository: CaloriesRepository) {
    
    fun saveCaloriesData(caloriesData: CaloriesData) {
        // Валидация данных
        ValidationUtils.validateUserMetadata(caloriesData.meta)
        ValidationUtils.validateCalories(caloriesData.calories)
        
        caloriesRepository.saveCaloriesData(caloriesData)
    }
    
    fun getCaloriesDataByUserId(userId: String): List<CaloriesData> {
        // Валидация ID пользователя
        ValidationUtils.validateUserId(userId)
        
        return caloriesRepository.getCaloriesDataByUserId(userId)
    }
}
