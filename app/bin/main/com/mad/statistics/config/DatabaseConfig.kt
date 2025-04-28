package com.mad.statistics.config

import com.clickhouse.jdbc.ClickHouseDataSource
import org.slf4j.LoggerFactory
import java.sql.Connection
import java.util.*
import javax.sql.DataSource

object DatabaseConfig {
    private val logger = LoggerFactory.getLogger(DatabaseConfig::class.java)
    private lateinit var dataSource: DataSource

    fun init() {
        val properties = Properties().apply {
            put("user", AppConfig.databaseUser)
            put("password", AppConfig.databasePassword)
            put("compress", "0")
        }
        dataSource = ClickHouseDataSource(AppConfig.databaseUrl, properties)

        logger.info("Database initialized successfully with URL: ${AppConfig.clickhouseServiceUrl}")
    }

    fun getConnection(): Connection {
        return dataSource.connection
    }
}
