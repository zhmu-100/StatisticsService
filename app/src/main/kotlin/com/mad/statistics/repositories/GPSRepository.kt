package com.mad.statistics.repositories

import com.mad.statistics.clients.ClickHouseServiceClient
import com.mad.statistics.models.GPSData
import com.mad.statistics.models.common.ExerciseMetadata
import com.mad.statistics.models.common.GPSPosition
import com.mad.statistics.utils.toClickHouseDateTime
import com.mad.statistics.utils.parseClickHouseDateTime

import kotlinx.coroutines.runBlocking
import kotlinx.datetime.Instant
import kotlinx.serialization.json.*
import java.util.UUID

class GPSRepository(clickHouseServiceClient: ClickHouseServiceClient) : RepositoryBase(clickHouseServiceClient) {
    
    fun saveGPSData(gpsData: GPSData) {
        runBlocking {
            val dataToInsert = gpsData.positions.map { position ->
                buildJsonObject {
                    put("id", UUID.randomUUID().toString())
                    put("exercise_id", gpsData.meta.exerciseId)
                    put("timestamp", gpsData.meta.timestamp.toClickHouseDateTime())
                    put("position_timestamp", position.timestamp.toClickHouseDateTime())
                    put("latitude", position.latitude)
                    put("longitude", position.longitude)
                    put("altitude", position.altitude)
                    put("speed", position.speed)
                    put("accuracy", position.accuracy)
                }
            }
            
            clickHouseServiceClient.insert("gps_data", dataToInsert)
        }
    }
    
    fun getGPSDataByExerciseId(exerciseId: String): List<GPSData> {
        return try {
            val columns = listOf("id", "exercise_id", "timestamp", "position_timestamp", 
                               "latitude", "longitude", "altitude", "speed", "accuracy")
            val filters = mapOf("exercise_id" to exerciseId)
            val orderBy = "timestamp ASC, position_timestamp ASC"
            
            val result = runBlocking {
                clickHouseServiceClient.select("gps_data", columns, filters, orderBy)
            }
            
            val groupedResults = result.mapNotNull { element ->
                try {
                    if (element !is JsonObject) {
                        logWarn("Expected JsonObject but got ${element::class.simpleName}")
                        return@mapNotNull null
                    }
                    
                    val id = element["id"]?.jsonPrimitive?.contentOrNull ?: UUID.randomUUID().toString()
                    val exerciseIdFromDb = element["exercise_id"]?.jsonPrimitive?.contentOrNull ?: ""
                    val timestampStr = element["timestamp"]?.jsonPrimitive?.contentOrNull ?: ""
                    val positionTimestampStr = element["position_timestamp"]?.jsonPrimitive?.contentOrNull ?: ""
                    val latitude = element["latitude"]?.jsonPrimitive?.doubleOrNull ?: 0.0
                    val longitude = element["longitude"]?.jsonPrimitive?.doubleOrNull ?: 0.0
                    val altitude = element["altitude"]?.jsonPrimitive?.doubleOrNull ?: 0.0
                    val speed = element["speed"]?.jsonPrimitive?.doubleOrNull ?: 0.0
                    val accuracy = element["accuracy"]?.jsonPrimitive?.doubleOrNull ?: 0.0
                    
                    val timestamp = parseClickHouseDateTime(timestampStr)
                    val positionTimestamp = parseClickHouseDateTime(positionTimestampStr)
                    
                    Triple(
                        exerciseIdFromDb,
                        ExerciseMetadata(id = id, exerciseId = exerciseIdFromDb, timestamp = timestamp),
                        GPSPosition(
                            timestamp = positionTimestamp,
                            latitude = latitude,
                            longitude = longitude,
                            altitude = altitude,
                            speed = speed,
                            accuracy = accuracy
                        )
                    )
                } catch (e: Exception) {
                    logError("Error mapping GPS data: ${e.message}", e)
                    null
                }
            }.groupBy { it.first }
            
            // Преобразуем сгруппированные результаты в список GPSData
            groupedResults.map { (exerciseId, triples) ->
                val metadata = triples.first().second
                val positions = triples.map { it.third }
                
                GPSData(
                    meta = metadata,
                    positions = positions
                )
            }
        } catch (e: Exception) {
            logError("Error retrieving GPS data: ${e.message}", e)
            emptyList()
        }
    }
}