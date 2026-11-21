plugins {
    alias(libs.plugins.kotlinJvm)
    alias(libs.plugins.ktor)
    alias(libs.plugins.serialization)
    alias(libs.plugins.sqldelight)
}

group = "org.rks369.news"
version = "1.0.0"
application {
    mainClass = "org.rks369.news.ApplicationKt"
}

kotlin {
    compilerOptions {
        optIn.add("kotlin.time.ExperimentalTime")
    }
}

dependencies {
    api(project(":core"))
    implementation(libs.logback)
    implementation(libs.ktor.serverCore)
    implementation(libs.ktor.serverNetty)
    implementation(libs.ktor.serverContentNegotiation)
    implementation(libs.ktor.serverStatusPages)
    implementation(libs.ktor.serverCompression)
    implementation(libs.ktor.serverCachingHeaders)
    implementation(libs.ktor.serverHtmlBuilder)
    implementation(libs.ktor.client.core)
    implementation(libs.ktor.client.cio)
    implementation(libs.ktor.client.content.negotiation)
    implementation(libs.ktor.serialization.kotlinx.json)
    implementation(libs.kotlinx.serialization.json)
    implementation(libs.kotlinx.datetime)
    implementation(libs.sqldelight.jdbc.driver)
    implementation(libs.postgresql)
}

sqldelight {
    databases {
        create("NewsDatabase") {
            packageName.set("org.rks369.news.db")
            dialect(libs.sqldelight.postgresql.dialect)
        }
    }
}
