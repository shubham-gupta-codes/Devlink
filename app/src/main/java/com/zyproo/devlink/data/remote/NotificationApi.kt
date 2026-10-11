package com.zyproo.devlink.data.remote

import com.zyproo.devlink.data.remote.dto.NotificationResponse
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path

interface NotificationApi {

    @GET("api/notifications")
    suspend fun getMyNotifications(): List<NotificationResponse>

    @POST("api/notifications/{id}/read")
    suspend fun markAsRead(
        @Path("id") notificationId: Long
    )
}
