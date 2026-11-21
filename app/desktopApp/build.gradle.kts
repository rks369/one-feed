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

            windows { iconFile.set(project.file("icons/icon.ico")) }
            macOS { iconFile.set(project.file("icons/icon.icns")) }
            linux { iconFile.set(project.file("icons/icon.png")) }
        }
    }
}