// KoinConfig.kt — оставить только это:

package com.mad.statistics.config

import com.mad.client.LoggerClient
import com.mad.statistics.clients.ClickHouseServiceClient
import com.mad.statistics.repositories.CaloriesRepository
import com.mad.statistics.repositories.GPSRepository
import com.mad.statistics.repositories.HeartRateRepository
import com.mad.statistics.services.CaloriesService
import com.mad.statistics.services.GPSService
import com.mad.statistics.services.HeartRateService
import io.ktor.server.application.*
import io.ktor.server.config.*
import org.koin.dsl.module
import org.koin.ktor.plugin.Koin

fun Application.configureKoin() {
  val redisHost = System.getenv("REDIS_HOST") ?: "localhost"
  val redisPort = System.getenv("REDIS_PORT")?.toIntOrNull() ?: 6379
  val redisPassword = System.getenv("REDIS_PASSWORD") ?: ""

  install(Koin) {
    properties(
        mapOf(
            "redis.host" to redisHost,
            "redis.port" to redisPort.toString(),
            "redis.password" to redisPassword))
    modules(appModule, loggingModule)
  }
}

val appModule = module {
  single { ClickHouseServiceClient() }
  single { GPSRepository(get()) }
  single { HeartRateRepository(get()) }
  single { CaloriesRepository(get()) }
  single { GPSService(get()) }
  single { HeartRateService(get()) }
  single { CaloriesService(get()) }
}

// Модуль для логирования
val loggingModule = module {
  single {
    LoggerClient(
            host = getProperty("redis.host", "localhost"),
            port = getProperty("redis.port", "6379").toInt(),
            password = getProperty("redis.password", ""))
        .also { logger ->
          // Закрываем Redis-клиент при остановке JVM
          Runtime.getRuntime().addShutdownHook(Thread { logger.close() })
        }
  }
}
