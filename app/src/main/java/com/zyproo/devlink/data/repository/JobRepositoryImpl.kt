package com.zyproo.devlink.data.repository

import com.google.gson.Gson
import com.zyproo.devlink.common.ApiResult
import com.zyproo.devlink.data.remote.JobApi
import com.zyproo.devlink.data.remote.dto.*
import java.io.IOException
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class JobRepositoryImpl @Inject constructor(
    private val api: JobApi,
    private val gson: Gson
) : JobRepository {

    override suspend fun createJob(request: CreateJobRequest): ApiResult<JobResponse> =
        safeCall { api.createJob(request) }

    override suspend fun updateJob(jobId: Long, request: UpdateJobRequest): ApiResult<JobResponse> =
        safeCall { api.updateJob(jobId, request) }

    override suspend fun deleteJob(jobId: Long): ApiResult<Unit> =
        safeCall {
            api.deleteJob(jobId)
            Unit
        }

    override suspend fun getRecruiterJobs(): ApiResult<List<JobResponse>> =
        safeCall { api.getRecruiterJobs() }

    override suspend fun getAllOpenJobs(): ApiResult<List<JobResponse>> =
        safeCall { api.getAllOpenJobs() }

    override suspend fun getJobDetails(jobId: Long): ApiResult<JobResponse> =
        safeCall { api.getJobDetails(jobId) }

    override suspend fun applyToJob(jobId: Long): ApiResult<JobApplicationResponse> =
        safeCall { api.applyToJob(jobId) }

    override suspend fun getJobApplications(jobId: Long): ApiResult<List<JobApplicationResponse>> =
        safeCall { api.getJobApplications(jobId) }

    override suspend fun getStudentApplications(): ApiResult<List<JobApplicationResponse>> =
        safeCall { api.getStudentApplications() }

    override suspend fun evaluateApplication(applicationId: Long, status: JobApplicationStatus): ApiResult<JobApplicationResponse> =
        safeCall { api.evaluateApplication(applicationId, EvaluateApplicationRequest(status)) }

    override suspend fun shortlistCandidate(studentId: Long, jobId: Long?): ApiResult<SavedCandidateResponse> =
        safeCall { api.shortlistCandidate(studentId, jobId) }

    override suspend fun getSavedCandidates(): ApiResult<List<SavedCandidateResponse>> =
        safeCall { api.getSavedCandidates() }

    override suspend fun removeShortlistedCandidate(studentId: Long, jobId: Long?): ApiResult<Unit> =
        safeCall {
            api.removeShortlistedCandidate(studentId, jobId)
            Unit
        }

    override suspend fun removeShortlistedCandidateById(id: Long): ApiResult<Unit> =
        safeCall {
            api.removeShortlistedCandidateById(id)
            Unit
        }

    override suspend fun removeCandidateFromAllShortlists(studentId: Long): ApiResult<Unit> =
        safeCall {
            api.removeCandidateFromAllShortlists(studentId)
            Unit
        }

    override suspend fun getRecommendedCandidates(jobId: Long): ApiResult<List<ProfileResponse>> =
        safeCall { api.getRecommendedCandidates(jobId) }

    private suspend fun <T> safeCall(call: suspend () -> T): ApiResult<T> {
        return try {
            ApiResult.Success(call())
        } catch (e: retrofit2.HttpException) {
            val errorBody = e.response()?.errorBody()?.string()
            val message = try {
                val apiError = gson.fromJson(errorBody, ApiError::class.java)
                apiError?.message
            } catch (ex: Exception) {
                null
            }
            ApiResult.Error(message ?: "Something went wrong (HTTP ${e.code()}).")
        } catch (e: IOException) {
            ApiResult.Error("Can't reach the server. Check your connection.")
        } catch (e: Exception) {
            ApiResult.Error(e.localizedMessage ?: "An error occurred.")
        }
    }
}
