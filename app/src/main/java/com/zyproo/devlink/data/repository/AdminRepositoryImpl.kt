package com.zyproo.devlink.data.repository

import com.google.gson.Gson
import com.zyproo.devlink.common.ApiResult
import com.zyproo.devlink.data.remote.AdminApi
import com.zyproo.devlink.data.remote.dto.*
import retrofit2.Response
import java.io.IOException
import javax.inject.Inject

class AdminRepositoryImpl @Inject constructor(
    private val api: AdminApi,
    private val gson: Gson
) : AdminRepository {

    override suspend fun getDashboardStats(): ApiResult<AdminDashboardStats> =
        safeCall { api.getDashboardStats() }

    override suspend fun listUsers(role: String?, enabled: Boolean?): ApiResult<List<AdminUserListItem>> =
        safeCall { api.listUsers(role, enabled) }

    override suspend fun suspendUser(userId: Long): ApiResult<Unit> =
        safeCallUnit { api.suspendUser(userId) }

    override suspend fun reactivateUser(userId: Long): ApiResult<Unit> =
        safeCallUnit { api.reactivateUser(userId) }

    override suspend fun deleteUser(userId: Long): ApiResult<Unit> =
        safeCallUnit { api.deleteUser(userId) }

    override suspend fun getPendingRecruiters(): ApiResult<List<PendingRecruiterResponse>> =
        safeCall { api.getPendingRecruiters() }

    override suspend fun approveRecruiter(userId: Long): ApiResult<Unit> =
        safeCallUnit { api.approveRecruiter(userId) }

    override suspend fun rejectRecruiter(userId: Long, reason: String): ApiResult<Unit> =
        safeCallUnit { api.rejectRecruiter(userId, RejectUserRequest(reason)) }

    override suspend fun createUser(request: CreateUserAdminRequest): ApiResult<AdminUserListItem> =
        safeCall { api.createUser(request) }

    private suspend fun <T> safeCall(call: suspend () -> Response<T>): ApiResult<T> {
        return try {
            val response = call()
            val body = response.body()
            if (response.isSuccessful && body != null) {
                ApiResult.Success(body)
            } else {
                val errorBody = response.errorBody()?.string()
                val message = try {
                    gson.fromJson(errorBody, com.zyproo.devlink.data.remote.dto.ApiError::class.java)?.message
                        ?: "Error (${response.code()})"
                } catch (e: Exception) { "Error (${response.code()})" }
                ApiResult.Error(message)
            }
        } catch (e: IOException) {
            ApiResult.Error("Can't reach the server. Check your connection.")
        } catch (e: Exception) {
            ApiResult.Error(e.localizedMessage ?: "Something went wrong.")
        }
    }

    private suspend fun safeCallUnit(call: suspend () -> Response<Unit>): ApiResult<Unit> {
        return try {
            val response = call()
            if (response.isSuccessful) {
                ApiResult.Success(Unit)
            } else {
                val errorBody = response.errorBody()?.string()
                val message = try {
                    gson.fromJson(errorBody, com.zyproo.devlink.data.remote.dto.ApiError::class.java)?.message
                        ?: "Error (${response.code()})"
                } catch (e: Exception) { "Error (${response.code()})" }
                ApiResult.Error(message)
            }
        } catch (e: IOException) {
            ApiResult.Error("Can't reach the server. Check your connection.")
        } catch (e: Exception) {
            ApiResult.Error(e.localizedMessage ?: "Something went wrong.")
        }
    }
}
