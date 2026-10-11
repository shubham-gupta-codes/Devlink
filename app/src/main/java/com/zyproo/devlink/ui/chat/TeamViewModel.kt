package com.zyproo.devlink.ui.chat

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.zyproo.devlink.data.remote.dto.*
import com.zyproo.devlink.data.repository.TeamRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class TeamViewModel @Inject constructor(
    private val teamRepository: TeamRepository
) : ViewModel() {

    private val _myTeams = MutableStateFlow<List<TeamResponse>>(emptyList())
    val myTeams: StateFlow<List<TeamResponse>> = _myTeams.asStateFlow()

    private val _activeTeam = MutableStateFlow<TeamResponse?>(null)
    val activeTeam: StateFlow<TeamResponse?> = _activeTeam.asStateFlow()

    private val _pendingInvites = MutableStateFlow<List<TeamInviteResponse>>(emptyList())
    val pendingInvites: StateFlow<List<TeamInviteResponse>> = _pendingInvites.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error.asStateFlow()

    fun loadMyTeams() {
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null
            teamRepository.getMyTeams()
                .onSuccess {
                    _myTeams.value = it
                    _isLoading.value = false
                }
                .onFailure {
                    _error.value = it.message
                    _isLoading.value = false
                }
        }
    }

    fun loadTeam(teamId: Long) {
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null
            teamRepository.getTeam(teamId)
                .onSuccess {
                    _activeTeam.value = it
                    _isLoading.value = false
                }
                .onFailure {
                    _error.value = it.message
                    _isLoading.value = false
                }
        }
    }

    fun createTeam(name: String, description: String?, photoUrl: String?, onComplete: () -> Unit) {
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null
            teamRepository.createTeam(name, description, photoUrl)
                .onSuccess {
                    loadMyTeams()
                    onComplete()
                }
                .onFailure {
                    _error.value = it.message
                    _isLoading.value = false
                }
        }
    }

    fun updateTeam(teamId: Long, name: String, description: String?, photoUrl: String?, onComplete: () -> Unit) {
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null
            teamRepository.updateTeam(teamId, name, description, photoUrl)
                .onSuccess {
                    loadMyTeams()
                    if (_activeTeam.value?.id == teamId) {
                        _activeTeam.value = it
                    }
                    onComplete()
                }
                .onFailure {
                    _error.value = it.message
                    _isLoading.value = false
                }
        }
    }

    fun inviteUser(teamId: Long, username: String, onComplete: () -> Unit) {
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null
            teamRepository.inviteUser(teamId, username)
                .onSuccess {
                    _isLoading.value = false
                    onComplete()
                }
                .onFailure {
                    _error.value = it.message
                    _isLoading.value = false
                }
        }
    }

    fun loadPendingInvites() {
        viewModelScope.launch {
            teamRepository.getPendingInvites()
                .onSuccess {
                    _pendingInvites.value = it
                }
        }
    }

    fun respondToInvite(inviteId: Long, accept: Boolean, onComplete: () -> Unit) {
        viewModelScope.launch {
            _isLoading.value = true
            teamRepository.respondToInvite(inviteId, accept)
                .onSuccess {
                    loadPendingInvites()
                    loadMyTeams()
                    onComplete()
                }
                .onFailure {
                    _error.value = it.message
                    _isLoading.value = false
                }
        }
    }
}
