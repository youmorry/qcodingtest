import org.jooq.meta.jaxb.Logging

// Flyway の Gradle タスク（flywayMigrate）が PostgreSQL へ接続するために、
// JDBC ドライバと DB 固有モジュールを buildscript のクラスパスへ載せる。
buildscript {
    repositories {
        mavenCentral()
    }
    dependencies {
        classpath("org.flywaydb:flyway-database-postgresql:12.4.0")
        classpath("org.postgresql:postgresql:42.7.11")
    }
}

plugins {
    kotlin("jvm") version "2.3.21"
    kotlin("plugin.spring") version "2.3.21"
    id("org.springframework.boot") version "4.1.0"
    id("io.spring.dependency-management") version "1.1.7"
    id("org.flywaydb.flyway") version "12.4.0"
    id("org.jooq.jooq-codegen-gradle") version "3.21.5"
    id("org.jlleitschuh.gradle.ktlint") version "14.2.0"
}

group = "com.example"
version = "0.0.1-SNAPSHOT"

java {
    toolchain {
        languageVersion = JavaLanguageVersion.of(25)
    }
}

repositories {
    mavenCentral()
}

dependencies {
    implementation("org.springframework.boot:spring-boot-starter")
    implementation("org.springframework.boot:spring-boot-starter-jooq")
    implementation("org.flywaydb:flyway-core")
    runtimeOnly("org.flywaydb:flyway-database-postgresql")
    runtimeOnly("org.postgresql:postgresql")
    implementation("org.jetbrains.kotlin:kotlin-reflect")
    testImplementation("org.springframework.boot:spring-boot-starter-test")
    testImplementation("org.jetbrains.kotlin:kotlin-test-junit5")
    testRuntimeOnly("org.junit.platform:junit-platform-launcher")

    // jOOQ コード生成タスクが PostgreSQL へ接続するためのドライバ。
    jooqCodegen("org.postgresql:postgresql")
}

// ローカルの PostgreSQL（Docker Compose）への接続情報。
// jOOQ のコード生成元はこの DB に Flyway で適用したスキーマとする（README 2.3）。
val dbUrl = "jdbc:postgresql://localhost:5432/qcodingtest"
val dbUser = "qcodingtest"
val dbPassword = "qcodingtest"

// jOOQ 生成コードの出力先（バージョン管理対象）。スキーマ変更時のみ再生成してコミットする。
val jooqOutputDir = "src/generated/jooq"

flyway {
    url = dbUrl
    user = dbUser
    password = dbPassword
    locations = arrayOf("filesystem:src/main/resources/db/migration")
}

jooq {
    configuration {
        logging = Logging.WARN
        jdbc {
            driver = "org.postgresql.Driver"
            url = dbUrl
            user = dbUser
            password = dbPassword
        }
        generator {
            name = "org.jooq.codegen.KotlinGenerator"
            database {
                name = "org.jooq.meta.postgres.PostgresDatabase"
                inputSchema = "public"
                // Flyway の管理テーブルは生成対象から除外する。
                excludes = "flyway_schema_history"
            }
            generate {
                isDeprecated = false
                isPojos = false
                isDaos = false
            }
            target {
                packageName = "com.example.qcodingtest.jooq"
                // 生成コードはバージョン管理対象とするため、build/ ではなくソースツリーへ出力する。
                directory = jooqOutputDir
            }
        }
    }
}

// 生成された jOOQ コードをコンパイル対象へ追加する。
sourceSets.main {
    kotlin.srcDir(jooqOutputDir)
}

kotlin {
    compilerOptions {
        freeCompilerArgs.addAll("-Xjsr305=strict", "-Xannotation-default-target=param-property")
    }
}

// 生成フロー: Flyway でスキーマ適用 → jOOQ がそのスキーマからコード生成。
// 生成コードはバージョン管理対象のため、通常のコンパイルは再生成に依存させない
// （DB が無くてもビルド・テスト可能）。スキーマ変更時のみ `./gradlew jooqCodegen` を実行し、
// 差分をコミットする運用とする。
tasks.named("jooqCodegen") {
    dependsOn(tasks.named("flywayMigrate"))
}

tasks.withType<Test> {
    useJUnitPlatform()
}

ktlint {
    // jOOQ の自動生成コードは整形対象外とする。
    filter {
        exclude { it.file.path.contains(jooqOutputDir) }
    }
}
