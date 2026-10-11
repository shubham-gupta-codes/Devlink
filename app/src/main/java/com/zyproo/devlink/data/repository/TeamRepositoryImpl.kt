package com.zyproo.devlink.data.repository

import com.zyproo.devlink.data.remote.TeamApi
import com.zyproo.devlink.data.remote.dto.*
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class TeamRepositoryImpl @Inject constructor(
    private val teamApi: TeamApi
) : TeamRepository {

    override suspend fun createTeam(name: String, description: String?, photoUrl: String?): Result<TeamResponse> = runCatching {
        val res = teamApi.createTeam(CreateTeamRequest(name, description, photoUrl))
        if (res.isSuccessful) res.body() ?: throw Exception("Body is null")
        else throw Exception("Failed to create team: ${res.message()}")
    }

    override suspend fun updateTeam(teamId: Long, name: String, description: String?, photoUrl: String?): Result<TeamResponse> = runCatching {
        val res = teamApi.updateTeam(teamId, UpdateTeamRequest(name, description, photoUrl))
        if (res.isSuccessful) res.body() ?: throw Exception("Body is null")
        else throw Exception("Failed to update team: ${res.message()}")
    }

    override suspend fun getTeam(teamId: Long): Result<TeamResponse> = runCatching {
        val res = teamApi.getTeam(teamId)
        if (res.isSuccessful) res.body() ?: throw Exception("Body is null")
        else throw Exception("Failed to get team: ${res.message()}")
    }

    override suspend fun getMyTeams(): Result<List<TeamResponse>> = runCatching {
        val res = teamApi.getMyTeams()
        if (res.isSuccessful) res.body() ?: emptyList()
        else throw Exception("Failed to get my teams: ${res.message()}")
    }

    override suspend fun inviteUser(teamId: Long, username: String): Result<TeamInviteResponse> = runCatching {
        val res = teamApi.inviteUser(teamId, username)
        if (res.isSuccessful) res.body() ?: throw Exception("Body is null")
        else throw Exception("Failed to invite: ${res.message()}")
    }

    override suspend fun respondToInvite(inviteId: Long, accept: Boolean): Result<Unit> = runCatching {
        val res = teamApi.respondToInvite(inviteId, accept)
        if (res.isSuccessful) Unit
        else throw Exception("Failed to respond to invite: ${res.message()}")
    }

    override suspend fun getPendingInvites(): Result<List<TeamInviteResponse>> = runCatching {
        val res = teamApi.getPendingInvites()
        if (res.isSuccessful) res.body() ?: emptyList()
        else throw Exception("Failed to fetch pending invites: ${res.message()}")
    }
}
