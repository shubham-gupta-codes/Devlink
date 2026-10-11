package com.zyproo.devlink.ui.job

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.zyproo.devlink.common.ApiResult
import com.zyproo.devlink.data.remote.dto.*
import com.zyproo.devlink.data.repository.JobRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

data class JobUiState(
    val recruiterJobs: List<JobResponse> = emptyList(),
    val openJobs: List<JobResponse> = emptyList(),
    val currentJobDetails: JobResponse? = null,
    val jobApplications: List<JobApplicationResponse> = emptyList(),
    val studentApplications: List<JobApplicationResponse> = emptyList(),
    val savedCandidates: List<SavedCandidateResponse> = emptyList(),
    val recommendedCandidates: List<ProfileResponse> = emptyList(),
    val isLoading: Boolean = false,
    val isSaving: Boolean = false,
    val error: String? = null,
    val saveSuccess: Boolean = false,
    val applySuccess: Boolean = false
)

@HiltViewModel
class JobViewModel @Inject constructor(
    private val jobRepository: JobRepository
) : ViewModel() {

    var state by mutableStateOf(JobUiState())
        private set

    fun resetSaveSuccess() {
        state = state.copy(saveSuccess = false)
    }

    fun resetApplySuccess() {
        state = state.copy(applySuccess = false)
    }

    fun createJob(
        title: String,
        description: String,
        jobType: JobType,
        requiredSkills: List<String>,
        location: String,
        salaryRange: String?,
        deadline: String,
        startDate: String? = null
    ) {
        viewModelScope.launch {
            state = state.copy(isSaving = true, error = null, saveSuccess = false)
            val request = CreateJobRequest(
                title = title,
                description = description,
                jobType = jobType,
                requiredSkills = requiredSkills,
                location = location,
                salaryRange = salaryRange,
                deadline = deadline,
                startDate = startDate
            )
            when (val result = jobRepository.createJob(request)) {
                is ApiResult.Success -> {
                    state = state.copy(
                        isSaving = false,
                        saveSuccess = true,
                        recruiterJobs = state.recruiterJobs + result.data
                    )
                }
                is ApiResult.Error -> {
                    state = state.copy(isSaving = false, error = result.message)
                }
            }
        }
    }

    fun updateJob(
        jobId: Long,
        title: String,
        description: String,
        jobType: JobType,
        requiredSkills: List<String>,
        location: String,
        salaryRange: String?,
        deadline: String,
        startDate: String? = null,
        status: JobStatus? = null,
        onSuccess: () -> Unit = {}
    ) {
        viewModelScope.launch {
            state = state.copy(isSaving = true, error = null, saveSuccess = false)
            val request = UpdateJobRequest(
                title = title,
                description = description,
                jobType = jobType,
                requiredSkills = requiredSkills,
                location = location,
                salaryRange = salaryRange,
                deadline = deadline,
                startDate = startDate,
                status = status
            )
            when (val result = jobRepository.updateJob(jobId, request)) {
                is ApiResult.Success -> {
                    val updated = result.data
                    val updatedList = state.recruiterJobs.map { if (it.id == jobId) updated else it }
                    val currentUpdated = if (state.currentJobDetails?.id == jobId) updated else state.currentJobDetails
                    state = state.copy(
                        isSaving = false,
                        saveSuccess = true,
                        recruiterJobs = updatedList,
                        currentJobDetails = currentUpdated
                    )
                    onSuccess()
                }
                is ApiResult.Error -> {
                    state = state.copy(isSaving = false, error = result.message)
                }
            }
        }
    }

    fun deleteJob(jobId: Long, onSuccess: () -> Unit = {}) {
        viewModelScope.launch {
            state = state.copy(isLoading = true, error = null)
            when (val result = jobRepository.deleteJob(jobId)) {
                is ApiResult.Success -> {
                    val updatedList = state.recruiterJobs.filter { it.id != jobId }
                    state = state.copy(
                        isLoading = false,
                        recruiterJobs = updatedList,
                        currentJobDetails = if (state.currentJobDetails?.id == jobId) null else state.currentJobDetails
                    )
                    onSuccess()
                }
                is ApiResult.Error -> {
                    state = state.copy(isLoading = false, error = result.message)
                }
            }
        }
    }

    fun loadRecruiterJobs() {
        viewModelScope.launch {
            state = state.copy(isLoading = true, error = null)
            when (val result = jobRepository.getRecruiterJobs()) {
                is ApiResult.Success -> {
                    state = state.copy(isLoading = false, recruiterJobs = result.data)
                }
                is ApiResult.Error -> {
                    state = state.copy(isLoading = false, error = result.message)
                }
            }
        }
    }

    fun loadOpenJobs() {
        viewModelScope.launch {
            state = state.copy(isLoading = true, error = null)
            when (val result = jobRepository.getAllOpenJobs()) {
                is ApiResult.Success -> {
                    state = state.copy(isLoading = false, openJobs = result.data)
                }
                is ApiResult.Error -> {
                    state = state.copy(isLoading = false, error = result.message)
                }
            }
        }
    }

    fun loadJobDetails(jobId: Long) {
        viewModelScope.launch {
            state = state.copy(isLoading = true, error = null)
            when (val result = jobRepository.getJobDetails(jobId)) {
                is ApiResult.Success -> {
                    state = state.copy(isLoading = false, currentJobDetails = result.data)
                }
                is ApiResult.Error -> {
                    state = state.copy(isLoading = false, error = result.message)
                }
            }
        }
    }

    fun applyToJob(jobId: Long) {
        viewModelScope.launch {
            state = state.copy(isLoading = true, error = null, applySuccess = false)
            when (val result = jobRepository.applyToJob(jobId)) {
                is ApiResult.Success -> {
                    state = state.copy(isLoading = false, applySuccess = true)
                    loadOpenJobs()
                }
                is ApiResult.Error -> {
                    state = state.copy(isLoading = false, error = result.message)
                }
            }
        }
    }

    fun loadJobApplications(jobId: Long) {
        viewModelScope.launch {
            state = state.copy(isLoading = true, error = null)
            when (val result = jobRepository.getJobApplications(jobId)) {
                is ApiResult.Success -> {
                    state = state.copy(isLoading = false, jobApplications = result.data)
                }
                is ApiResult.Error -> {
                    state = state.copy(isLoading = false, error = result.message)
                }
            }
        }
    }

    fun evaluateApplication(applicationId: Long, jobId: Long, status: JobApplicationStatus) {
        viewModelScope.launch {
            state = state.copy(isLoading = true, error = null)
            when (val result = jobRepository.evaluateApplication(applicationId, status)) {
                is ApiResult.Success -> {
                    val updatedList = state.jobApplications.map {
                        if (it.id == applicationId) it.copy(status = status) else it
                    }
                    state = state.copy(isLoading = false, jobApplications = updatedList)
                }
                is ApiResult.Error -> {
                    state = state.copy(isLoading = false, error = result.message)
                }
            }
        }
    }

    fun shortlistCandidate(studentId: Long, jobId: Long?, onComplete: () -> Unit = {}) {
        viewModelScope.launch {
            state = state.copy(isLoading = true, error = null)
            when (val result = jobRepository.shortlistCandidate(studentId, jobId)) {
                is ApiResult.Success -> {
                    state = state.copy(
                        isLoading = false,
                        savedCandidates = state.savedCandidates + result.data
                    )
                    onComplete()
                }
                is ApiResult.Error -> {
                    state = state.copy(isLoading = false, error = result.message)
                }
            }
        }
    }

    fun loadSavedCandidates() {
        viewModelScope.launch {
            state = state.copy(isLoading = true, error = null)
            when (val result = jobRepository.getSavedCandidates()) {
                is ApiResult.Success -> {
                    state = state.copy(isLoading = false, savedCandidates = result.data)
                }
                is ApiResult.Error -> {
                    state = state.copy(isLoading = false, error = result.message)
                }
            }
        }
    }

    fun loadRecommendedCandidates(jobId: Long) {
        viewModelScope.launch {
            state = state.copy(isLoading = true, error = null)
            when (val result = jobRepository.getRecommendedCandidates(jobId)) {
                is ApiResult.Success -> {
                    state = state.copy(isLoading = false, recommendedCandidates = result.data)
                }
                is ApiResult.Error -> {
                    state = state.copy(isLoading = false, error = result.message)
                }
            }
        }
    }

    fun loadStudentApplications() {
        viewModelScope.launch {
            state = state.copy(isLoading = true, error = null)
            when (val result = jobRepository.getStudentApplications()) {
                is ApiResult.Success -> {
                    state = state.copy(isLoading = false, studentApplications = result.data)
                }
                is ApiResult.Error -> {
                    state = state.copy(isLoading = false, error = result.message)
                }
            }
        }
    }

    fun applyToJob(jobId: Long, onComplete: () -> Unit = {}) {
        viewModelScope.launch {
            state = state.copy(isLoading = true, error = null, applySuccess = false)
            when (val result = jobRepository.applyToJob(jobId)) {
                is ApiResult.Success -> {
                    state = state.copy(isLoading = false, applySuccess = true)
                    loadStudentApplications()
                    onComplete()
                }
                is ApiResult.Error -> {
                    state = state.copy(isLoading = false, error = result.message)
                }
            }
        }
    }
}
