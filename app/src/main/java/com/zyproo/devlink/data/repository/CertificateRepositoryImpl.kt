package com.zyproo.devlink.data.repository

import com.google.gson.Gson
import com.zyproo.devlink.common.ApiResult
import com.zyproo.devlink.data.remote.CertificateApi
import com.zyproo.devlink.data.remote.dto.*
import retrofit2.Response
import java.io.IOException
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class CertificateRepositoryImpl @Inject constructor(
    private val api: CertificateApi,
    private val gson: Gson
) : CertificateRepository {

    override suspend fun uploadCertificate(request: UploadCertificateRequest): ApiResult<CertificateResponse> =
        safeCall { api.uploadCertificate(request) }

    override suspend fun getStudentCertificates(studentId: Long): ApiResult<List<CertificateResponse>> =
        safeCall { api.getStudentCertificates(studentId) }

    private suspend fun <T> safeCall(call: suspend () -> Response<T>): ApiResult<T> {
        return try {
            val response = call()
            val body = response.body()
            if (response.isSuccessful && body != null) {
                ApiResult.Success(body)
            } else {
                val errorBody = response.errorBody()?.string()
                val message = try {
                    val apiError = gson.fromJson(errorBody, ApiError::class.java)
                    apiError?.message
                } catch (e: Exception) {
                    null
                }
                ApiResult.Error(message ?: "Something went wrong (HTTP ${response.code()}).")
            }
        } catch (e: IOException) {
            ApiResult.Error("Can't reach the server. Check your connection.")
        } catch (e: Exception) {
            ApiResult.Error(e.localizedMessage ?: "An error occurred.")
        }
    }
}
