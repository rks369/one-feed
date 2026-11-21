package org.rks369.news.network

// The desktopApp run task sets news.apiBaseUrl to the local server; packaged apps use the release server.
internal actual val apiBaseUrl: String =
    System.getProperty("news.apiBaseUrl") ?: RELEASE_API_BASE_URL
