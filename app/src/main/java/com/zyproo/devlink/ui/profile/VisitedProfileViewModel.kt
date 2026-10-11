package com.zyproo.devlink.ui.profile

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.zyproo.devlink.common.ApiResult
import com.zyproo.devlink.data.remote.dto.*
import com.zyproo.devlink.data.repository.CertificateRepository
import com.zyproo.devlink.data.repository.FeedbackRepository
import com.zyproo.devlink.data.repository.ProjectRepository
import com.zyproo.devlink.data.repository.SkillRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class VisitedProfileUiState(
    val projects: List<ProjectResponse> = emptyList(),
    val certificates: List<CertificateResponse> = emptyList(),
    val approvedSkills: List<StudentSkillResponse> = emptyList(),
    val teacherFeedbacks: List<TeacherFeedbackResponse> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null,
    val feedbackSuccess: Boolean = false
)

@HiltViewModel
class VisitedProfileViewModel @Inject constructor(
    private val projectRepository: ProjectRepository,
    private val certificateRepository: CertificateRepository,
    private val skillRepository: SkillRepository,
    private val feedbackRepository: FeedbackRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(VisitedProfileUiState())
    val uiState: StateFlow<VisitedProfileUiState> = _uiState.asStateFlow()

    fun loadVisitedProfileData(studentId: Long) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            
            val projectsDeferred = async { projectRepository.getStudentProjects(studentId) }
            val certsDeferred = async { certificateRepository.getStudentCertificates(studentId) }
            val skillsDeferred = async { skillRepository.getStudentApprovedSkillsById(studentId) }
            val feedbackDeferred = async { feedbackRepository.getStudentFeedbacks(studentId) }

            val projectsResult = projectsDeferred.await()
            val certsResult = certsDeferred.await()
            val skillsResult = skillsDeferred.await()
            val feedbackResult = feedbackDeferred.await()

            val fetchedProjects = when (projectsResult) {
                is ApiResult.Success -> projectsResult.data
                else -> emptyList()
            }

            val fetchedCerts = when (certsResult) {
                is ApiResult.Success -> certsResult.data
                else -> emptyList()
            }

            val fetchedSkills = if (skillsResult.isSuccess) {
                skillsResult.getOrDefault(emptyList())
            } else {
                emptyList()
            }

            val fetchedFeedbacks = when (feedbackResult) {
                is ApiResult.Success -> feedbackResult.data
                else -> emptyList()
            }

            _uiState.update {
                it.copy(
                    isLoading = false,
                    projects = fetchedProjects,
                    certificates = fetchedCerts,
                    approvedSkills = fetchedSkills,
                    teacherFeedbacks = fetchedFeedbacks
                )
            }
        }
    }

    fun submitTeacherFeedback(studentId: Long, content: String, onComplete: () -> Unit = {}) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null, feedbackSuccess = false) }
            when (val result = feedbackRepository.addFeedback(studentId, CreateFeedbackRequest(content))) {
                is ApiResult.Success -> {
                    _uiState.update { it.copy(isLoading = false, feedbackSuccess = true) }
                    loadVisitedProfileData(studentId)
                    onComplete()
                }
                is ApiResult.Error -> {
                    _uiState.update { it.copy(isLoading = false, error = result.message) }
                }
            }
        }
    }

    fun clearVisitedProfileData() {
        _uiState.update { VisitedProfileUiState() }
    }
}
