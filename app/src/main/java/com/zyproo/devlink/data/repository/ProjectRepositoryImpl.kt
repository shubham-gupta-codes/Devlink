package com.zyproo.devlink.data.repository

import com.google.gson.Gson
import com.zyproo.devlink.common.ApiResult
import com.zyproo.devlink.data.remote.ProjectApi
import com.zyproo.devlink.data.remote.dto.*
import retrofit2.Response
import java.io.IOException
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ProjectRepositoryImpl @Inject constructor(
    private val api: ProjectApi,
    private val gson: Gson
) : ProjectRepository {

    override suspend fun uploadProject(request: UploadProjectRequest): ApiResult<ProjectResponse> =
        safeCall { api.uploadProject(request) }

    override suspend fun getProjectFeed(filter: String): ApiResult<List<ProjectResponse>> =
        safeCall { api.getProjectFeed(filter) }

    override suspend fun toggleLike(projectId: Long): ApiResult<ProjectResponse> =
        safeCall { api.toggleLike(projectId) }

    override suspend fun addComment(projectId: Long, request: ProjectCommentRequest): ApiResult<ProjectCommentResponse> =
        safeCall { api.addComment(projectId, request) }

    override suspend fun getComments(projectId: Long): ApiResult<List<ProjectCommentResponse>> =
        safeCall { api.getComments(projectId) }

    override suspend fun getStudentProjects(studentId: Long): ApiResult<List<ProjectResponse>> =
        safeCall { api.getStudentProjects(studentId) }

    override suspend fun deleteProject(projectId: Long): ApiResult<Map<String, String>> =
        safeCall { api.deleteProject(projectId) }

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
