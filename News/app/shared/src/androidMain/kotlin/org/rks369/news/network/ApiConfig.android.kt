package org.rks369.news.network

import android.content.pm.ApplicationInfo
import org.rks369.news.platform.AndroidAppContext

internal actual val apiBaseUrl: String
    get() {
        val isDebuggable = AndroidAppContext.context.applicationInfo.flags and ApplicationInfo.FLAG_DEBUGGABLE != 0
        return if (isDebuggable) "http://10.0.2.2:8080" else RELEASE_API_BASE_URL
    }
