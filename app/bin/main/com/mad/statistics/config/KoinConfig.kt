package com.mad.statistics.config

import com.mad.statistics.clients.ClickHouseServiceClient
import com.mad.statistics.repositories.CaloriesRepository
import com.mad.statistics.repositories.GPSRepository
import com.mad.statistics.repositories.HeartRateRepository
import com.mad.statistics.services.CaloriesService
import com.mad.statistics.services.GPSService
import com.mad.statistics.services.HeartRateService
import io.ktor.server.application.*
import org.koin.dsl.module
import org.koin.ktor.plugin.Koin
import org.koin.logger.slf4jLogger

fun Application.configureKoin() {
    install(Koin) {
        slf4jLogger()
        modules(appModule)
    }
}

val appModule = module {
    // ClickHouse Service Client
    single { ClickHouseServiceClient(AppConfig.clickhouseServiceUrl) }
    
    // Репозитории
    single { GPSRepository(get()) }
    single { HeartRateRepository(get()) }
    single { CaloriesRepository(get()) }
    
    // Сервисы
    single { GPSService(get()) }
    single { HeartRateService(get()) }
    single { CaloriesService(get()) }
}