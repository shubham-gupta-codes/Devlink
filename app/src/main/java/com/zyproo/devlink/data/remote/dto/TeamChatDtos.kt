package com.zyproo.devlink.data.remote.dto

// Teams
data class CreateTeamRequest(
    val name: String,
    val description: String?,
    val photoUrl: String?
)

data class UpdateTeamRequest(
    val name: String,
    val description: String?,
    val photoUrl: String?
)

data class TeamResponse(
    val id: Long,
    val name: String,
    val description: String?,
    val photoUrl: String?,
    val createdByUserId: Long,
    val createdByUsername: String,
    val createdAt: String,
    val members: List<TeamMemberResponse>
)

data class TeamMemberResponse(
    val userId: Long,
    val username: String,
    val name: String?,
    val role: String, // ADMIN, MEMBER
    val joinedAt: String
)

data class TeamInviteResponse(
    val id: Long,
    val teamId: Long,
    val teamName: String,
    val invitedUserId: Long,
    val invitedUsername: String,
    val invitedByUserId: Long,
    val invitedByUsername: String,
    val status: String,
    val createdAt: String
)

// Chats
data class ConversationResponse(
    val id: Long,
    val type: String, // DIRECT, GROUP
    val teamId: Long?,
    val name: String,
    val photoUrl: String?,
    val lastMessagePreview: String?,
    val lastMessageAt: String?,
    val participants: List<ParticipantResponse>
)

data class ParticipantResponse(
    val userId: Long,
    val username: String,
    val name: String?
)

data class MessageResponse(
    val id: Long,
    val conversationId: Long,
    val senderId: Long,
    val senderUsername: String,
    val content: String,
    val createdAt: String
)
