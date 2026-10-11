package com.zyproo.devlink.data.repository

import com.zyproo.devlink.common.ApiResult
import com.zyproo.devlink.data.remote.dto.AuthResponse
import com.zyproo.devlink.data.remote.dto.LoginResponse

interface AuthRepository {
    suspend fun login(email: String, password: String): ApiResult<LoginResponse>
    suspend fun verifyOtp(email: String, otp: String): ApiResult<AuthResponse>
    suspend fun requestForgotPassword(email: String): ApiResult<Unit>
    suspend fun resetPassword(email: String, otp: String, newPassword: String): ApiResult<Unit>
}
