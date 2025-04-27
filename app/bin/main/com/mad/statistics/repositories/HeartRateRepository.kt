package com.mad.statistics.repositories

import com.mad.statistics.clients.ClickHouseServiceClient
import com.mad.statistics.models.HeartRateData
import com.mad.statistics.models.common.ExerciseMetadata
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
                put("timestamp", heartRateData.meta.timestamp.toString())
                put("bpm", heartRateData.bpm)
            }
            
            clickHouseServiceClient.insert("heart_rate_data", listOf(dataToInsert))
        }
    }
    
    fun getHeartRateDataByExerciseId(exerciseId: String): List<HeartRateData> {
        return runBlocking {
            val columns = listOf("id", "exercise_id", "timestamp", "bpm")
            val filters = mapOf("exercise_id" to exerciseId)
            val orderBy = "timestamp ASC"
            
            val result = clickHouseServiceClient.select("heart_rate_data", columns, filters, orderBy)
            
            result.map { row ->
                val id = row.jsonObject["id"]?.jsonPrimitive?.content ?: UUID.randomUUID().toString()
                val exerciseIdFromDb = row.jsonObject["exercise_id"]?.jsonPrimitive?.content ?: ""
                val timestamp = Instant.parse(row.jsonObject["timestamp"]?.jsonPrimitive?.content ?: "")
                val bpm = row.jsonObject["bpm"]?.jsonPrimitive?.int ?: 0
                
                HeartRateData(
                    meta = ExerciseMetadata(
                        id = id,
                        exerciseId = exerciseIdFromDb,
                        timestamp = timestamp
                    ),
                    bpm = bpm
                )
            }
        }
    }
}