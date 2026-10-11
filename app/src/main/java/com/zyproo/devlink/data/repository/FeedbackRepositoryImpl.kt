package com.zyproo.devlink.data.repository

import com.google.gson.Gson
import com.zyproo.devlink.common.ApiResult
import com.zyproo.devlink.data.remote.FeedbackApi
import com.zyproo.devlink.data.remote.dto.ApiError
import com.zyproo.devlink.data.remote.dto.CreateFeedbackRequest
import com.zyproo.devlink.data.remote.dto.TeacherFeedbackResponse
import java.io.IOException
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class FeedbackRepositoryImpl @Inject constructor(
    private val api: FeedbackApi,
    private val gson: Gson
) : FeedbackRepository {

    override suspend fun addFeedback(studentId: Long, request: CreateFeedbackRequest): ApiResult<TeacherFeedbackResponse> =
        safeCall { api.addFeedback(studentId, request) }

    override suspend fun getStudentFeedbacks(studentId: Long): ApiResult<List<TeacherFeedbackResponse>> =
        safeCall { api.getStudentFeedbacks(studentId) }

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
