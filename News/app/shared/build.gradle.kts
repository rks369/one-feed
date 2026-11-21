import org.jetbrains.kotlin.gradle.ExperimentalWasmDsl
import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    alias(libs.plugins.kotlinMultiplatform)
    alias(libs.plugins.androidMultiplatformLibrary)
    alias(libs.plugins.composeMultiplatform)
    alias(libs.plugins.composeCompiler)
    alias(libs.plugins.serialization)
    alias(libs.plugins.sqldelight)
}

kotlin {
    listOf(
        iosArm64(),
        iosSimulatorArm64()
    ).forEach { iosTarget ->
        iosTarget.binaries.framework {
            baseName = "Shared"
            isStatic = true
        }
    }
    
    jvm()
    
    js {
        browser()
    }
    
    @OptIn(ExperimentalWasmDsl::class)
    wasmJs {
        browser()
    }
    
    android {
       namespace = "org.rks369.news.app.shared"
       compileSdk = libs.versions.android.compileSdk.get().toInt()
       minSdk = libs.versions.android.minSdk.get().toInt()
    
       compilerOptions {
           jvmTarget = JvmTarget.JVM_11
       }
       androidResources {
           enable = true
       }
    }
    
    sourceSets {
        commonMain.dependencies {
            api(project(":core"))
            implementation(libs.compose.runtime)
            implementation(libs.compose.foundation)
            implementation(libs.compose.material3)
            implementation(libs.compose.ui)
            implementation(libs.compose.components.resources)
            implementation(libs.compose.uiToolingPreview)
            implementation(libs.androidx.lifecycle.viewmodelCompose)
            implementation(libs.androidx.lifecycle.runtimeCompose)
            implementation(libs.ktor.client.core)
            implementation(libs.ktor.client.content.negotiation)
            implementation(libs.ktor.serialization.kotlinx.json)
            implementation(libs.kotlinx.serialization.json)
            implementation(libs.coil.compose)
            implementation(libs.coil.network.ktor3)
            implementation(libs.compose.materialIconsCore)
            implementation(libs.sqldelight.runtime)
            implementation(libs.sqldelight.coroutines.extensions)
            implementation(libs.kotlinx.datetime)
        }

        val settingsSharedDir = "src/settingsShared/kotlin"

        val bookmarksSqlDir = "src/bookmarksSql/kotlin"
        val bookmarksInMemoryDir = "src/bookmarksInMemory/kotlin"

        androidMain.configure {
            kotlin.srcDir(settingsSharedDir)
            kotlin.srcDir(bookmarksSqlDir)
        }
        androidMain.dependencies {
            implementation(libs.compose.uiToolingPreview)
            implementation(libs.compose.uiTooling)
            implementation(libs.ktor.client.okhttp)
            implementation(libs.multiplatform.settings.no.arg)
            implementation(libs.sqldelight.android.driver)
        }

        iosMain.configure {
            kotlin.srcDir(settingsSharedDir)
            kotlin.srcDir(bookmarksSqlDir)
        }
        iosMain.dependencies {
            implementation(libs.ktor.client.darwin)
            implementation(libs.multiplatform.settings.no.arg)
            implementation(libs.sqldelight.native.driver)
        }

        jvmMain.configure {
            kotlin.srcDir(settingsSharedDir)
            kotlin.srcDir(bookmarksSqlDir)
        }
        jvmMain.dependencies {
            implementation(libs.ktor.client.okhttp)
            implementation(libs.multiplatform.settings.no.arg)
            implementation(libs.sqldelight.sqlite.driver)
        }

        jsMain.configure {
            kotlin.srcDir(settingsSharedDir)
            kotlin.srcDir(bookmarksInMemoryDir)
        }
        jsMain.dependencies {
            implementation(libs.wrappers.browser)
            implementation(libs.multiplatform.settings.no.arg)
        }

        wasmJsMain.configure {
            kotlin.srcDir(bookmarksInMemoryDir)
        }
    }
}

sqldelight {
    databases {
        create("BookmarksDatabase") {
            packageName.set("org.rks369.news.bookmarks.db")
        }
    }
}

dependencies {
    androidRuntimeClasspath(libs.compose.uiTooling)
}