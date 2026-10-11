package com.zyproo.devlink.data.repository

import com.zyproo.devlink.data.remote.dto.*

interface TeamRepository {
    suspend fun createTeam(name: String, description: String?, photoUrl: String?): Result<TeamResponse>
    suspend fun updateTeam(teamId: Long, name: String, description: String?, photoUrl: String?): Result<TeamResponse>
    suspend fun getTeam(teamId: Long): Result<TeamResponse>
    suspend fun getMyTeams(): Result<List<TeamResponse>>
    suspend fun inviteUser(teamId: Long, username: String): Result<TeamInviteResponse>
    suspend fun respondToInvite(inviteId: Long, accept: Boolean): Result<Unit>
    suspend fun getPendingInvites(): Result<List<TeamInviteResponse>>
}
