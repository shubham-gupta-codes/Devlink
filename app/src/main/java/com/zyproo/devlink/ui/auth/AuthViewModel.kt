package com.zyproo.devlink.ui.auth

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.zyproo.devlink.common.ApiResult
import com.zyproo.devlink.common.Role
import com.zyproo.devlink.data.repository.AuthRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class AuthUiState(
    val email: String = "",
    val password: String = "",
    val otp: String = "",
    val isLoginLoading: Boolean = false,
    val loginError: String? = null,
    val otpExpiryMinutes: Int? = null,
    val isVerifyLoading: Boolean = false,
    val verifyError: String? = null,
    val serverHost: String = ""
)


sealed class AuthNavigationEvent {
    data class NavigateToOtpScreen(val email: String) : AuthNavigationEvent()
    data class NavigateToHome(val role: Role) : AuthNavigationEvent()
}

@HiltViewModel
class AuthViewModel @Inject constructor(
    private val authRepository: AuthRepository,
    private val sessionManager: com.zyproo.devlink.data.local.SessionManager
) : ViewModel() {

    private val _uiState = MutableStateFlow(AuthUiState())
    val uiState: StateFlow<AuthUiState> = _uiState.asStateFlow()

    private val _navigationEvents = MutableSharedFlow<AuthNavigationEvent>()
    val navigationEvents: SharedFlow<AuthNavigationEvent> = _navigationEvents.asSharedFlow()

    init {
        viewModelScope.launch {
            sessionManager.serverHostFlow.collect { host ->
                _uiState.update { it.copy(serverHost = host.orEmpty()) }
            }
        }
    }

    fun updateServerHost(host: String) {
        viewModelScope.launch {
            sessionManager.saveServerHost(host)
        }
    }

    fun onEmailChange(value: String) {
        _uiState.update { it.copy(email = value, loginError = null) }
    }

    fun onPasswordChange(value: String) {
        _uiState.update { it.copy(password = value, loginError = null) }
    }

    fun onOtpChange(value: String) {
        if (value.length <= 6 && value.all { it.isDigit() || it == ' ' }) {
            _uiState.update { it.copy(otp = value, verifyError = null) }
        }
    }

    fun clearOtp() {
        _uiState.update { it.copy(otp = "", verifyError = null) }
    }

    fun login() {
        val state = _uiState.value
        if (state.email.isBlank() || state.password.isBlank()) {
            _uiState.update { it.copy(loginError = "Please enter both email and password.") }
            return
        }

        viewModelScope.launch {
            _uiState.update { it.copy(isLoginLoading = true, loginError = null) }

            when (val result = authRepository.login(state.email.trim(), state.password)) {
                is ApiResult.Success -> {
                    _uiState.update {
                        it.copy(
                            isLoginLoading = false,
                            otpExpiryMinutes = result.data.otpExpiryMinutes
                        )
                    }
                    _navigationEvents.emit(AuthNavigationEvent.NavigateToOtpScreen(state.email.trim()))
                }
                is ApiResult.Error -> {
                    val isConnectionError = result.message.contains("connection", ignoreCase = true) ||
                                            result.message.contains("reach the server", ignoreCase = true)
                    if (isConnectionError) {
                        _uiState.update {
                            it.copy(
                                isLoginLoading = false,
                                otpExpiryMinutes = 5
                            )
                        }
                        _navigationEvents.emit(AuthNavigationEvent.NavigateToOtpScreen(state.email.trim()))
                    } else {
                        _uiState.update { it.copy(isLoginLoading = false, loginError = result.message) }
                    }
                }
            }
        }
    }

    fun verifyOtp() {
        val state = _uiState.value
        val cleanOtp = state.otp.trim().replace(" ", "")
        if (cleanOtp.length != 6 || !cleanOtp.all { it.isDigit() }) {
            _uiState.update { it.copy(verifyError = "Please enter the complete 6-digit verification code.") }
            return
        }

        viewModelScope.launch {
            _uiState.update { it.copy(isVerifyLoading = true, verifyError = null) }

            when (val result = authRepository.verifyOtp(state.email.trim(), cleanOtp)) {
                is ApiResult.Success -> {
                    _uiState.update { it.copy(isVerifyLoading = false) }
                    val role = Role.fromString(result.data.role)
                    _navigationEvents.emit(AuthNavigationEvent.NavigateToHome(role))
                }
                is ApiResult.Error -> {
                    _uiState.update { it.copy(isVerifyLoading = false, verifyError = result.message) }
                }
            }
        }
    }

    fun requestForgotPassword(email: String, onSent: () -> Unit) {
        if (email.isBlank()) {
            _uiState.update { it.copy(loginError = "Please enter your email.") }
            return
        }
        viewModelScope.launch {
            _uiState.update { it.copy(isLoginLoading = true, loginError = null) }
            when (val result = authRepository.requestForgotPassword(email.trim())) {
                is ApiResult.Success -> {
                    _uiState.update { it.copy(isLoginLoading = false) }
                    onSent()
                }
                is ApiResult.Error -> {
                    _uiState.update { it.copy(isLoginLoading = false, loginError = result.message) }
                }
            }
        }
    }

    fun resetPassword(email: String, otp: String, newPassword: String, onReset: () -> Unit) {
        if (email.isBlank() || otp.isBlank() || newPassword.isBlank()) {
            _uiState.update { it.copy(loginError = "Please fill in all fields.") }
            return
        }
        viewModelScope.launch {
            _uiState.update { it.copy(isLoginLoading = true, loginError = null) }
            when (val result = authRepository.resetPassword(email.trim(), otp.trim(), newPassword)) {
                is ApiResult.Success -> {
                    _uiState.update { it.copy(isLoginLoading = false) }
                    onReset()
                }
                is ApiResult.Error -> {
                    _uiState.update { it.copy(isLoginLoading = false, loginError = result.message) }
                }
            }
        }
    }

    fun setEmail(email: String) {
        _uiState.update { it.copy(email = email) }
    }
    fun resendOtp() {
        val state = _uiState.value
        viewModelScope.launch {
            _uiState.update { it.copy(isVerifyLoading = true, verifyError = null) }
            when (val result = authRepository.login(state.email.trim(), state.password)) {
                is ApiResult.Success -> {
                    _uiState.update {
                        it.copy(
                            isVerifyLoading = false,
                            otpExpiryMinutes = result.data.otpExpiryMinutes
                        )
                    }
                }
                is ApiResult.Error -> {
                    _uiState.update { it.copy(isVerifyLoading = false, verifyError = result.message) }
                }
            }
        }
    }

    fun logout(onLoggedOut: () -> Unit = {}) {
        viewModelScope.launch {
            sessionManager.clearSession()
            onLoggedOut()
        }
    }
}



