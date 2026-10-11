package com.zyproo.devlink.data.repository

import com.google.gson.Gson
import com.zyproo.devlink.common.ApiResult
import com.zyproo.devlink.data.remote.ProfileApi
import com.zyproo.devlink.data.remote.dto.ApiError
import com.zyproo.devlink.data.remote.dto.ProfileResponse
import com.zyproo.devlink.data.remote.dto.UpdateProfileRequest
import retrofit2.Response
import java.io.IOException
import javax.inject.Inject

import com.zyproo.devlink.data.remote.dto.Course
import com.zyproo.devlink.data.remote.dto.Branch

class ProfileRepositoryImpl @Inject constructor(
    private val api: ProfileApi,
    private val gson: Gson
) : ProfileRepository {

    override suspend fun getMyProfile(): ApiResult<ProfileResponse> =
        safeCall { api.getMyProfile() }

    override suspend fun getProfileById(userId: Long): ApiResult<ProfileResponse> =
        safeCall { api.getProfileById(userId) }

    override suspend fun getProfileByUsername(username: String): ApiResult<ProfileResponse> =
        safeCall { api.getProfileByUsername(username) }

    override suspend fun updateProfile(request: UpdateProfileRequest): ApiResult<ProfileResponse> =
        safeCall { api.updateProfile(request) }

    override suspend fun getStudentRankings(course: Course?, branch: Branch?, admissionYear: Int?): ApiResult<List<ProfileResponse>> =
        safeCall { api.getStudentRankings(course?.name, branch?.name, admissionYear) }

    override suspend fun searchStudents(
        query: String?,
        course: Course?,
        branch: Branch?,
        admissionYear: Int?,
        semester: Int?,
        skill: String?,
        badge: String?
    ): ApiResult<List<ProfileResponse>> =
        safeCall {
            api.searchStudents(
                query = query,
                course = course?.name,
                branch = branch?.name,
                admissionYear = admissionYear,
                semester = semester,
                skill = skill,
                badge = badge
            )
        }

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
