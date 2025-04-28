package com.mad.statistics.services

import com.mad.statistics.models.GPSData
import com.mad.statistics.models.common.ExerciseMetadata
import com.mad.statistics.models.common.GPSPosition
import com.mad.statistics.repositories.GPSRepository
import com.mad.statistics.utils.ValidationUtils
import io.mockk.*
import kotlinx.datetime.Instant
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import kotlin.test.assertEquals

class GPSServiceTest {
    private lateinit var repository: GPSRepository
    private lateinit var service: GPSService

    @BeforeEach
    fun setUp() {
        repository = mockk(relaxed = true)
        mockkObject(ValidationUtils)
        service = GPSService(repository)
    }

    @AfterEach
    fun tearDown() {
        unmockkAll()
    }

    @Test
    fun `saveGPSData with valid data calls repository`() {
        val meta = mockk<ExerciseMetadata>()
        val position = mockk<GPSPosition>()
        val positions = listOf(position)
        val gpsData = GPSData(meta = meta, positions = positions)

        every { ValidationUtils.validateExerciseMetadata(meta) } just Runs
        every { ValidationUtils.validateGPSPositions(positions) } just Runs

        service.saveGPSData(gpsData)

        verify(exactly = 1) { ValidationUtils.validateExerciseMetadata(meta) }
        verify(exactly = 1) { ValidationUtils.validateGPSPositions(positions) }
        verify(exactly = 1) { repository.saveGPSData(gpsData) }
    }

    @Test
    fun `saveGPSData when metadata invalid throws exception and does not call repository`() {
        val meta = mockk<ExerciseMetadata>()
        val positions = listOf(mockk<GPSPosition>())
        val gpsData = GPSData(meta = meta, positions = positions)
        every { ValidationUtils.validateExerciseMetadata(meta) } throws IllegalArgumentException("Invalid metadata")

        assertThrows<IllegalArgumentException> {
            service.saveGPSData(gpsData)
        }
        verify(exactly = 1) { ValidationUtils.validateExerciseMetadata(meta) }
        verify(exactly = 0) { ValidationUtils.validateGPSPositions(any()) }
        verify(exactly = 0) { repository.saveGPSData(any()) }
    }

    @Test
    fun `saveGPSData when positions invalid throws exception and does not call repository`() {

        val meta = mockk<ExerciseMetadata>()
        val positions = listOf(mockk<GPSPosition>())
        val gpsData = GPSData(meta = meta, positions = positions)
        every { ValidationUtils.validateExerciseMetadata(meta) } just Runs
        every { ValidationUtils.validateGPSPositions(positions) } throws IllegalArgumentException("Invalid positions")

        assertThrows<IllegalArgumentException> {
            service.saveGPSData(gpsData)
        }
        verify(exactly = 1) { ValidationUtils.validateExerciseMetadata(meta) }
        verify(exactly = 1) { ValidationUtils.validateGPSPositions(positions) }
        verify(exactly = 0) { repository.saveGPSData(any()) }
    }

    @Test
    fun `getGPSDataByExerciseId with valid id returns data and calls repository`() {
        val exerciseId = "exercise-1"
        val meta1 = mockk<ExerciseMetadata>()
        val meta2 = mockk<ExerciseMetadata>()
        val dataList = listOf(
            GPSData(meta = meta1, positions = listOf(mockk())),
            GPSData(meta = meta2, positions = listOf(mockk()))
        )
        every { ValidationUtils.validateExerciseId(exerciseId) } just Runs
        every { repository.getGPSDataByExerciseId(exerciseId) } returns dataList

        val result = service.getGPSDataByExerciseId(exerciseId)

        verify(exactly = 1) { ValidationUtils.validateExerciseId(exerciseId) }
        verify(exactly = 1) { repository.getGPSDataByExerciseId(exerciseId) }
        assertEquals(dataList, result)
    }

    @Test
    fun `getGPSDataByExerciseId when id invalid throws exception and does not call repository`() {
        val exerciseId = ""
        every { ValidationUtils.validateExerciseId(exerciseId) } throws IllegalArgumentException("Invalid ID")

        assertThrows<IllegalArgumentException> {
            service.getGPSDataByExerciseId(exerciseId)
        }
        verify(exactly = 1) { ValidationUtils.validateExerciseId(exerciseId) }
        verify(exactly = 0) { repository.getGPSDataByExerciseId(any()) }
    }
}
