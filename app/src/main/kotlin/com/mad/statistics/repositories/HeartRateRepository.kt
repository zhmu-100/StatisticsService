package com.mad.statistics.repositories

import com.mad.statistics.clients.ClickHouseServiceClient
import com.mad.statistics.models.HeartRateData
import com.mad.statistics.models.common.ExerciseMetadata
import com.mad.statistics.utils.toClickHouseDateTime
import kotlinx.coroutines.runBlocking
import kotlinx.datetime.Instant
import kotlinx.serialization.json.*
import java.util.UUID

class HeartRateRepository(clickHouseServiceClient: ClickHouseServiceClient) : RepositoryBase(clickHouseServiceClient) {
    
    fun saveHeartRateData(heartRateData: HeartRateData) {
        runBlocking {
            val dataToInsert = buildJsonObject {
                put("id", heartRateData.meta.id.ifEmpty { UUID.randomUUID().toString() })
                put("exercise_id", heartRateData.meta.exerciseId)
                put("timestamp", heartRateData.meta.timestamp.toClickHouseDateTime())
                put("bpm", heartRateData.bpm)
            }
            
            clickHouseServiceClient.insert("heart_rate_data", listOf(dataToInsert))
        }
    }

    fun getHeartRateDataByExerciseId(exerciseId: String): List<HeartRateData> {
        return try {
            val columns = listOf("id", "exercise_id", "timestamp", "bpm")
            val filters = mapOf("exercise_id" to exerciseId)
            val orderBy = "timestamp ASC"
            
            val result = runBlocking {
                clickHouseServiceClient.select("heart_rate_data", columns, filters, orderBy)
            }
            
            // Преобразуем результат в список HeartRateData
            result.mapNotNull { element ->
                try {
                    if (element !is JsonObject) {
                        logger.warn("Expected JsonObject but got ${element::class.simpleName}")
                        return@mapNotNull null
                    }
                    
                    val id = element["id"]?.jsonPrimitive?.contentOrNull ?: UUID.randomUUID().toString()
                    val exerciseIdFromDb = element["exercise_id"]?.jsonPrimitive?.contentOrNull ?: ""
                    val timestampStr = element["timestamp"]?.jsonPrimitive?.contentOrNull ?: ""
                    val bpm = element["bpm"]?.jsonPrimitive?.intOrNull ?: 0
                    
                    val timestamp = try {
                        Instant.parse(timestampStr)
                    } catch (e: Exception) {
                        logger.error("Error parsing timestamp: $timestampStr", e)
                        Instant.fromEpochMilliseconds(0)
                    }
                    
                    HeartRateData(
                        meta = ExerciseMetadata(
                            id = id,
                            exerciseId = exerciseIdFromDb,
                            timestamp = timestamp
                        ),
                        bpm = bpm
                    )
                } catch (e: Exception) {
                    logger.error("Error mapping heart rate data: ${e.message}", e)
                    null
                }
            }
        } catch (e: Exception) {
            logger.error("Error retrieving heart rate data: ${e.message}", e)
            emptyList()
        }
    }
}