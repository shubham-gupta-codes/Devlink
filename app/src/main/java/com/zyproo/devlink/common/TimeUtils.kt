package com.zyproo.devlink.common

import java.time.Duration
import java.time.Instant
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter
import java.util.Locale

/**
 * Parses any ISO 8601 or standard datetime string into a ZonedDateTime in the user's local zone.
 */
private fun parseToLocalZonedDateTime(dateStr: String?): ZonedDateTime? {
    if (dateStr.isNullOrBlank()) return null
    return try {
        Instant.parse(dateStr).atZone(ZoneId.systemDefault())
    } catch (_: Exception) {
        try {
            LocalDateTime.parse(dateStr).atZone(ZoneId.systemDefault())
        } catch (_: Exception) {
            try {
                LocalDate.parse(dateStr).atStartOfDay(ZoneId.systemDefault())
            } catch (_: Exception) {
                null
            }
        }
    }
}

/**
 * Formats a relative time string (e.g., "just now", "15m ago", "2h ago", "3d ago").
 */
fun formatRelativeTime(createdAt: String?): String {
    if (createdAt.isNullOrBlank()) return ""
    return try {
        val zdt = parseToLocalZonedDateTime(createdAt) ?: return createdAt
        val instant = zdt.toInstant()
        val now = Instant.now()
        val duration = Duration.between(instant, now)
        val seconds = duration.seconds

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
        val zdt = parseToLocalZonedDateTime(dateStr) ?: return dateStr
        val formatter = DateTimeFormatter.ofPattern("EEE, d MMM yyyy 'at' hh:mm a", Locale.ENGLISH)
        zdt.format(formatter)
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
        val zdt = parseToLocalZonedDateTime(dateStr) ?: return dateStr.take(10)
        val formatter = DateTimeFormatter.ofPattern("d MMM yyyy", Locale.ENGLISH)
        zdt.format(formatter)
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
        val zdt = parseToLocalZonedDateTime(dateStr) ?: return dateStr.take(16).replace("T", " ")
        val formatter = DateTimeFormatter.ofPattern("d MMM yyyy, hh:mm a", Locale.ENGLISH)
        zdt.format(formatter)
    } catch (_: Exception) {
        dateStr.take(16).replace("T", " ")
    }
}
