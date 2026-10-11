package com.zyproo.devlink.ui.profile

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.zyproo.devlink.common.ApiResult
import com.zyproo.devlink.data.remote.dto.*
import com.zyproo.devlink.data.repository.CertificateRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class CertificateUiState(
    val certificates: List<CertificateResponse> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null,
    val isUploading: Boolean = false,
    val uploadSuccess: Boolean = false,
    val uploadError: String? = null
)

@HiltViewModel
class CertificateViewModel @Inject constructor(
    private val repository: CertificateRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(CertificateUiState())
    val uiState: StateFlow<CertificateUiState> = _uiState.asStateFlow()

    fun loadStudentCertificates(studentId: Long) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            when (val result = repository.getStudentCertificates(studentId)) {
                is ApiResult.Success -> {
                    _uiState.update { it.copy(isLoading = false, certificates = result.data) }
                }
                is ApiResult.Error -> {
                    _uiState.update { it.copy(isLoading = false, error = result.message) }
                }
            }
        }
    }

    fun uploadCertificate(
        title: String,
        issuingOrg: String,
        purpose: String,
        fileUrl: String,
        fileType: FileType
    ) {
        viewModelScope.launch {
            _uiState.update { it.copy(isUploading = true, uploadError = null, uploadSuccess = false) }
            val request = UploadCertificateRequest(title, issuingOrg, purpose, fileUrl, fileType)
            when (val result = repository.uploadCertificate(request)) {
                is ApiResult.Success -> {
                    _uiState.update { it.copy(isUploading = false, uploadSuccess = true) }
                }
                is ApiResult.Error -> {
                    _uiState.update { it.copy(isUploading = false, uploadError = result.message) }
                }
            }
        }
    }

    fun resetUploadSuccess() {
        _uiState.update { it.copy(uploadSuccess = false, uploadError = null) }
    }
}
