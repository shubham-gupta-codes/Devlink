package com.zyproo.devlink.ui.profile

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.zyproo.devlink.common.ApiResult
import com.zyproo.devlink.data.remote.dto.*
import com.zyproo.devlink.data.repository.ProfileRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class ProfileUiState(
    val profile: ProfileResponse? = null,
    val rankings: List<ProfileResponse> = emptyList(),
    val searchResults: List<ProfileResponse> = emptyList(),
    val isSearching: Boolean = false,
    val searchError: String? = null,
    val isLoading: Boolean = false,
    val error: String? = null,
    val isSaving: Boolean = false,
    val saveError: String? = null,
    val saveSuccess: Boolean = false
)

@HiltViewModel
class ProfileViewModel @Inject constructor(
    private val repository: ProfileRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(ProfileUiState())
    val uiState: StateFlow<ProfileUiState> = _uiState.asStateFlow()

    fun loadProfile() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            when (val result = repository.getMyProfile()) {
                is ApiResult.Success -> {
                    _uiState.update { it.copy(isLoading = false, profile = result.data) }
                }
                is ApiResult.Error -> {
                    _uiState.update { it.copy(isLoading = false, error = result.message) }
                }
            }
        }
    }

    fun updateProfile(
        username: String?,
        name: String?,
        course: Course?,
        branch: Branch?,
        admissionYear: Int?,
        semester: Int?,
        bio: String?,
        contact: String?,
        photoUrl: String?,
        employeeId: Long?,
        department: String?,
        designation: String?,
        companyName: String?,
        companyLogo: String?,
        socialLinks: List<SocialLinkRequest>?,
        about: String? = null,
        website: String? = null
    ) {
        viewModelScope.launch {
            _uiState.update { it.copy(isSaving = true, saveError = null, saveSuccess = false) }
            val request = UpdateProfileRequest(
                username = username?.takeIf { it.isNotBlank() },
                name = name,
                course = course,
                branch = branch,
                admissionYear = admissionYear,
                semester = semester,
                bio = bio,
                contact = contact,
                photoUrl = photoUrl,
                employeeId = employeeId,
                department = department,
                designation = designation,
                companyName = companyName,
                companyLogo = companyLogo,
                about = about,
                website = website,
                socialLinks = socialLinks
            )
            when (val result = repository.updateProfile(request)) {
                is ApiResult.Success -> {
                    _uiState.update {
                        it.copy(
                            isSaving = false,
                            profile = result.data,
                            saveSuccess = true
                        )
                    }
                }
                is ApiResult.Error -> {
                    _uiState.update { it.copy(isSaving = false, saveError = result.message) }
                }
            }
        }
    }

    fun loadStudentRankings(course: Course? = null, branch: Branch? = null, admissionYear: Int? = null) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            when (val result = repository.getStudentRankings(course, branch, admissionYear)) {
                is ApiResult.Success -> {
                    _uiState.update { it.copy(isLoading = false, rankings = result.data) }
                }
                is ApiResult.Error -> {
                    _uiState.update { it.copy(isLoading = false, error = result.message) }
                }
            }
        }
    }

    fun searchStudents(
        query: String?,
        course: Course?,
        branch: Branch?,
        admissionYear: Int?,
        semester: Int?,
        skill: String?,
        badge: String?
    ) {
        viewModelScope.launch {
            _uiState.update { it.copy(isSearching = true, searchError = null) }
            when (val result = repository.searchStudents(query, course, branch, admissionYear, semester, skill, badge)) {
                is ApiResult.Success -> {
                    _uiState.update { it.copy(isSearching = false, searchResults = result.data) }
                }
                is ApiResult.Error -> {
                    _uiState.update { it.copy(isSearching = false, searchError = result.message) }
                }
            }
        }
    }

    fun resetSaveSuccess() {
        _uiState.update { it.copy(saveSuccess = false, saveError = null) }
    }

    fun fetchProfileByUsername(username: String, onResult: (ProfileResponse?) -> Unit) {
        viewModelScope.launch {
            when (val result = repository.getProfileByUsername(username)) {
                is ApiResult.Success -> onResult(result.data)
                else -> onResult(null)
            }
        }
    }

    fun fetchProfileById(userId: Long, onResult: (ProfileResponse?) -> Unit) {
        viewModelScope.launch {
            when (val result = repository.getProfileById(userId)) {
                is ApiResult.Success -> onResult(result.data)
                else -> onResult(null)
            }
        }
    }
}
