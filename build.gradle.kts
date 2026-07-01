import org.jooq.meta.jaxb.Logging

// flywayMigrate は build JVM 内で実行されるため、JDBC ドライバを buildscript のクラスパスへ載せる。
buildscript {
    repositories {
        mavenCentral()
    }
    dependencies {
        // springboot の依存に合わせてバージョンを指定する。
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
    id("org.openapi.generator") version "7.23.0"
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
    implementation("org.springframework.boot:spring-boot-starter-web")
    implementation("org.springframework.boot:spring-boot-starter-validation")
    implementation("org.springframework.boot:spring-boot-starter-jooq")
    implementation("org.springframework.boot:spring-boot-flyway")
    implementation("org.jetbrains.kotlin:kotlin-reflect")
    runtimeOnly("org.flywaydb:flyway-database-postgresql")
    runtimeOnly("org.postgresql:postgresql")

    testImplementation("org.springframework.boot:spring-boot-starter-test")
    testImplementation("org.springframework.boot:spring-boot-webmvc-test")
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

// OpenAPI からの生成コードはコミットせず build/ 配下へ毎ビルド生成する。
val openApiOutputDir = layout.buildDirectory.dir("generated/openapi")

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
                isKotlinNotNullRecordAttributes = true
            }
            target {
                packageName = "com.example.qcodingtest.jooq"
                directory = jooqOutputDir
            }
        }
    }
}

// API interface と DTO のみを生成し（interfaceOnly）、Controller 実装は生成 interface を実装する。
openApiGenerate {
    generatorName = "kotlin-spring"
    inputSpec.set("$rootDir/src/main/resources/openapi/openapi.yaml")
    outputDir.set(openApiOutputDir.get().asFile.path)
    apiPackage = "com.example.qcodingtest.presentation.api"
    modelPackage = "com.example.qcodingtest.presentation.model"
    configOptions =
        mapOf(
            "interfaceOnly" to "true",
            // バージョン3以降という意味
            "useSpringBoot3" to "true",
            "useTags" to "true",
            "documentationProvider" to "none",
            "enumPropertyNaming" to "original",
            "serializationLibrary" to "jackson",
        )
}

sourceSets.main {
    kotlin.srcDir(jooqOutputDir)
    kotlin.srcDir(openApiOutputDir.map { it.dir("src/main/kotlin") })
}

kotlin {
    compilerOptions {
        freeCompilerArgs.addAll("-Xjsr305=strict", "-Xannotation-default-target=param-property")
    }
}

// DB接続依存なため、 jooq 生成コードはコミット対象とする。
tasks.named("jooqCodegen") {
    dependsOn(tasks.named("flywayMigrate"))
}

// openapi interface/DTO をコンパイル対象に含めるため、Kotlin コンパイル前に生成する。
tasks.named("compileKotlin") {
    dependsOn(tasks.named("openApiGenerate"))
}

tasks.withType<Test> {
    useJUnitPlatform()
    systemProperty("user.timezone", "Asia/Tokyo")
}

tasks.withType<org.springframework.boot.gradle.tasks.run.BootRun> {
    systemProperty("user.timezone", "Asia/Tokyo")
}

ktlint {
    // jOOQ・OpenAPI の生成コードは整形対象外とする。
    filter {
        exclude { it.file.path.contains(jooqOutputDir) }
        exclude { it.file.path.contains("generated/openapi") }
    }
}

// ktlint の lint 対象からは除外しているが、入力ソース（srcDir）には openapi gen の生成ディレクトリが含まれるため、生成タスクへの依存を明示する。
tasks.withType<org.jlleitschuh.gradle.ktlint.tasks.BaseKtLintCheckTask>().configureEach {
    dependsOn(tasks.named("openApiGenerate"))
}
