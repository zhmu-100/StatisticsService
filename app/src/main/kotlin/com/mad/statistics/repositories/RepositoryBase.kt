package com.mad.statistics.repositories

import com.mad.statistics.clients.ClickHouseServiceClient
import org.slf4j.LoggerFactory

abstract class RepositoryBase(protected val clickHouseServiceClient: ClickHouseServiceClient) {
    protected val logger = LoggerFactory.getLogger(this::class.java)
}