package com.zyproo.devlink.data.remote

import com.zyproo.devlink.data.remote.dto.*
import retrofit2.Response
import retrofit2.http.*

interface AdminApi {

    @GET("api/admin/stats")
    suspend fun getDashboardStats(): Response<AdminDashboardStats>

    @GET("api/admin/users")
    suspend fun listUsers(
        @Query("role") role: String? = null,
        @Query("enabled") enabled: Boolean? = null
    ): Response<List<AdminUserListItem>>

    @PATCH("api/admin/users/{id}/suspend")
    suspend fun suspendUser(@Path("id") id: Long): Response<Unit>

    @PATCH("api/admin/users/{id}/reactivate")
    suspend fun reactivateUser(@Path("id") id: Long): Response<Unit>

    @DELETE("api/admin/users/{id}")
    suspend fun deleteUser(@Path("id") id: Long): Response<Unit>

    @GET("api/admin/approvals/recruiters")
    suspend fun getPendingRecruiters(): Response<List<PendingRecruiterResponse>>

    @PATCH("api/admin/approvals/recruiters/{id}/approve")
    suspend fun approveRecruiter(@Path("id") id: Long): Response<Unit>

    @PATCH("api/admin/approvals/recruiters/{id}/reject")
    suspend fun rejectRecruiter(
        @Path("id") id: Long,
        @Body body: RejectUserRequest
    ): Response<Unit>

    @POST("api/admin/user")
    suspend fun createUser(@Body request: CreateUserAdminRequest): Response<AdminUserListItem>
}
