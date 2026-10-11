package com.zyproo.devlink.common

import com.zyproo.devlink.BuildConfig

object ActiveHostHolder {
    @Volatile
    var activeHost: String? = null
}

fun String?.toFullMediaUrl(customHost: String? = null): String? {
    if (this.isNullOrBlank()) return null

    val hostToUse = customHost 
        ?: ActiveHostHolder.activeHost 
        ?: BuildConfig.API_BASE_URL

    val base = if (!hostToUse.startsWith("http://") && !hostToUse.startsWith("https://")) {
        "http://$hostToUse"
    } else {
        hostToUse
    }

    // 1. If it's a relative path to files
    if (this.startsWith("/api/files/") || this.startsWith("api/files/")) {
        val path = if (this.startsWith("/")) this else "/$this"
        return base.trimEnd('/') + path
    }

    // 2. If it's an absolute URL pointing to backend API files (e.g. from an old IP or localhost or 10.0.2.2)
    val apiFilesIndex = this.indexOf("/api/files/")
    if (apiFilesIndex != -1 && (this.startsWith("http://") || this.startsWith("https://"))) {
        val path = this.substring(apiFilesIndex)
        return base.trimEnd('/') + path
    }

    // 3. For any external URLs (e.g., github, google, unsplash, etc.)
    if (this.startsWith("http://") || this.startsWith("https://")) {
        return this
    }

    // 4. Default relative URL
    return base.trimEnd('/') + if (this.startsWith("/")) this else "/$this"
}
