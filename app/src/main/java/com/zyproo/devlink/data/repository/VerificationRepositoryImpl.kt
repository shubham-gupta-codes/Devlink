package com.zyproo.devlink.data.repository

import com.google.gson.Gson
import com.zyproo.devlink.common.ApiResult
import com.zyproo.devlink.data.remote.VerificationApi
import com.zyproo.devlink.data.remote.dto.*
import retrofit2.Response
import java.io.IOException
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class VerificationRepositoryImpl @Inject constructor(
    private val api: VerificationApi,
    private val gson: Gson
) : VerificationRepository {

    override suspend fun getTeacherQueue(entityType: EntityType?): ApiResult<List<VerificationRequestResponse>> =
        safeCall { api.getTeacherQueue(entityType) }

    override suspend fun evaluateRequest(requestId: Long, request: EvaluateVerificationRequest): ApiResult<VerificationRequestResponse> =
        safeCall { api.evaluateRequest(requestId, request) }

    override suspend fun getTeacherAnalytics(): ApiResult<TeacherAnalyticsResponse> =
        safeCall { api.getTeacherAnalytics() }

    private suspend fun <T> safeCall(call: suspend () -> Response<T>): ApiResult<T> {
        return try {
            val response = call()
            val body = response.body()
            if (response.isSuccessful && body != null) {
                ApiResult.Success(body)
            } else {
                val errorBody = response.errorBody()?.string()
                val message = try {
                    val apiError = gson.fromJson(errorBody, ApiError::class.java)
                    apiError?.message
                } catch (e: Exception) {
                    null
                }
                ApiResult.Error(message ?: "Something went wrong (HTTP ${response.code()}).")
            }
        } catch (e: IOException) {
            ApiResult.Error("Can't reach the server. Check your connection.")
        } catch (e: Exception) {
            ApiResult.Error(e.localizedMessage ?: "An error occurred.")
        }
    }
}
