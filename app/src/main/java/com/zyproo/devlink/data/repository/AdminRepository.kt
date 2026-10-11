package com.zyproo.devlink.data.repository

import com.zyproo.devlink.common.ApiResult
import com.zyproo.devlink.data.remote.dto.*

interface AdminRepository {
    suspend fun getDashboardStats(): ApiResult<AdminDashboardStats>
    suspend fun listUsers(role: String? = null, enabled: Boolean? = null): ApiResult<List<AdminUserListItem>>
    suspend fun suspendUser(userId: Long): ApiResult<Unit>
    suspend fun reactivateUser(userId: Long): ApiResult<Unit>
    suspend fun deleteUser(userId: Long): ApiResult<Unit>
    suspend fun getPendingRecruiters(): ApiResult<List<PendingRecruiterResponse>>
    suspend fun approveRecruiter(userId: Long): ApiResult<Unit>
    suspend fun rejectRecruiter(userId: Long, reason: String): ApiResult<Unit>
    suspend fun createUser(request: CreateUserAdminRequest): ApiResult<AdminUserListItem>
}
