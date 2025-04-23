package com.mad.statistics.repositories

import com.mad.statistics.config.DatabaseConfig
import org.slf4j.LoggerFactory
import java.sql.Connection

abstract class RepositoryBase {
    protected val logger = LoggerFactory.getLogger(this::class.java)
    
    protected fun getConnection(): Connection {
        return DatabaseConfig.getConnection()
    }
}
