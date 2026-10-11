package com.zyproo.devlink.ui.profile

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.zyproo.devlink.common.ApiResult
import com.zyproo.devlink.data.remote.dto.*
import com.zyproo.devlink.data.repository.VerificationRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class TeacherVerificationUiState(
    val projectsQueue: List<VerificationRequestResponse> = emptyList(),
    val certificatesQueue: List<VerificationRequestResponse> = emptyList(),
    val analytics: TeacherAnalyticsResponse? = null,
    val isLoading: Boolean = false,
    val error: String? = null,
    val isEvaluating: Boolean = false,
    val evaluationSuccess: Boolean = false,
    val evaluationError: String? = null
)

@HiltViewModel
class TeacherVerificationViewModel @Inject constructor(
    private val repository: VerificationRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(TeacherVerificationUiState())
    val uiState: StateFlow<TeacherVerificationUiState> = _uiState.asStateFlow()

    fun loadQueues() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            
            val projectsResult = repository.getTeacherQueue(EntityType.PROJECT)
            val certsResult = repository.getTeacherQueue(EntityType.CERTIFICATE)
            val analyticsResult = repository.getTeacherAnalytics()

            if (projectsResult is ApiResult.Success && certsResult is ApiResult.Success) {
                val analyticsData = (analyticsResult as? ApiResult.Success)?.data
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        projectsQueue = projectsResult.data,
                        certificatesQueue = certsResult.data,
                        analytics = analyticsData
                    )
                }
            } else {
                val errMsg = (projectsResult as? ApiResult.Error)?.message 
                    ?: (certsResult as? ApiResult.Error)?.message 
                    ?: "Failed to load queues"
                _uiState.update { it.copy(isLoading = false, error = errMsg) }
            }
        }
    }

    fun evaluateRequest(
        requestId: Long,
        status: VerificationStatus,
        remarks: String? = null,
        points: Int? = null,
        scheduleAt: String? = null
    ) {
        viewModelScope.launch {
            _uiState.update { it.copy(isEvaluating = true, evaluationError = null, evaluationSuccess = false) }
            val request = EvaluateVerificationRequest(status, remarks, points, scheduleAt)
            when (val result = repository.evaluateRequest(requestId, request)) {
                is ApiResult.Success -> {
                    _uiState.update { it.copy(isEvaluating = false, evaluationSuccess = true) }
                    loadQueues()
                }
                is ApiResult.Error -> {
                    _uiState.update { it.copy(isEvaluating = false, evaluationError = result.message) }
                }
            }
        }
    }

    fun resetEvaluationSuccess() {
        _uiState.update { it.copy(evaluationSuccess = false, evaluationError = null) }
    }
}
