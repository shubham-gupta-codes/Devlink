package com.zyproo.devlink.ui.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.zyproo.devlink.common.Role
import com.zyproo.devlink.ui.auth.LoginScreen
import com.zyproo.devlink.ui.auth.OtpVerificationScreen
import com.zyproo.devlink.ui.home.DashboardScreen
import com.zyproo.devlink.ui.home.PlaceholderHomeScreen
import com.zyproo.devlink.ui.onboarding.OnboardingScreen
import com.zyproo.devlink.ui.splash.SplashScreen

import androidx.hilt.navigation.compose.hiltViewModel
import com.zyproo.devlink.ui.auth.AuthViewModel

@Composable
fun DevLinkNavHost(navController: NavHostController = rememberNavController()) {
    val authViewModel: AuthViewModel = hiltViewModel()
    val handleLogout = {
        authViewModel.logout {
            navController.navigate(DevLinkDestinations.LOGIN) {
                popUpTo(0) { inclusive = true }
            }
        }
    }

    NavHost(
        navController = navController,
        startDestination = DevLinkDestinations.SPLASH
    ) {
        composable(DevLinkDestinations.SPLASH) {
            SplashScreen(
                onNavigateToOnboarding = {
                    navController.navigate(DevLinkDestinations.ONBOARDING) {
                        popUpTo(DevLinkDestinations.SPLASH) { inclusive = true }
                    }
                },
                onNavigateToLogin = {
                    navController.navigate(DevLinkDestinations.LOGIN) {
                        popUpTo(DevLinkDestinations.SPLASH) { inclusive = true }
                    }
                },
                onNavigateToHome = { route ->
                    navController.navigate(route) {
                        popUpTo(DevLinkDestinations.SPLASH) { inclusive = true }
                    }
                }
            )
        }

        composable(DevLinkDestinations.ONBOARDING) {
            OnboardingScreen(
                onFinishOnboarding = {
                    navController.navigate(DevLinkDestinations.LOGIN) {
                        popUpTo(DevLinkDestinations.ONBOARDING) { inclusive = true }
                    }
                }
            )
        }

        composable(DevLinkDestinations.LOGIN) {
            LoginScreen(
                onOtpSent = { email ->
                    navController.navigate(DevLinkDestinations.otpVerificationRoute(email))
                }
            )
        }

        composable(
            route = DevLinkDestinations.OTP_VERIFICATION,
            arguments = listOf(
                navArgument("email") { type = NavType.StringType }
            )
        ) { backStackEntry ->
            val email = backStackEntry.arguments?.getString("email") ?: ""

            OtpVerificationScreen(
                email = email,
                onVerified = { homeRoute ->
                    navController.navigate(homeRoute) {
                        popUpTo(0) { inclusive = true }
                    }
                },
                onBack = { navController.popBackStack() }
            )
        }

        composable(DevLinkDestinations.HOME_STUDENT) {
            DashboardScreen(
                role = Role.STUDENT,
                onLogoutClick = { handleLogout() }
            )
        }
        composable(DevLinkDestinations.HOME_TEACHER) {
            DashboardScreen(
                role = Role.TEACHER,
                onLogoutClick = { handleLogout() }
            )
        }
        composable(DevLinkDestinations.HOME_ADMIN) {
            DashboardScreen(
                role = Role.ADMIN,
                onLogoutClick = { handleLogout() }
            )
        }
        composable(DevLinkDestinations.HOME_RECRUITER) {
            DashboardScreen(
                role = Role.RECRUITER,
                onLogoutClick = { handleLogout() }
            )
        }
    }
}

