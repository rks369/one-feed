import org.jetbrains.compose.desktop.application.dsl.TargetFormat

plugins {
    alias(libs.plugins.kotlinJvm)
    alias(libs.plugins.composeMultiplatform)
    alias(libs.plugins.composeCompiler)
}

dependencies {
    implementation(project(":app:shared"))

    implementation(compose.desktop.currentOs)
    implementation(libs.kotlinx.coroutinesSwing)

    implementation(libs.compose.uiToolingPreview)
}

compose.desktop {
    application {
        mainClass = "org.rks369.news.MainKt"

        nativeDistributions {
            targetFormats(TargetFormat.Dmg, TargetFormat.Msi, TargetFormat.Deb)
            packageName = "OneFeed"
            packageVersion = "1.0.0"

            windows {
                iconFile.set(project.file("icons/icon.ico"))
                menu = true
                shortcut = true
                menuGroup = "One Feed"
                upgradeUuid = "3904560e-c34d-418b-a68c-be5749d5e6b6"
            }
            macOS { iconFile.set(project.file("icons/icon.icns")) }
            linux { iconFile.set(project.file("icons/icon.png")) }
        }
    }
}

tasks.withType<JavaExec>().configureEach {
    if (name == "run") systemProperty("news.apiBaseUrl", "http://localhost:8080")
}
