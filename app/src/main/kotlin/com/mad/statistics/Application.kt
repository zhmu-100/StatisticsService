package com.mad.statistics

import com.mad.statistics.config.AppConfig
import com.mad.statistics.config.DatabaseConfig
import com.mad.statistics.config.configureKoin
import com.mad.statistics.plugins.configureErrorHandling
import com.mad.statistics.routes.configureCaloriesRoutes
import com.mad.statistics.routes.configureGPSRoutes
import com.mad.statistics.routes.configureHeartRateRoutes
import com.example.datalogger.redis.RedisLoggerImpl
import io.ktor.serialization.kotlinx.json.*
import io.ktor.server.application.*
import io.ktor.server.engine.*
import io.ktor.server.netty.*
import io.ktor.server.plugins.callloging.*
import io.ktor.server.plugins.contentnegotiation.*
import io.ktor.server.plugins.cors.routing.*
import kotlinx.serialization.json.Json
import org.koin.ktor.ext.inject
import org.slf4j.LoggerFactory
import org.slf4j.event.Level

fun main() {
    val logger = LoggerFactory.getLogger("com.mad.statistics.Application")
    logger.info("Starting Statistics Service with configuration:")
    logger.info("Port: ${AppConfig.port}")
    logger.info("Database Mode: ${AppConfig.dbMode}")
    logger.info("Database Host: ${AppConfig.dbHost}")
    logger.info("Database Port: ${AppConfig.dbPort}")
    logger.info("ClickHouse Service URL: ${AppConfig.clickhouseServiceUrl}")
    logger.info("Redis Host: ${AppConfig.redisHost}")
    logger.info("Redis Port: ${AppConfig.redisPort}")

    embeddedServer(Netty, port = AppConfig.port, host = "0.0.0.0") {
        module()
    }.start(wait = true)
}

fun Application.module() {
    DatabaseConfig.init()
    
    // Инициализация Koin
    configureKoin()
    
    // Получаем экземпляр RedisLoggerImpl для закрытия соединения при остановке
    val redisLogger by inject<RedisLoggerImpl>()
    
    // Настраиваем обработку событий жизненного цикла приложения
    environment.monitor.subscribe(ApplicationStopping) {
        // Закрываем соединение с Redis при остановке приложения
        redisLogger.close()
    }
    
    install(ContentNegotiation) {
        json(Json {
            prettyPrint = true
            isLenient = true
            ignoreUnknownKeys = true
        })
    }
    
    install(CallLogging) {
        level = Level.INFO
    }
    
    install(CORS) {
        anyHost()
        allowHeader("Content-Type")
        allowHeader("X-User-Id") // Добавляем заголовок для идентификации пользователя
        allowMethod(io.ktor.http.HttpMethod.Options)
        allowMethod(io.ktor.http.HttpMethod.Get)
        allowMethod(io.ktor.http.HttpMethod.Post)
    }
    
    // Используем наш новый обработчик ошибок с логированием
    configureErrorHandling()
    
    configureGPSRoutes()
    configureHeartRateRoutes()
    configureCaloriesRoutes()
}