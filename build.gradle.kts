import org.jooq.meta.jaxb.Logging

// flywayMigrate は build JVM 内で実行されるため、JDBC ドライバを buildscript のクラスパスへ載せる。
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
    implementation("org.springframework.boot:spring-boot-flyway")
    implementation("org.jetbrains.kotlin:kotlin-reflect")
    runtimeOnly("org.flywaydb:flyway-database-postgresql")
    runtimeOnly("org.postgresql:postgresql")

    testImplementation("org.springframework.boot:spring-boot-starter-test")
    testImplementation("org.springframework.boot:spring-boot-jooq-test")
    testImplementation("org.springframework.boot:spring-boot-testcontainers")
    testImplementation("org.testcontainers:testcontainers-junit-jupiter")
    testImplementation("org.testcontainers:testcontainers-postgresql")
    testImplementation("org.jetbrains.kotlin:kotlin-test-junit5")
    testRuntimeOnly("org.junit.platform:junit-platform-launcher")

    jooqCodegen("org.postgresql:postgresql")
}

// jOOQ のコード生成元となるローカル PostgreSQL（Docker Compose）への接続情報。
val dbUrl = "jdbc:postgresql://localhost:5432/qcodingtest"
val dbUser = "qcodingtest"
val dbPassword = "qcodingtest"
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
                excludes = "flyway_schema_history"
            }
            generate {
                isDeprecated = false
                isPojos = false
                isDaos = false
            }
            target {
                packageName = "com.example.qcodingtest.jooq"
                directory = jooqOutputDir
            }
        }
    }
}

sourceSets.main {
    kotlin.srcDir(jooqOutputDir)
}

kotlin {
    compilerOptions {
        freeCompilerArgs.addAll("-Xjsr305=strict", "-Xannotation-default-target=param-property")
    }
}

// 生成コードはコミット対象とし通常ビルドを再生成に依存させない（DB 無しでビルド・テスト可能）。
// スキーマ変更時のみ flywayMigrate → jooqCodegen を実行して差分をコミットする（README 2.3）。
tasks.named("jooqCodegen") {
    dependsOn(tasks.named("flywayMigrate"))
}

tasks.withType<Test> {
    useJUnitPlatform()
}

ktlint {
    // jOOQ 生成コードは整形対象外とする。
    filter {
        exclude { it.file.path.contains(jooqOutputDir) }
    }
}
