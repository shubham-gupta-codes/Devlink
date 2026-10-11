package com.zyproo.devlink.data.remote

import com.zyproo.devlink.data.remote.dto.*
import retrofit2.http.*

interface SkillApi {

    @GET("api/skills")
    suspend fun getAllSkills(): List<SkillResponse>

    @POST("api/skills/request")
    suspend fun requestSkillVerification(
        @Body request: SkillRequestDto
    ): SkillRequestResponse

    @GET("api/skills/request/student")
    suspend fun getStudentRequests(): List<SkillRequestResponse>

    @GET("api/skills/student")
    suspend fun getStudentApprovedSkills(): List<StudentSkillResponse>

    @GET("api/skills/student/{studentId}")
    suspend fun getStudentApprovedSkillsById(
        @Path("studentId") studentId: Long
    ): List<StudentSkillResponse>

    @GET("api/skills/request/teacher")
    suspend fun getTeacherPendingRequests(): List<SkillRequestResponse>

    @POST("api/skills/request/{requestId}/meet")
    suspend fun scheduleMeeting(
        @Path("requestId") requestId: Long,
        @Body request: ScheduleMeetingRequest
    ): SkillRequestResponse

    @POST("api/skills/request/{requestId}/approve")
    suspend fun approveRequest(
        @Path("requestId") requestId: Long,
        @Body request: ApproveSkillRequest
    ): SkillRequestResponse

    @POST("api/skills/request/{requestId}/reject")
    suspend fun rejectRequest(
        @Path("requestId") requestId: Long,
        @Body request: RejectSkillRequest
    ): SkillRequestResponse
}
