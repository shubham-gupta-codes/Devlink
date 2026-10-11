package com.zyproo.devlink.data.repository

import com.zyproo.devlink.common.ApiResult
import com.zyproo.devlink.data.remote.dto.CreateFeedbackRequest
import com.zyproo.devlink.data.remote.dto.TeacherFeedbackResponse

interface FeedbackRepository {
    suspend fun addFeedback(studentId: Long, request: CreateFeedbackRequest): ApiResult<TeacherFeedbackResponse>
    suspend fun getStudentFeedbacks(studentId: Long): ApiResult<List<TeacherFeedbackResponse>>
}
