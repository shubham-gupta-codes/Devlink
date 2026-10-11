package com.zyproo.devlink.data.remote

import com.zyproo.devlink.data.remote.dto.AuthResponse
import com.zyproo.devlink.data.remote.dto.LoginRequest
import com.zyproo.devlink.data.remote.dto.LoginResponse
import com.zyproo.devlink.data.remote.dto.RefreshTokenRequest
import com.zyproo.devlink.data.remote.dto.VerifyOtpRequest
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.POST


interface AuthApi {

    @POST("api/user/login")
    suspend fun login(@Body request: LoginRequest): Response<LoginResponse>

    @POST("api/user/verify")
    suspend fun verifyOtp(@Body request: VerifyOtpRequest): Response<AuthResponse>

    @POST("api/user/refresh-token")
    suspend fun refreshToken(@Body request: RefreshTokenRequest): Response<AuthResponse>

    @POST("api/user/forgot-password/request")
    suspend fun requestForgotPassword(@Body request: ForgotPasswordRequest): Response<Unit>

    @POST("api/user/forgot-password/reset")
    suspend fun resetPassword(@Body request: ResetPasswordRequest): Response<Unit>
}

data class ForgotPasswordRequest(val email: String)
data class ResetPasswordRequest(val email: String, val otp: String, val newPassword: String)
