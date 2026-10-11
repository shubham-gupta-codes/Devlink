package com.zyproo.devlink.data.remote

import com.zyproo.devlink.data.remote.dto.CreateFeedbackRequest
import com.zyproo.devlink.data.remote.dto.TeacherFeedbackResponse
import retrofit2.http.*

interface FeedbackApi {

    @POST("api/feedback/student/{studentId}")
    suspend fun addFeedback(
        @Path("studentId") studentId: Long,
        @Body request: CreateFeedbackRequest
    ): TeacherFeedbackResponse

    @GET("api/feedback/student/{studentId}")
    suspend fun getStudentFeedbacks(
        @Path("studentId") studentId: Long
    ): List<TeacherFeedbackResponse>
}
