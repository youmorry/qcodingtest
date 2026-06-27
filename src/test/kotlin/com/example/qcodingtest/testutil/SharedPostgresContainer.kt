package com.example.qcodingtest.testutil

import org.testcontainers.postgresql.PostgreSQLContainer
import org.testcontainers.utility.DockerImageName

/**
 * DB を伴うテスト全体で共有する PostgreSQL コンテナ（Singleton Container パターン）。
 *
 * JVM 内で 1 つだけ起動し、フルコンテキストの統合テストとリポジトリのスライステストの
 * 双方から使い回す。破棄は Testcontainers の Ryuk が JVM 終了時に行う。
 */
object SharedPostgresContainer {
    val INSTANCE: PostgreSQLContainer =
        PostgreSQLContainer(DockerImageName.parse("postgres:17")).apply { start() }
}
