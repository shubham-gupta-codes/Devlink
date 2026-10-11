package com.zyproo.devlink.data.repository

import com.zyproo.devlink.data.remote.dto.NotificationResponse

interface NotificationRepository {
    suspend fun getMyNotifications(): Result<List<NotificationResponse>>
    suspend fun markAsRead(notificationId: Long): Result<Unit>
}
