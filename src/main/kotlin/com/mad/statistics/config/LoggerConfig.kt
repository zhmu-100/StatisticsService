package com.mad.statistics.config

import com.example.datalogger.config.DataLoggerConfig
import com.example.datalogger.logger.ActivityLogger
import com.example.datalogger.logger.ErrorLogger
import com.example.datalogger.redis.RedisLoggerImpl
import org.koin.dsl.module

/** Конфигурация для библиотеки логирования */
val loggerModule = module {
  // Создаем конфигурацию логгера
  single {
    DataLoggerConfig(
        redisHost = AppConfig.redisHost,
        redisPort = AppConfig.redisPort,
        redisPassword = AppConfig.redisPassword)
  }

  // Создаем экземпляр логгера
  single {
    val config = get<DataLoggerConfig>()
    config.createLogger()
  }

  // Предоставляем интерфейсы для логирования
  single<ActivityLogger> { get<RedisLoggerImpl>() }
  single<ErrorLogger> { get<RedisLoggerImpl>() }
}
