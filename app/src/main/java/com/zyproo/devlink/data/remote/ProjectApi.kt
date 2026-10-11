package com.zyproo.devlink.data.remote

import com.zyproo.devlink.data.remote.dto.*
import retrofit2.Response
import retrofit2.http.*

interface ProjectApi {
    @POST("api/projects")
    suspend fun uploadProject(@Body request: UploadProjectRequest): Response<ProjectResponse>

    @GET("api/projects/feed")
    suspend fun getProjectFeed(@Query("filter") filter: String): Response<List<ProjectResponse>>

    @POST("api/projects/{projectId}/like")
    suspend fun toggleLike(@Path("projectId") projectId: Long): Response<ProjectResponse>

    @POST("api/projects/{projectId}/comments")
    suspend fun addComment(
        @Path("projectId") projectId: Long,
        @Body request: ProjectCommentRequest
    ): Response<ProjectCommentResponse>

    @GET("api/projects/{projectId}/comments")
    suspend fun getComments(@Path("projectId") projectId: Long): Response<List<ProjectCommentResponse>>

    @GET("api/projects/student/{studentId}")
    suspend fun getStudentProjects(@Path("studentId") studentId: Long): Response<List<ProjectResponse>>

    @DELETE("api/projects/{projectId}")
    suspend fun deleteProject(@Path("projectId") projectId: Long): Response<Map<String, String>>
}
