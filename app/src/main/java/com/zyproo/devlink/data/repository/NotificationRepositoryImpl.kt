package com.zyproo.devlink.data.repository

import com.zyproo.devlink.data.remote.NotificationApi
import com.zyproo.devlink.data.remote.dto.NotificationResponse
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class NotificationRepositoryImpl @Inject constructor(
    private val notificationApi: NotificationApi
) : NotificationRepository {

    override suspend fun getMyNotifications(): Result<List<NotificationResponse>> = runCatching {
        notificationApi.getMyNotifications()
    }

    override suspend fun markAsRead(notificationId: Long): Result<Unit> = runCatching {
        notificationApi.markAsRead(notificationId)
    }
}
