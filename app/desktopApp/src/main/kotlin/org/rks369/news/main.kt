package org.rks369.news

import androidx.compose.runtime.remember
import androidx.compose.ui.graphics.painter.BitmapPainter
import androidx.compose.ui.graphics.toComposeImageBitmap
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import javax.imageio.ImageIO

private fun loadIcon() = BitmapPainter(
    Thread.currentThread().contextClassLoader.getResourceAsStream("icon.png")!!
        .use { ImageIO.read(it) }
        .toComposeImageBitmap(),
)

fun main() = application {
    Window(
        onCloseRequest = ::exitApplication,
        title = "One Feed",
        icon = remember { loadIcon() },
    ) {
        App()
    }
}