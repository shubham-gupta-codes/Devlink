package com.zyproo.devlink.ui.file

import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.zyproo.devlink.common.ApiResult
import com.zyproo.devlink.data.repository.FileRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class FileUploadUiState(
    val isUploading: Boolean = false,
    val uploadedUrl: String? = null,
    val error: String? = null
)

@HiltViewModel
class FileViewModel @Inject constructor(
    private val fileRepository: FileRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(FileUploadUiState())
    val uiState: StateFlow<FileUploadUiState> = _uiState.asStateFlow()

    fun uploadFile(uri: Uri, onSuccess: (String) -> Unit) {
        viewModelScope.launch {
            _uiState.update { it.copy(isUploading = true, error = null) }
            when (val result = fileRepository.uploadFile(uri)) {
                is ApiResult.Success -> {
                    _uiState.update { it.copy(isUploading = false, uploadedUrl = result.data) }
                    onSuccess(result.data)
                }
                is ApiResult.Error -> {
                    _uiState.update { it.copy(isUploading = false, error = result.message) }
                }
            }
        }
    }
}
