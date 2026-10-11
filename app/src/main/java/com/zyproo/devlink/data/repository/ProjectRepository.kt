package com.zyproo.devlink.data.repository

import com.zyproo.devlink.common.ApiResult
import com.zyproo.devlink.data.remote.dto.*

interface ProjectRepository {
    suspend fun uploadProject(request: UploadProjectRequest): ApiResult<ProjectResponse>
    suspend fun getProjectFeed(filter: String): ApiResult<List<ProjectResponse>>
    suspend fun toggleLike(projectId: Long): ApiResult<ProjectResponse>
    suspend fun addComment(projectId: Long, request: ProjectCommentRequest): ApiResult<ProjectCommentResponse>
    suspend fun getComments(projectId: Long): ApiResult<List<ProjectCommentResponse>>
    suspend fun getStudentProjects(studentId: Long): ApiResult<List<ProjectResponse>>
    suspend fun deleteProject(projectId: Long): ApiResult<Map<String, String>>
}
