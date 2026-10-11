

package com.zyproo.devlink.ui.profile

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.zyproo.devlink.data.remote.dto.*
import com.zyproo.devlink.data.repository.NotificationRepository
import com.zyproo.devlink.data.repository.SkillRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

data class SkillUiState(
    val skillsCatalog: List<SkillResponse> = emptyList(),
    val studentRequests: List<SkillRequestResponse> = emptyList(),
    val approvedSkills: List<StudentSkillResponse> = emptyList(),
    val teacherQueue: List<SkillRequestResponse>
    = emptyList(),
    val notifications: List<NotificationResponse> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null,
    val requestSuccess: Boolean = false,
    val decisionSuccess: Boolean = false
)


@HiltViewModel
class SkillViewModel @Inject constructor(
    private val skillRepository: SkillRepository,
    private val notificationRepository: NotificationRepository
) : ViewModel() {

    var state by mutableStateOf(SkillUiState())
        private set

    fun resetRequestSuccess() {
        state = state.copy(requestSuccess = false)
    }

    fun resetDecisionSuccess() {
        state = state.copy(decisionSuccess = false)
    }

    fun loadSkillsCatalog() {
        viewModelScope.launch {
            state = state.copy(isLoading = true, error = null)
            skillRepository.getAllSkills()
                .onSuccess { catalog ->
                    state = state.copy(skillsCatalog = catalog, isLoading = false)
                }
                .onFailure { error ->
                    state = state.copy(error = error.localizedMessage ?: "Failed to load skill catalog", isLoading = false)
                }
        }
    }

    fun loadStudentSkills() {
        viewModelScope.launch {
            state = state.copy(isLoading = true, error = null)
            skillRepository.getStudentApprovedSkills()
                .onSuccess { approved ->
                    state = state.copy(approvedSkills = approved, isLoading = false)
                }
                .onFailure { error ->
                    state = state.copy(error = error.localizedMessage ?: "Failed to load approved skills", isLoading = false)
                }
        }
    }

    fun loadStudentRequests() {
        viewModelScope.launch {
            state = state.copy(isLoading = true, error = null)
            skillRepository.getStudentRequests()
                .onSuccess { requests ->
                    state = state.copy(studentRequests = requests, isLoading = false)
                }
                .onFailure { error ->
                    state = state.copy(error = error.localizedMessage ?: "Failed to load requested skills", isLoading = false)
                }
        }
    }

    fun requestSkillVerification(skillId: Long) {
        viewModelScope.launch {
            state = state.copy(isLoading = true, error = null)
            skillRepository.requestSkillVerification(skillId)
                .onSuccess {
                    state = state.copy(requestSuccess = true, isLoading = false)
                    loadStudentRequests()
                }
                .onFailure { error ->
                    state = state.copy(error = error.localizedMessage ?: "Failed to request skill evaluation", isLoading = false)
                }
        }
    }

    fun loadTeacherQueue() {
        viewModelScope.launch {
            state = state.copy(isLoading = true, error = null)
            skillRepository.getTeacherPendingRequests()
                .onSuccess { queue ->
                    state = state.copy(teacherQueue = queue.filter { it.status == SkillStatus.PENDING || it.status == SkillStatus.MEET }, isLoading = false)
                }
                .onFailure { error ->
                    state = state.copy(error = error.localizedMessage ?: "Failed to load pending queue", isLoading = false)
                }
        }
    }

    fun scheduleMeeting(requestId: Long, meetingDate: String, notes: String) {
        viewModelScope.launch {
            state = state.copy(isLoading = true, error = null)
            skillRepository.scheduleMeeting(requestId, meetingDate, notes)
                .onSuccess {
                    state = state.copy(decisionSuccess = true, isLoading = false)
                    loadTeacherQueue()
                }
                .onFailure { error ->
                    state = state.copy(error = error.localizedMessage ?: "Failed to schedule evaluation", isLoading = false)
                }
        }
    }

    fun approveRequest(requestId: Long, level: SkillLevel, points: Int) {
        viewModelScope.launch {
            state = state.copy(isLoading = true, error = null)
            skillRepository.approveRequest(requestId, level, points)
                .onSuccess {
                    state = state.copy(decisionSuccess = true, isLoading = false)
                    loadTeacherQueue()
                }
                .onFailure { error ->
                    state = state.copy(error = error.localizedMessage ?: "Failed to approve request", isLoading = false)
                }
        }
    }

    fun rejectRequest(requestId: Long, reason: String) {
        viewModelScope.launch {
            state = state.copy(isLoading = true, error = null)
            skillRepository.rejectRequest(requestId, reason)
                .onSuccess {
                    state = state.copy(decisionSuccess = true, isLoading = false)
                    loadTeacherQueue()
                }
                .onFailure { error ->
                    state = state.copy(error = error.localizedMessage ?: "Failed to reject request", isLoading = false)
                }
        }
    }

    fun loadNotifications() {
        viewModelScope.launch {
            state = state.copy(isLoading = true, error = null)
            notificationRepository.getMyNotifications()
                .onSuccess { list ->
                    state = state.copy(notifications = list, isLoading = false)
                }
                .onFailure { error ->
                    state = state.copy(error = error.localizedMessage ?: "Failed to load notifications", isLoading = false)
                }
        }
    }

    fun markNotificationAsRead(notificationId: Long) {
        viewModelScope.launch {
            notificationRepository.markAsRead(notificationId)
                .onSuccess {
                    state = state.copy(
                        notifications = state.notifications.map {
                            if (it.id == notificationId) it.copy(read = true) else it
                        }
                    )
                }
        }
    }
}
