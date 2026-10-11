package com.zyproo.devlink.data.remote.dto



// ── Requests ───

data class LoginRequest(
    val email: String,
    val password: String
)

data class VerifyOtpRequest(
    val email: String,
    val otp: String
)

data class RefreshTokenRequest(
    val refreshToken: String
)

// ── Responses ────────────────────────────────────────────────────────

data class LoginResponse(
    val message: String,
    val email: String,
    val otpExpiryMinutes: Int
)

data class AuthResponse(
    val accessToken: String,
    val refreshToken: String,
    val tokenType: String,
    val userId: Long,
    val username: String,
    val email: String,
    val role: String,
    val homeRoute: String
)

/** Mirrors the backend's GlobalExceptionHandler.ApiError shape, for parsing error response bodies. */
data class ApiError(
    val timestamp: String? = null,
    val status: Int? = null,
    val error: String? = null,
    val message: String? = null,
    val path: String? = null
)
