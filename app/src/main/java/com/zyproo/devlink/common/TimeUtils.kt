package com.zyproo.devlink.common

import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.TimeZone

/**
 * Parses any ISO 8601 or standard datetime string into a Date object.
 * Fully compatible with all Android API levels (API 24+ without requiring API 26 java.time).
 */
private fun parseToDate(dateStr: String?): Date? {
    if (dateStr.isNullOrBlank()) return null
    val clean = dateStr.trim()

    val patterns = arrayOf(
        "yyyy-MM-dd'T'HH:mm:ss.SSSXXX",
        "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'",
        "yyyy-MM-dd'T'HH:mm:ssXXX",
        "yyyy-MM-dd'T'HH:mm:ss'Z'",
        "yyyy-MM-dd'T'HH:mm:ss.SSS",
        "yyyy-MM-dd'T'HH:mm:ss",
        "yyyy-MM-dd HH:mm:ss",
        "yyyy-MM-dd"
    )

    for (pattern in patterns) {
        try {
            val sdf = SimpleDateFormat(pattern, Locale.US)
            if (pattern.endsWith("'Z'") || (!pattern.contains("X") && pattern.contains("T"))) {
                sdf.timeZone = TimeZone.getTimeZone("UTC")
            }
            val date = sdf.parse(clean)
            if (date != null) return date
        } catch (_: Exception) {
            // Try next format
        }
    }
    return null
}

/**
 * Formats a relative time string (e.g., "just now", "15m ago", "2h ago", "3d ago").
 */
fun formatRelativeTime(createdAt: String?): String {
    if (createdAt.isNullOrBlank()) return ""
    return try {
        val date = parseToDate(createdAt) ?: return createdAt
        val now = System.currentTimeMillis()
        val diffMs = now - date.time
        val seconds = diffMs / 1000

        when {
            seconds < 0 -> "Upcoming"
            seconds < 60 -> "just now"
            seconds < 3600 -> "${seconds / 60}m ago"
            seconds < 86400 -> "${seconds / 3600}h ago"
            seconds < 2592000 -> "${seconds / 86400}d ago"
            seconds < 31536000 -> "${seconds / 2592000}mo ago"
            else -> "${seconds / 31536000}y ago"
        }
    } catch (_: Exception) {
        createdAt
    }
}

/**
 * Formats a meeting or schedule datetime into human-friendly format:
 * e.g., "Mon, 24 Aug 2026 at 10:30 AM"
 */
fun formatMeetingDateTime(dateStr: String?): String {
    if (dateStr.isNullOrBlank()) return "Not scheduled"
    return try {
        val date = parseToDate(dateStr) ?: return dateStr
        val formatter = SimpleDateFormat("EEE, d MMM yyyy 'at' hh:mm a", Locale.ENGLISH).apply {
            timeZone = TimeZone.getDefault()
        }
        formatter.format(date)
    } catch (_: Exception) {
        dateStr
    }
}

/**
 * Formats a date into a clean date string:
 * e.g., "24 Aug 2026"
 */
fun formatDisplayDate(dateStr: String?): String {
    if (dateStr.isNullOrBlank()) return ""
    return try {
        val date = parseToDate(dateStr) ?: return dateStr.take(10)
        val formatter = SimpleDateFormat("d MMM yyyy", Locale.ENGLISH).apply {
            timeZone = TimeZone.getDefault()
        }
        formatter.format(date)
    } catch (_: Exception) {
        dateStr.take(10)
    }
}

/**
 * Formats a timestamp into a clean compact date and time:
 * e.g., "24 Aug 2026, 02:30 PM"
 */
fun formatDisplayDateTime(dateStr: String?): String {
    if (dateStr.isNullOrBlank()) return ""
    return try {
        val date = parseToDate(dateStr) ?: return dateStr.take(16).replace("T", " ")
        val formatter = SimpleDateFormat("d MMM yyyy, hh:mm a", Locale.ENGLISH).apply {
            timeZone = TimeZone.getDefault()
        }
        formatter.format(date)
    } catch (_: Exception) {
        dateStr.take(16).replace("T", " ")
    }
}
