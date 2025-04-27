package com.mad.statistics.repositories

import com.mad.statistics.clients.ClickHouseServiceClient
import com.mad.statistics.models.GPSData
import com.mad.statistics.models.common.ExerciseMetadata
import com.mad.statistics.models.common.GPSPosition
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
                    put("timestamp", gpsData.meta.timestamp.toString())
                    put("position_timestamp", position.timestamp.toString())
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
        return runBlocking {
            val columns = listOf("id", "exercise_id", "timestamp", "position_timestamp", 
                               "latitude", "longitude", "altitude", "speed", "accuracy")
            val filters = mapOf("exercise_id" to exerciseId)
            val orderBy = "timestamp ASC, position_timestamp ASC"
            
            val result = clickHouseServiceClient.select("gps_data", columns, filters, orderBy)
            
            // Группировка результатов по exercise_id и timestamp
            val groupedResults = result.groupBy { 
                val exerciseId = it.jsonObject["exercise_id"]?.jsonPrimitive?.content ?: ""
                val timestamp = it.jsonObject["timestamp"]?.jsonPrimitive?.content ?: ""
                Pair(exerciseId, timestamp)
            }
            
            groupedResults.map { (key, rows) ->
                val (exerciseId, timestamp) = key
                val id = rows.firstOrNull()?.jsonObject?.get("id")?.jsonPrimitive?.content ?: UUID.randomUUID().toString()
                
                val positions = rows.map { row ->
                    GPSPosition(
                        timestamp = Instant.parse(row.jsonObject["position_timestamp"]?.jsonPrimitive?.content ?: ""),
                        latitude = row.jsonObject["latitude"]?.jsonPrimitive?.double ?: 0.0,
                        longitude = row.jsonObject["longitude"]?.jsonPrimitive?.double ?: 0.0,
                        altitude = row.jsonObject["altitude"]?.jsonPrimitive?.double ?: 0.0,
                        speed = row.jsonObject["speed"]?.jsonPrimitive?.double ?: 0.0,
                        accuracy = row.jsonObject["accuracy"]?.jsonPrimitive?.double ?: 0.0
                    )
                }
                
                GPSData(
                    meta = ExerciseMetadata(
                        id = id,
                        exerciseId = exerciseId,
                        timestamp = Instant.parse(timestamp)
                    ),
                    positions = positions
                )
            }
        }
    }
}