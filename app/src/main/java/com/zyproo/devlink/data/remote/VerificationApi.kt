package com.zyproo.devlink.data.remote

import com.zyproo.devlink.data.remote.dto.*
import retrofit2.Response
import retrofit2.http.*

interface VerificationApi {
    @GET("api/verifications/queue")
    suspend fun getTeacherQueue(@Query("entityType") entityType: EntityType?): Response<List<VerificationRequestResponse>>

    @POST("api/verifications/{requestId}/evaluate")
    suspend fun evaluateRequest(
        @Path("requestId") requestId: Long,
        @Body request: EvaluateVerificationRequest
    ): Response<VerificationRequestResponse>

    @GET("api/verifications/analytics")
    suspend fun getTeacherAnalytics(): Response<TeacherAnalyticsResponse>
}
