package com.zyproo.devlink.ui.profile

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.zyproo.devlink.common.ApiResult
import com.zyproo.devlink.data.remote.dto.*
import com.zyproo.devlink.data.repository.ProjectRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class ProjectUiState(
    val feed: List<ProjectResponse> = emptyList(),
    val commentsMap: Map<Long, List<ProjectCommentResponse>> = emptyMap(), // projectId -> comments
    val savedProjectIds: Set<Long> = emptySet(),
    val studentProjects: List<ProjectResponse> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null,
    val isUploading: Boolean = false,
    val uploadSuccess: Boolean = false,
    val uploadError: String? = null,
    val isCommenting: Boolean = false,
    val commentError: String? = null
)

@HiltViewModel
class ProjectViewModel @Inject constructor(
    private val repository: ProjectRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(ProjectUiState())
    val uiState: StateFlow<ProjectUiState> = _uiState.asStateFlow()

    fun toggleSave(projectId: Long) {
        _uiState.update { state ->
            val set = state.savedProjectIds.toMutableSet()
            if (set.contains(projectId)) {
                set.remove(projectId)
            } else {
                set.add(projectId)
            }
            state.copy(savedProjectIds = set)
        }
    }

    fun loadFeed(filter: String = "all") {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            when (val result = repository.getProjectFeed(filter)) {
                is ApiResult.Success -> {
                    _uiState.update { it.copy(isLoading = false, feed = result.data) }
                }
                is ApiResult.Error -> {
                    _uiState.update { it.copy(isLoading = false, error = result.message) }
                }
            }
        }
    }

    fun toggleLike(projectId: Long) {
        viewModelScope.launch {
            // Optimistic UI update
            _uiState.update { state ->
                val updatedFeed = state.feed.map {
                    if (it.id == projectId) {
                        val newLiked = !it.likedByCurrentUser
                        val newCount = if (newLiked) it.likeCount + 1 else maxOf(0L, it.likeCount - 1)
                        it.copy(likedByCurrentUser = newLiked, likeCount = newCount)
                    } else it
                }
                val updatedStudentProjects = state.studentProjects.map {
                    if (it.id == projectId) {
                        val newLiked = !it.likedByCurrentUser
                        val newCount = if (newLiked) it.likeCount + 1 else maxOf(0L, it.likeCount - 1)
                        it.copy(likedByCurrentUser = newLiked, likeCount = newCount)
                    } else it
                }
                state.copy(feed = updatedFeed, studentProjects = updatedStudentProjects)
            }

            when (val result = repository.toggleLike(projectId)) {
                is ApiResult.Success -> {
                    _uiState.update { state ->
                        val updatedFeed = state.feed.map {
                            if (it.id == projectId) result.data else it
                        }
                        val updatedStudentProjects = state.studentProjects.map {
                            if (it.id == projectId) result.data else it
                        }
                        state.copy(feed = updatedFeed, studentProjects = updatedStudentProjects)
                    }
                }
                is ApiResult.Error -> {
                    _uiState.update { it.copy(error = result.message) }
                }
            }
        }
    }

    fun loadComments(projectId: Long) {
        viewModelScope.launch {
            when (val result = repository.getComments(projectId)) {
                is ApiResult.Success -> {
                    _uiState.update { state ->
                        val updatedComments = state.commentsMap.toMutableMap()
                        updatedComments[projectId] = result.data
                        state.copy(commentsMap = updatedComments)
                    }
                }
                is ApiResult.Error -> {
                    _uiState.update { it.copy(commentError = result.message) }
                }
            }
        }
    }

    fun addComment(projectId: Long, content: String, parentCommentId: Long? = null) {
        viewModelScope.launch {
            _uiState.update { it.copy(isCommenting = true, commentError = null) }
            val request = ProjectCommentRequest(content, parentCommentId)
            when (val result = repository.addComment(projectId, request)) {
                is ApiResult.Success -> {
                    _uiState.update { it.copy(isCommenting = false) }
                    loadComments(projectId)
                    _uiState.update { state ->
                        val updatedFeed = state.feed.map {
                            if (it.id == projectId) it.copy(commentCount = it.commentCount + 1) else it
                        }
                        val updatedStudentProjects = state.studentProjects.map {
                            if (it.id == projectId) it.copy(commentCount = it.commentCount + 1) else it
                        }
                        state.copy(feed = updatedFeed, studentProjects = updatedStudentProjects)
                    }
                }
                is ApiResult.Error -> {
                    _uiState.update { it.copy(isCommenting = false, commentError = result.message) }
                }
            }
        }
    }

    fun loadStudentProjects(studentId: Long) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            when (val result = repository.getStudentProjects(studentId)) {
                is ApiResult.Success -> {
                    _uiState.update { it.copy(isLoading = false, studentProjects = result.data) }
                }
                is ApiResult.Error -> {
                    _uiState.update { it.copy(isLoading = false, error = result.message) }
                }
            }
        }
    }

    fun uploadProject(
        name: String,
        description: String,
        techStack: String,
        githubUrl: String?,
        demoUrl: String?,
        imageUrls: List<String>? = null
    ) {
        viewModelScope.launch {
            _uiState.update { it.copy(isUploading = true, uploadError = null, uploadSuccess = false) }
            val request = UploadProjectRequest(name, description, techStack, githubUrl, demoUrl, imageUrls)
            when (val result = repository.uploadProject(request)) {
                is ApiResult.Success -> {
                    _uiState.update { it.copy(isUploading = false, uploadSuccess = true) }
                }
                is ApiResult.Error -> {
                    _uiState.update { it.copy(isUploading = false, uploadError = result.message) }
                }
            }
        }
    }

    fun deleteProject(projectId: Long) {
        viewModelScope.launch {
            when (val result = repository.deleteProject(projectId)) {
                is ApiResult.Success -> {
                    _uiState.update { state ->
                        val updatedStudentProjects = state.studentProjects.filter { it.id != projectId }
                        val updatedFeed = state.feed.filter { it.id != projectId }
                        state.copy(studentProjects = updatedStudentProjects, feed = updatedFeed)
                    }
                }
                is ApiResult.Error -> {
                    _uiState.update { it.copy(error = result.message) }
                }
            }
        }
    }

    fun resetUploadSuccess() {
        _uiState.update { it.copy(uploadSuccess = false, uploadError = null) }
    }
}
