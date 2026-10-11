package com.zyproo.devlink.data.remote

import com.zyproo.devlink.data.remote.dto.*
import retrofit2.Response
import retrofit2.http.*

interface TeamApi {
    @POST("api/teams")
    suspend fun createTeam(@Body request: CreateTeamRequest): Response<TeamResponse>

    @PUT("api/teams/{teamId}")
    suspend fun updateTeam(@Path("teamId") teamId: Long, @Body request: UpdateTeamRequest): Response<TeamResponse>

    @GET("api/teams/{teamId}")
    suspend fun getTeam(@Path("teamId") teamId: Long): Response<TeamResponse>

    @GET("api/teams/my")
    suspend fun getMyTeams(): Response<List<TeamResponse>>

    @POST("api/teams/{teamId}/invite")
    suspend fun inviteUser(@Path("teamId") teamId: Long, @Query("username") username: String): Response<TeamInviteResponse>

    @POST("api/teams/invites/{inviteId}/respond")
    suspend fun respondToInvite(@Path("inviteId") inviteId: Long, @Query("accept") accept: Boolean): Response<Map<String, String>>

    @GET("api/teams/invites/pending")
    suspend fun getPendingInvites(): Response<List<TeamInviteResponse>>
}
