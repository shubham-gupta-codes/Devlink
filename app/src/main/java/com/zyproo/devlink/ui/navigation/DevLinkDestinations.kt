package com.zyproo.devlink.ui.navigation

import com.zyproo.devlink.common.Role


object DevLinkDestinations {
    const val SPLASH = "splash"
    const val ONBOARDING = "onboarding"
    const val LOGIN = "login"
    const val OTP_VERIFICATION = "otp_verification/{email}"

    fun otpVerificationRoute(email: String) = "otp_verification/$email"

    const val HOME_STUDENT = "home/student"
    const val HOME_TEACHER = "home/teacher"
    const val HOME_ADMIN = "home/admin"
    const val HOME_RECRUITER = "home/recruiter"

    /** Maps a verified Role straight to its home route — mirrors AuthService.homeRouteFor() on the backend. */
    fun homeRouteFor(role: Role): String = when (role) {
        Role.STUDENT -> HOME_STUDENT
        Role.TEACHER -> HOME_TEACHER
        Role.ADMIN -> HOME_ADMIN
        Role.RECRUITER -> HOME_RECRUITER
    }
}
