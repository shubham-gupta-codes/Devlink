package com.zyproo.devlink.common

import java.time.Duration
import java.time.Instant
import java.time.LocalDateTime
import java.time.ZoneId

fun formatRelativeTime(createdAt: String?): String {
    if (createdAt.isNullOrBlank()) return ""
    return try {
        val instant = try {
            Instant.parse(createdAt)
        } catch (e: Exception) {
            val ldt = LocalDateTime.parse(createdAt)
            ldt.atZone(ZoneId.systemDefault()).toInstant()
        }
        val now = Instant.now()
        val duration = Duration.between(instant, now)
        val seconds = duration.seconds

        when {
            seconds < 60 -> "just now"
            seconds < 3600 -> "${seconds / 60}m ago"
            seconds < 86400 -> "${seconds / 3600}h ago"
            seconds < 2592000 -> "${seconds / 86400}d ago"
            seconds < 31536000 -> "${seconds / 2592000}mo ago"
            else -> "${seconds / 31536000}y ago"
        }
    } catch (e: Exception) {
        createdAt
    }
}
