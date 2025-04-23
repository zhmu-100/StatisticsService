package com.mad.statistics.repositories

import com.mad.statistics.models.GPSData
import com.mad.statistics.models.common.ExerciseMetadata
import com.mad.statistics.models.common.GPSPosition
import com.mad.statistics.utils.toJavaInstant
import kotlinx.datetime.Instant
import kotlinx.datetime.toKotlinInstant
import java.sql.ResultSet
import java.sql.Timestamp
import java.util.UUID

class GPSRepository : RepositoryBase() {
    
    fun saveGPSData(gpsData: GPSData) {
        getConnection().use { connection ->
            val sql = """
                INSERT INTO gps_data (
                    id, exercise_id, timestamp, position_timestamp, 
                    latitude, longitude, altitude, speed, accuracy
                ) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)
            """.trimIndent()
            
            connection.prepareStatement(sql).use { statement ->
                for (position in gpsData.positions) {
                    val id = UUID.randomUUID().toString()
                    
                    statement.setString(1, id)
                    statement.setString(2, gpsData.meta.exerciseId)
                    statement.setTimestamp(3, Timestamp.from(gpsData.meta.timestamp.toJavaInstant()))
                    statement.setTimestamp(4, Timestamp.from(position.timestamp.toJavaInstant()))
                    statement.setDouble(5, position.latitude)
                    statement.setDouble(6, position.longitude)
                    statement.setDouble(7, position.altitude)
                    statement.setDouble(8, position.speed)
                    statement.setDouble(9, position.accuracy)
                    
                    statement.addBatch()
                }
                
                statement.executeBatch()
            }
        }
    }
    
    fun getGPSDataByExerciseId(exerciseId: String): List<GPSData> {
        val result = mutableMapOf<String, MutableList<GPSPosition>>()
        val metadata = mutableMapOf<String, ExerciseMetadata>()
        
        getConnection().use { connection ->
            val sql = """
                SELECT id, exercise_id, timestamp, position_timestamp, 
                       latitude, longitude, altitude, speed, accuracy
                FROM gps_data
                WHERE exercise_id = ?
                ORDER BY timestamp, position_timestamp
            """.trimIndent()
            
            connection.prepareStatement(sql).use { statement ->
                statement.setString(1, exerciseId)
                
                statement.executeQuery().use { resultSet ->
                    while (resultSet.next()) {
                        val id = resultSet.getString("id")
                        val exerciseIdFromDb = resultSet.getString("exercise_id")
                        val timestamp = resultSet.getTimestamp("timestamp").toInstant().toKotlinInstant()
                        
                        if (!metadata.containsKey(exerciseIdFromDb)) {
                            metadata[exerciseIdFromDb] = ExerciseMetadata(
                                id = id,
                                exerciseId = exerciseIdFromDb,
                                timestamp = timestamp
                            )
                        }
                        
                        val position = createGPSPositionFromResultSet(resultSet)
                        
                        if (!result.containsKey(exerciseIdFromDb)) {
                            result[exerciseIdFromDb] = mutableListOf()
                        }
                        
                        result[exerciseIdFromDb]?.add(position)
                    }
                }
            }
        }
        
        return result.map { (exerciseId, positions) ->
            GPSData(
                meta = metadata[exerciseId] ?: error("Metadata not found for exercise ID: $exerciseId"),
                positions = positions
            )
        }
    }
    
    private fun createGPSPositionFromResultSet(resultSet: ResultSet): GPSPosition {
        val positionTimestamp = resultSet.getTimestamp("position_timestamp").toInstant().toKotlinInstant()
        val latitude = resultSet.getDouble("latitude")
        val longitude = resultSet.getDouble("longitude")
        val altitude = resultSet.getDouble("altitude")
        val speed = resultSet.getDouble("speed")
        val accuracy = resultSet.getDouble("accuracy")
        
        return GPSPosition(
            timestamp = positionTimestamp,
            latitude = latitude,
            longitude = longitude,
            altitude = altitude,
            speed = speed,
            accuracy = accuracy
        )
    }
}