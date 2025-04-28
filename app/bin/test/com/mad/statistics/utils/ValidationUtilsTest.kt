package com.mad.statistics.utils

import com.mad.statistics.models.common.ExerciseMetadata
import com.mad.statistics.models.common.GPSPosition
import com.mad.statistics.models.common.UserMetadata
import kotlinx.datetime.Instant
import org.junit.jupiter.api.Test
import kotlin.test.assertFailsWith
import kotlin.test.assertTrue

class ValidationUtilsTest {

    private val dummyTimestamp = Instant.fromEpochMilliseconds(0)

    @Test
    fun `validateExerciseMetadata passes for valid metadata`() {
        val metadata = ExerciseMetadata(
            id = "meta1",
            exerciseId = "exercise1",
            timestamp = dummyTimestamp
        )
        // Should not throw
        ValidationUtils.validateExerciseMetadata(metadata)
    }

    @Test
    fun `validateExerciseMetadata throws for blank id`() {
        val metadata = ExerciseMetadata(
            id = "",
            exerciseId = "exercise1",
            timestamp = dummyTimestamp
        )
        val ex = assertFailsWith<IllegalArgumentException> {
            ValidationUtils.validateExerciseMetadata(metadata)
        }
        assertTrue(ex.message!!.contains("Metadata ID cannot be empty"))
    }

    @Test
    fun `validateExerciseMetadata throws for blank exerciseId`() {
        val metadata = ExerciseMetadata(
            id = "meta1",
            exerciseId = "",
            timestamp = dummyTimestamp
        )
        val ex = assertFailsWith<IllegalArgumentException> {
            ValidationUtils.validateExerciseMetadata(metadata)
        }
        assertTrue(ex.message!!.contains("Exercise ID cannot be empty"))
    }

    @Test
    fun `validateUserMetadata passes for valid metadata`() {
        val metadata = UserMetadata(
            id = "userMeta1",
            userId = "user1",
            timestamp = dummyTimestamp
        )
        ValidationUtils.validateUserMetadata(metadata)
    }

    @Test
    fun `validateUserMetadata throws for blank id`() {
        val metadata = UserMetadata(
            id = "",
            userId = "user1",
            timestamp = dummyTimestamp
        )
        val ex = assertFailsWith<IllegalArgumentException> {
            ValidationUtils.validateUserMetadata(metadata)
        }
        assertTrue(ex.message!!.contains("Metadata ID cannot be empty"))
    }

    @Test
    fun `validateUserMetadata throws for blank userId`() {
        val metadata = UserMetadata(
            id = "userMeta1",
            userId = "",
            timestamp = dummyTimestamp
        )
        val ex = assertFailsWith<IllegalArgumentException> {
            ValidationUtils.validateUserMetadata(metadata)
        }
        assertTrue(ex.message!!.contains("User ID cannot be empty"))
    }

    @Test
    fun `validateGPSPositions passes for valid list`() {
        val positions = listOf(
            GPSPosition(
                latitude = 0.0,
                longitude = 0.0,
                accuracy = 5.0,
                timestamp = dummyTimestamp,
                altitude = 100.0,
                speed = 1.0
            ),
            GPSPosition(
                latitude = 45.0,
                longitude = 90.0,
                accuracy = 10.0,
                timestamp = dummyTimestamp,
                altitude = 200.0,
                speed = 2.5
            )
        )
        ValidationUtils.validateGPSPositions(positions)
    }

    @Test
    fun `validateGPSPositions throws for empty list`() {
        val ex = assertFailsWith<IllegalArgumentException> {
            ValidationUtils.validateGPSPositions(emptyList())
        }
        assertTrue(ex.message!!.contains("GPS positions list cannot be empty"))
    }

    @Test
    fun `validateGPSPositions throws for invalid latitude`() {
        val positions = listOf(
            GPSPosition(
                latitude = -91.0,
                longitude = 0.0,
                accuracy = 1.0,
                timestamp = dummyTimestamp,
                altitude = 0.0,
                speed = 0.0
            )
        )
        val ex = assertFailsWith<IllegalArgumentException> {
            ValidationUtils.validateGPSPositions(positions)
        }
        assertTrue(ex.message!!.contains("Latitude must be between -90 and 90"))
    }

    @Test
    fun `validateGPSPositions throws for invalid longitude`() {
        val positions = listOf(
            GPSPosition(
                latitude = 0.0,
                longitude = 181.0,
                accuracy = 1.0,
                timestamp = dummyTimestamp,
                altitude = 0.0,
                speed = 0.0
            )
        )
        val ex = assertFailsWith<IllegalArgumentException> {
            ValidationUtils.validateGPSPositions(positions)
        }
        assertTrue(ex.message!!.contains("Longitude must be between -180 and 180"))
    }

    @Test
    fun `validateGPSPositions throws for non-positive accuracy`() {
        val positions = listOf(
            GPSPosition(
                latitude = 0.0,
                longitude = 0.0,
                accuracy = 0.0,
                timestamp = dummyTimestamp,
                altitude = 0.0,
                speed = 0.0
            )
        )
        val ex = assertFailsWith<IllegalArgumentException> {
            ValidationUtils.validateGPSPositions(positions)
        }
        assertTrue(ex.message!!.contains("Accuracy must be positive"))
    }

    @Test
    fun `validateHeartRate passes for valid bpm`() {
        ValidationUtils.validateHeartRate(60)
    }

    @Test
    fun `validateHeartRate throws for zero bpm`() {
        val ex = assertFailsWith<IllegalArgumentException> {
            ValidationUtils.validateHeartRate(0)
        }
        assertTrue(ex.message!!.contains("Heart rate (BPM) must be positive"))
    }

    @Test
    fun `validateHeartRate throws for unrealistically high bpm`() {
        val ex = assertFailsWith<IllegalArgumentException> {
            ValidationUtils.validateHeartRate(500)
        }
        assertTrue(ex.message!!.contains("Heart rate (BPM) is unrealistically high"))
    }

    @Test
    fun `validateCalories passes for zero and positive`() {
        ValidationUtils.validateCalories(0.0)
        ValidationUtils.validateCalories(250.5)
    }

    @Test
    fun `validateCalories throws for negative value`() {
        val ex = assertFailsWith<IllegalArgumentException> {
            ValidationUtils.validateCalories(-10.0)
        }
        assertTrue(ex.message!!.contains("Calories must be non-negative"))
    }

    @Test
    fun `validateExerciseId throws for blank`() {
        val ex = assertFailsWith<IllegalArgumentException> {
            ValidationUtils.validateExerciseId("")
        }
        assertTrue(ex.message!!.contains("Exercise ID cannot be empty"))
    }

    @Test
    fun `validateUserId throws for blank`() {
        val ex = assertFailsWith<IllegalArgumentException> {
            ValidationUtils.validateUserId("")
        }
        assertTrue(ex.message!!.contains("User ID cannot be empty"))
    }
}
