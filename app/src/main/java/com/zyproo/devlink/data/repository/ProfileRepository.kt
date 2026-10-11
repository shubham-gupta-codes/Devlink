package com.zyproo.devlink.data.repository

import com.zyproo.devlink.common.ApiResult
import com.zyproo.devlink.data.remote.dto.ProfileResponse
import com.zyproo.devlink.data.remote.dto.UpdateProfileRequest

import com.zyproo.devlink.data.remote.dto.Course
import com.zyproo.devlink.data.remote.dto.Branch

interface ProfileRepository {
    suspend fun getMyProfile(): ApiResult<ProfileResponse>
    suspend fun getProfileById(userId: Long): ApiResult<ProfileResponse>
    suspend fun getProfileByUsername(username: String): ApiResult<ProfileResponse>
    suspend fun updateProfile(request: UpdateProfileRequest): ApiResult<ProfileResponse>
    suspend fun getStudentRankings(course: Course?, branch: Branch?, admissionYear: Int?): ApiResult<List<ProfileResponse>>
    suspend fun searchStudents(
        query: String?,
        course: Course?,
        branch: Branch?,
        admissionYear: Int?,
        semester: Int?,
        skill: String?,
        badge: String?
    ): ApiResult<List<ProfileResponse>>
}
