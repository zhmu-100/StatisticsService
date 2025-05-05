package com.mad.statistics.services

import com.mad.statistics.models.HeartRateData
import com.mad.statistics.models.common.ExerciseMetadata
import com.mad.statistics.repositories.HeartRateRepository
import com.mad.statistics.utils.ValidationUtils
import io.mockk.*
import kotlin.test.assertEquals
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows

class HeartRateServiceTest {
  private lateinit var repository: HeartRateRepository
  private lateinit var service: HeartRateService

  @BeforeEach
  fun setUp() {
    repository = mockk(relaxed = true)
    mockkObject(ValidationUtils)
    service = HeartRateService(repository)
  }

  @AfterEach
  fun tearDown() {
    unmockkAll()
  }

  @Test
  fun `saveHeartRateData with valid data calls repository`() {
    val meta = mockk<ExerciseMetadata>()
    val heartRateData = HeartRateData(meta = meta, bpm = 75)
    every { ValidationUtils.validateExerciseMetadata(meta) } just Runs
    every { ValidationUtils.validateHeartRate(75) } just Runs

    service.saveHeartRateData(heartRateData)

    verify(exactly = 1) { ValidationUtils.validateExerciseMetadata(meta) }
    verify(exactly = 1) { ValidationUtils.validateHeartRate(75) }
    verify(exactly = 1) { repository.saveHeartRateData(heartRateData) }
  }

  @Test
  fun `saveHeartRateData when metadata invalid throws exception and does not call repository`() {
    val meta = mockk<ExerciseMetadata>()
    val heartRateData = HeartRateData(meta = meta, bpm = 75)
    every { ValidationUtils.validateExerciseMetadata(meta) } throws
        IllegalArgumentException("Invalid metadata")

    assertThrows<IllegalArgumentException> { service.saveHeartRateData(heartRateData) }
    verify(exactly = 1) { ValidationUtils.validateExerciseMetadata(meta) }
    verify(exactly = 0) { ValidationUtils.validateHeartRate(any()) }
    verify(exactly = 0) { repository.saveHeartRateData(any()) }
  }

  @Test
  fun `saveHeartRateData when bpm invalid throws exception and does not call repository`() {
    val meta = mockk<ExerciseMetadata>()
    val heartRateData = HeartRateData(meta = meta, bpm = -5)
    every { ValidationUtils.validateExerciseMetadata(meta) } just Runs
    every { ValidationUtils.validateHeartRate(-5) } throws IllegalArgumentException("Invalid bpm")

    assertThrows<IllegalArgumentException> { service.saveHeartRateData(heartRateData) }
    verify(exactly = 1) { ValidationUtils.validateExerciseMetadata(meta) }
    verify(exactly = 1) { ValidationUtils.validateHeartRate(-5) }
    verify(exactly = 0) { repository.saveHeartRateData(any()) }
  }

  @Test
  fun `getHeartRateDataByExerciseId with valid id returns data and calls repository`() {
    val exerciseId = "exercise-1"
    val meta1 = mockk<ExerciseMetadata>()
    val meta2 = mockk<ExerciseMetadata>()
    val expectedList =
        listOf(HeartRateData(meta = meta1, bpm = 80), HeartRateData(meta = meta2, bpm = 85))
    every { ValidationUtils.validateExerciseId(exerciseId) } just Runs
    every { repository.getHeartRateDataByExerciseId(exerciseId) } returns expectedList

    val result = service.getHeartRateDataByExerciseId(exerciseId)

    verify(exactly = 1) { ValidationUtils.validateExerciseId(exerciseId) }
    verify(exactly = 1) { repository.getHeartRateDataByExerciseId(exerciseId) }
    assertEquals(expectedList, result)
  }

  @Test
  fun `getHeartRateDataByExerciseId when id invalid throws exception and does not call repository`() {
    val exerciseId = ""
    every { ValidationUtils.validateExerciseId(exerciseId) } throws
        IllegalArgumentException("Invalid ID")

    assertThrows<IllegalArgumentException> { service.getHeartRateDataByExerciseId(exerciseId) }
    verify(exactly = 1) { ValidationUtils.validateExerciseId(exerciseId) }
    verify(exactly = 0) { repository.getHeartRateDataByExerciseId(any()) }
  }
}
