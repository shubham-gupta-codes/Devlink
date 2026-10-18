package com.zyproo.devlink.data.remote

import com.zyproo.devlink.data.remote.dto.*
import retrofit2.http.*

interface JobApi {

    @POST("api/jobs")
    suspend fun createJob(@Body request: CreateJobRequest): JobResponse

    @PUT("api/jobs/{id}")
    suspend fun updateJob(
        @Path("id") jobId: Long,
        @Body request: UpdateJobRequest
    ): JobResponse

    @DELETE("api/jobs/{id}")
    suspend fun deleteJob(@Path("id") jobId: Long): Map<String, String>

    @GET("api/jobs/recruiter")
    suspend fun getRecruiterJobs(): List<JobResponse>

    @GET("api/jobs/open")
    suspend fun getAllOpenJobs(): List<JobResponse>

    @GET("api/jobs/{id}")
    suspend fun getJobDetails(@Path("id") jobId: Long): JobResponse

    @POST("api/jobs/{id}/apply")
    suspend fun applyToJob(@Path("id") jobId: Long): JobApplicationResponse

    @GET("api/jobs/{id}/applications")
    suspend fun getJobApplications(@Path("id") jobId: Long): List<JobApplicationResponse>

    @GET("api/jobs/applications/my")
    suspend fun getStudentApplications(): List<JobApplicationResponse>

    @PUT("api/jobs/applications/{appId}")
    suspend fun evaluateApplication(
        @Path("appId") applicationId: Long,
        @Body request: EvaluateApplicationRequest
    ): JobApplicationResponse

    @POST("api/jobs/shortlist")
    suspend fun shortlistCandidate(
        @Query("studentId") studentId: Long,
        @Query("jobId") jobId: Long?
    ): SavedCandidateResponse

    @GET("api/jobs/shortlisted")
    suspend fun getSavedCandidates(): List<SavedCandidateResponse>

    @DELETE("api/jobs/shortlist")
    suspend fun removeShortlistedCandidate(
        @Query("studentId") studentId: Long,
        @Query("jobId") jobId: Long?
    ): retrofit2.Response<Unit>

    @DELETE("api/jobs/shortlist/{id}")
    suspend fun removeShortlistedCandidateById(
        @Path("id") id: Long
    ): retrofit2.Response<Unit>

    @DELETE("api/jobs/shortlist/student/{studentId}")
    suspend fun removeCandidateFromAllShortlists(
        @Path("studentId") studentId: Long
    ): retrofit2.Response<Unit>

    @GET("api/jobs/{id}/recommended")
    suspend fun getRecommendedCandidates(@Path("id") jobId: Long): List<ProfileResponse>
}
