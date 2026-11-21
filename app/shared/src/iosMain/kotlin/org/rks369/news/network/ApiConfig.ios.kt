package org.rks369.news.network

import kotlin.experimental.ExperimentalNativeApi

@OptIn(ExperimentalNativeApi::class)
internal actual val apiBaseUrl: String =
    if (Platform.isDebugBinary) "http://localhost:8080" else RELEASE_API_BASE_URL
