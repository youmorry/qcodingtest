package com.example.qcodingtest.repository

import com.example.qcodingtest.testutil.SharedPostgresContainer
import org.springframework.boot.testcontainers.service.connection.ServiceConnection
import org.testcontainers.postgresql.PostgreSQLContainer

abstract class AbstractRepositoryTest {
    companion object {
        @JvmStatic
        @ServiceConnection
        val postgres: PostgreSQLContainer = SharedPostgresContainer.INSTANCE
    }
}
