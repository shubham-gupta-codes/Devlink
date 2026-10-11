package com.zyproo.devlink.data.repository

import com.zyproo.devlink.common.ApiResult
import com.zyproo.devlink.data.remote.dto.*

interface VerificationRepository {
    suspend fun getTeacherQueue(entityType: EntityType?): ApiResult<List<VerificationRequestResponse>>
    suspend fun evaluateRequest(requestId: Long, request: EvaluateVerificationRequest): ApiResult<VerificationRequestResponse>
    suspend fun getTeacherAnalytics(): ApiResult<TeacherAnalyticsResponse>
}
