package com.zyproo.devlink.data.remote

import com.zyproo.devlink.data.remote.dto.ProfileResponse
import com.zyproo.devlink.data.remote.dto.UpdateProfileRequest
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.PUT
import retrofit2.http.Path
import retrofit2.http.Query

interface ProfileApi {
    @GET("api/user/profile")
    suspend fun getMyProfile(): Response<ProfileResponse>

    @GET("api/user/profile/{userId}")
    suspend fun getProfileById(@Path("userId") userId: Long): Response<ProfileResponse>

    @GET("api/user/profile/username/{username}")
    suspend fun getProfileByUsername(@Path("username") username: String): Response<ProfileResponse>

    @PUT("api/user/profile")
    suspend fun updateProfile(@Body request: UpdateProfileRequest): Response<ProfileResponse>

    @GET("api/user/profile/rankings")
    suspend fun getStudentRankings(
        @Query("course") course: String? = null,
        @Query("branch") branch: String? = null,
        @Query("admissionYear") admissionYear: Int? = null
    ): Response<List<ProfileResponse>>

    @GET("api/user/profile/search")
    suspend fun searchStudents(
        @Query("query") query: String? = null,
        @Query("course") course: String? = null,
        @Query("branch") branch: String? = null,
        @Query("admissionYear") admissionYear: Int? = null,
        @Query("semester") semester: Int? = null,
        @Query("skill") skill: String? = null,
        @Query("badge") badge: String? = null
    ): Response<List<ProfileResponse>>
}
