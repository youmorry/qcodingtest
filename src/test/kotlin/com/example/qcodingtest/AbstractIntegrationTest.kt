package com.example.qcodingtest

import com.example.qcodingtest.testutil.SharedPostgresContainer
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.testcontainers.service.connection.ServiceConnection
import org.testcontainers.postgresql.PostgreSQLContainer

/**
 * フルコンテキストを伴う統合テストの共通基底クラス。
 *
 * 共有 PostgreSQL コンテナ（[SharedPostgresContainer]）に `@ServiceConnection` で接続し、
 * データソース設定は Spring Boot が自動で解決する。
 */
@SpringBootTest
abstract class AbstractIntegrationTest {
    companion object {
        @JvmStatic
        @ServiceConnection
        val postgres: PostgreSQLContainer = SharedPostgresContainer.INSTANCE
    }
}
