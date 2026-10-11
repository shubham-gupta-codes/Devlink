package com.zyproo.devlink.ui.profile

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.zyproo.devlink.common.ApiResult
import com.zyproo.devlink.data.remote.dto.*
import com.zyproo.devlink.data.repository.AdminRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class AdminUiState(
    val stats: AdminDashboardStats? = null,
    val users: List<AdminUserListItem> = emptyList(),
    val pendingRecruiters: List<PendingRecruiterResponse> = emptyList(),
    val isLoading: Boolean = false,
    val isActionLoading: Boolean = false,
    val error: String? = null,
    val successMessage: String? = null,
    val roleFilter: String? = null,
    val enabledFilter: Boolean? = null
)

@HiltViewModel
class AdminViewModel @Inject constructor(
    private val adminRepository: AdminRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(AdminUiState())
    val uiState: StateFlow<AdminUiState> = _uiState

    fun loadAll() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)
            loadStats()
            loadUsers()
            loadPendingRecruiters()
            _uiState.value = _uiState.value.copy(isLoading = false)
        }
    }

    private suspend fun loadStats() {
        when (val result = adminRepository.getDashboardStats()) {
            is ApiResult.Success -> _uiState.value = _uiState.value.copy(stats = result.data)
            is ApiResult.Error -> _uiState.value = _uiState.value.copy(error = result.message)
        }
    }

    fun loadUsers(role: String? = null, enabled: Boolean? = null) {
        viewModelScope.launch {
            when (val result = adminRepository.listUsers(role, enabled)) {
                is ApiResult.Success -> _uiState.value = _uiState.value.copy(
                    users = result.data,
                    roleFilter = role,
                    enabledFilter = enabled
                )
                is ApiResult.Error -> _uiState.value = _uiState.value.copy(error = result.message)
            }
        }
    }

    fun loadPendingRecruiters() {
        viewModelScope.launch {
            when (val result = adminRepository.getPendingRecruiters()) {
                is ApiResult.Success -> _uiState.value = _uiState.value.copy(pendingRecruiters = result.data)
                is ApiResult.Error -> _uiState.value = _uiState.value.copy(error = result.message)
            }
        }
    }

    fun suspendUser(userId: Long) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isActionLoading = true)
            when (val result = adminRepository.suspendUser(userId)) {
                is ApiResult.Success -> {
                    _uiState.value = _uiState.value.copy(
                        successMessage = "User suspended successfully",
                        isActionLoading = false
                    )
                    loadUsers(_uiState.value.roleFilter, _uiState.value.enabledFilter)
                    loadStats()
                }
                is ApiResult.Error -> _uiState.value = _uiState.value.copy(
                    error = result.message,
                    isActionLoading = false
                )
            }
        }
    }

    fun reactivateUser(userId: Long) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isActionLoading = true)
            when (val result = adminRepository.reactivateUser(userId)) {
                is ApiResult.Success -> {
                    _uiState.value = _uiState.value.copy(
                        successMessage = "User reactivated successfully",
                        isActionLoading = false
                    )
                    loadUsers(_uiState.value.roleFilter, _uiState.value.enabledFilter)
                    loadStats()
                }
                is ApiResult.Error -> _uiState.value = _uiState.value.copy(
                    error = result.message,
                    isActionLoading = false
                )
            }
        }
    }

    fun deleteUser(userId: Long) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isActionLoading = true)
            when (val result = adminRepository.deleteUser(userId)) {
                is ApiResult.Success -> {
                    _uiState.value = _uiState.value.copy(
                        successMessage = "User deleted successfully",
                        isActionLoading = false
                    )
                    loadUsers(_uiState.value.roleFilter, _uiState.value.enabledFilter)
                    loadStats()
                }
                is ApiResult.Error -> _uiState.value = _uiState.value.copy(
                    error = result.message,
                    isActionLoading = false
                )
            }
        }
    }

    fun approveRecruiter(userId: Long) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isActionLoading = true)
            when (val result = adminRepository.approveRecruiter(userId)) {
                is ApiResult.Success -> {
                    _uiState.value = _uiState.value.copy(
                        successMessage = "Recruiter approved!",
                        isActionLoading = false
                    )
                    loadPendingRecruiters()
                    loadStats()
                }
                is ApiResult.Error -> _uiState.value = _uiState.value.copy(
                    error = result.message,
                    isActionLoading = false
                )
            }
        }
    }

    fun rejectRecruiter(userId: Long, reason: String) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isActionLoading = true)
            when (val result = adminRepository.rejectRecruiter(userId, reason)) {
                is ApiResult.Success -> {
                    _uiState.value = _uiState.value.copy(
                        successMessage = "Recruiter rejected",
                        isActionLoading = false
                    )
                    loadPendingRecruiters()
                    loadStats()
                }
                is ApiResult.Error -> _uiState.value = _uiState.value.copy(
                    error = result.message,
                    isActionLoading = false
                )
            }
        }
    }

    fun createUser(request: CreateUserAdminRequest) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isActionLoading = true)
            when (val result = adminRepository.createUser(request)) {
                is ApiResult.Success -> {
                    _uiState.value = _uiState.value.copy(
                        successMessage = "Account created! Credentials sent to ${request.email}",
                        isActionLoading = false
                    )
                    loadUsers(_uiState.value.roleFilter, _uiState.value.enabledFilter)
                    loadStats()
                }
                is ApiResult.Error -> _uiState.value = _uiState.value.copy(
                    error = result.message,
                    isActionLoading = false
                )
            }
        }
    }

    fun clearMessages() {
        _uiState.value = _uiState.value.copy(error = null, successMessage = null)
    }
}
