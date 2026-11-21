package org.rks369.news.config

import java.io.File
import java.util.Properties

data class ServerConfig(
    val port: Int,
    val nytApiKey: String,
    val databaseUrl: String,
    val publicBaseUrl: String,
    val webAppDir: File?,
) {
    companion object {
        fun fromEnvironment(): ServerConfig {
            val localProperties = Properties()
            val file = File("local.properties")
            if (file.exists()) file.inputStream().use { localProperties.load(it) }

            fun setting(name: String): String? = System.getenv(name) ?: localProperties.getProperty(name)

            val port = setting("PORT")?.toInt() ?: 8080
            return ServerConfig(
                port = port,
                nytApiKey = setting("NYT_API_KEY") ?: throw MissingSettingException("NYT_API_KEY"),
                databaseUrl = setting("DATABASE_URL") ?: throw MissingSettingException("DATABASE_URL"),
                publicBaseUrl = setting("PUBLIC_BASE_URL") ?: setting("RENDER_EXTERNAL_URL") ?: "http://localhost:$port",
                webAppDir = listOf(setting("WEB_APP_DIR"), "../app/webApp/build/dist/wasmJs/productionExecutable")
                    .firstNotNullOfOrNull { path -> path?.let(::File)?.takeIf { File(it, "index.html").exists() } },
            )
        }
    }
}

class MissingSettingException(name: String) :
    Exception("$name is not set. Add it to server/local.properties or set it as an environment variable.")
