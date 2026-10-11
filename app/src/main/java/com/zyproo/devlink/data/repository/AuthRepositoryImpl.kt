package com.zyproo.devlink.data.repository


import com.zyproo.devlink.data.local.SessionManager
import com.zyproo.devlink.data.remote.AuthApi
import com.zyproo.devlink.data.remote.dto.ApiError
import com.zyproo.devlink.data.remote.dto.AuthResponse
import com.zyproo.devlink.data.remote.dto.LoginRequest
import com.zyproo.devlink.data.remote.dto.LoginResponse
import com.zyproo.devlink.data.remote.dto.VerifyOtpRequest
import com.google.gson.Gson
import com.zyproo.devlink.common.ApiResult
import retrofit2.Response
import java.io.IOException
import javax.inject.Inject

class AuthRepositoryImpl @Inject constructor(
    private val api: AuthApi,
    private val sessionManager: SessionManager,
    private val gson: Gson
) : AuthRepository {

    override suspend fun login(email: String, password: String): ApiResult<LoginResponse> =
        safeCall { api.login(LoginRequest(email, password)) }

    override suspend fun verifyOtp(email: String, otp: String): ApiResult<AuthResponse> {
        val result = safeCall { api.verifyOtp(VerifyOtpRequest(email, otp)) }
        if (result is ApiResult.Success) {
            sessionManager.saveSession(result.data)
        }
        return result
    }

    override suspend fun requestForgotPassword(email: String): ApiResult<Unit> =
        safeCall { api.requestForgotPassword(com.zyproo.devlink.data.remote.ForgotPasswordRequest(email)) }

    override suspend fun resetPassword(email: String, otp: String, newPassword: String): ApiResult<Unit> =
        safeCall { api.resetPassword(com.zyproo.devlink.data.remote.ResetPasswordRequest(email, otp, newPassword)) }

    private suspend fun <T> safeCall(call: suspend () -> Response<T>): ApiResult<T> {
        return try {
            val response = call()
            val body = response.body()

            if (response.isSuccessful && body != null) {
                ApiResult.Success(body)
            } else {
                val errorMessage = parseErrorMessage(response)
                ApiResult.Error(errorMessage)
            }
        } catch (e: IOException) {
            // No internet, backend unreachable, connection dropped, etc.
            ApiResult.Error("Can't reach the server. Check your connection and try again.")
        } catch (e: Exception) {
            ApiResult.Error(e.localizedMessage ?: "Something went wrong. Please try again.")
        }
    }

    private fun parseErrorMessage(response: Response<*>): String {
        val errorBody = response.errorBody()?.string()
        return try {
            val apiError = gson.fromJson(errorBody, ApiError::class.java)
            apiError?.message ?: "Something went wrong (HTTP ${response.code()})."
        } catch (e: Exception) {
            "Something went wrong (HTTP ${response.code()})."
        }
    }
}
