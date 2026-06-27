package com.example.qcodingtest.infrastructure.repository

import com.example.qcodingtest.testutil.SharedPostgresContainer
import org.springframework.boot.testcontainers.service.connection.ServiceConnection
import org.testcontainers.postgresql.PostgreSQLContainer

/**
 * リポジトリのスライステスト（`@JooqTest`）の共通基底クラス。
 *
 * フルコンテキストを起動せずコンテナ共有のみを担う（スライス注釈は具象テスト側に置く）。
 * 共有 PostgreSQL コンテナ（[SharedPostgresContainer]）に `@ServiceConnection` で接続し、
 * Flyway マイグレーションはスライス起動時に自動適用される。
 */
abstract class AbstractRepositoryTest {
    companion object {
        @JvmStatic
        @ServiceConnection
        val postgres: PostgreSQLContainer = SharedPostgresContainer.INSTANCE
    }
}
