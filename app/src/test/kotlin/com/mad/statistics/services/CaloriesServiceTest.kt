package com.mad.statistics.services

import com.mad.statistics.models.CaloriesData
import com.mad.statistics.models.common.UserMetadata
import com.mad.statistics.repositories.CaloriesRepository
import com.mad.statistics.utils.ValidationUtils
import io.mockk.*
import kotlin.test.assertEquals
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows

class CaloriesServiceTest {
  private lateinit var repository: CaloriesRepository
  private lateinit var service: CaloriesService

  @BeforeEach
  fun setUp() {
    repository = mockk(relaxed = true)
    mockkObject(ValidationUtils)
    service = CaloriesService(repository)
  }

  @AfterEach
  fun tearDown() {
    unmockkAll()
  }

  @Test
  fun `saveCaloriesData with valid data calls repository`() {
    val meta = mockk<UserMetadata>()
    val caloriesData = CaloriesData(meta = meta, calories = 250.5)
    every { ValidationUtils.validateUserMetadata(meta) } just Runs
    every { ValidationUtils.validateCalories(250.5) } just Runs

    service.saveCaloriesData(caloriesData)

    verify(exactly = 1) { ValidationUtils.validateUserMetadata(meta) }
    verify(exactly = 1) { ValidationUtils.validateCalories(250.5) }
    verify(exactly = 1) { repository.saveCaloriesData(caloriesData) }
  }

  @Test
  fun `saveCaloriesData when metadata invalid throws exception and does not call repository`() {
    val meta = mockk<UserMetadata>()
    val caloriesData = CaloriesData(meta = meta, calories = 100.0)
    every { ValidationUtils.validateUserMetadata(meta) } throws
        IllegalArgumentException("Invalid metadata")

    assertThrows<IllegalArgumentException> { service.saveCaloriesData(caloriesData) }
    verify(exactly = 1) { ValidationUtils.validateUserMetadata(meta) }
    verify(exactly = 0) { ValidationUtils.validateCalories(any()) }
    verify(exactly = 0) { repository.saveCaloriesData(any()) }
  }

  @Test
  fun `saveCaloriesData when calories invalid throws exception and does not call repository`() {
    val meta = mockk<UserMetadata>()
    val caloriesData = CaloriesData(meta = meta, calories = -50.0)
    every { ValidationUtils.validateUserMetadata(meta) } just Runs
    every { ValidationUtils.validateCalories(-50.0) } throws
        IllegalArgumentException("Invalid calories")

    assertThrows<IllegalArgumentException> { service.saveCaloriesData(caloriesData) }
    verify(exactly = 1) { ValidationUtils.validateUserMetadata(meta) }
    verify(exactly = 1) { ValidationUtils.validateCalories(-50.0) }
    verify(exactly = 0) { repository.saveCaloriesData(any()) }
  }

  @Test
  fun `getCaloriesDataByUserId with valid id returns data and calls repository`() {
    val userId = "user-123"
    val meta1 = mockk<UserMetadata>()
    val meta2 = mockk<UserMetadata>()
    val dataList =
        listOf(
            CaloriesData(meta = meta1, calories = 200.0),
            CaloriesData(meta = meta2, calories = 350.0))
    every { ValidationUtils.validateUserId(userId) } just Runs
    every { repository.getCaloriesDataByUserId(userId) } returns dataList

    val result = service.getCaloriesDataByUserId(userId)

    verify(exactly = 1) { ValidationUtils.validateUserId(userId) }
    verify(exactly = 1) { repository.getCaloriesDataByUserId(userId) }
    assertEquals(dataList, result)
  }

  @Test
  fun `getCaloriesDataByUserId when id invalid throws exception and does not call repository`() {
    val userId = ""
    every { ValidationUtils.validateUserId(userId) } throws
        IllegalArgumentException("Invalid userId")

    assertThrows<IllegalArgumentException> { service.getCaloriesDataByUserId(userId) }
    verify(exactly = 1) { ValidationUtils.validateUserId(userId) }
    verify(exactly = 0) { repository.getCaloriesDataByUserId(any()) }
  }
}
