package com.zyproo.devlink.ui.splash

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.zyproo.devlink.common.Role
import com.zyproo.devlink.data.local.SessionManager
import com.zyproo.devlink.ui.navigation.DevLinkDestinations
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import javax.inject.Inject

sealed class SplashDestination {
    data object Loading : SplashDestination()
    data object Onboarding : SplashDestination()
    data object Login : SplashDestination()
    data class AlreadyLoggedIn(val route: String) : SplashDestination()
}

@HiltViewModel
class SplashViewModel @Inject constructor(
    private val sessionManager: SessionManager
) : ViewModel() {

    private val _destination = MutableStateFlow<SplashDestination>(SplashDestination.Loading)
    val destination: StateFlow<SplashDestination> = _destination.asStateFlow()

    init {
        viewModelScope.launch {
            val isLoggedIn = sessionManager.isLoggedInFlow.first()
            val isOnboardingCompleted = sessionManager.onboardingCompletedFlow.first()

            val destination = if (isLoggedIn) {
                val role = sessionManager.roleFlow.first()?.let(Role::fromString)
                if (role != null) {
                    SplashDestination.AlreadyLoggedIn(DevLinkDestinations.homeRouteFor(role))
                } else if (isOnboardingCompleted) {
                    SplashDestination.Login
                } else {
                    SplashDestination.Onboarding
                }
            } else if (isOnboardingCompleted) {
                SplashDestination.Login
            } else {
                SplashDestination.Onboarding
            }
            _destination.value = destination
        }
    }
}
