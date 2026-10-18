package com.zyproo.devlink.data.repository

import com.zyproo.devlink.common.ApiResult
import com.zyproo.devlink.data.remote.dto.*

interface JobRepository {
    suspend fun createJob(request: CreateJobRequest): ApiResult<JobResponse>
    suspend fun updateJob(jobId: Long, request: UpdateJobRequest): ApiResult<JobResponse>
    suspend fun deleteJob(jobId: Long): ApiResult<Unit>
    suspend fun getRecruiterJobs(): ApiResult<List<JobResponse>>
    suspend fun getAllOpenJobs(): ApiResult<List<JobResponse>>
    suspend fun getJobDetails(jobId: Long): ApiResult<JobResponse>
    suspend fun applyToJob(jobId: Long): ApiResult<JobApplicationResponse>
    suspend fun getJobApplications(jobId: Long): ApiResult<List<JobApplicationResponse>>
    suspend fun getStudentApplications(): ApiResult<List<JobApplicationResponse>>
    suspend fun evaluateApplication(applicationId: Long, status: JobApplicationStatus): ApiResult<JobApplicationResponse>
    suspend fun shortlistCandidate(studentId: Long, jobId: Long?): ApiResult<SavedCandidateResponse>
    suspend fun getSavedCandidates(): ApiResult<List<SavedCandidateResponse>>
    suspend fun removeShortlistedCandidate(studentId: Long, jobId: Long?): ApiResult<Unit>
    suspend fun removeShortlistedCandidateById(id: Long): ApiResult<Unit>
    suspend fun removeCandidateFromAllShortlists(studentId: Long): ApiResult<Unit>
    suspend fun getRecommendedCandidates(jobId: Long): ApiResult<List<ProfileResponse>>
}
