package com.zyproo.devlink.data.remote.dto

enum class SkillStatus {
    PENDING,
    MEET,
    ACCEPTED,
    REJECTED
}

enum class SkillLevel {
    LOW,
    MID,
    HIGH
}

data class SkillResponse(
    val id: Long,
    val name: String
)

data class SkillRequestDto(
    val skillId: Long
)

data class StudentSkillResponse(
    val id: Long,
    val skillName: String,
    val level: SkillLevel,
    val pointsAwarded: Int,
    val approvedByTeacherName: String?,
    val approvedAt: String
)

data class SkillRequestResponse(
    val id: Long,
    val studentName: String,
    val studentUsername: String,
    val skillName: String,
    val teacherName: String,
    val status: SkillStatus,
    val meetingDate: String?,
    val meetingNotes: String?,
    val rejectionReason: String?,
    val createdAt: String,
    val studentPhotoUrl: String? = null
)

data class NotificationResponse(
    val id: Long,
    val title: String,
    val message: String,
    val read: Boolean,
    val createdAt: String
)

data class ApproveSkillRequest(
    val level: SkillLevel,
    val points: Int
)

data class ScheduleMeetingRequest(
    val meetingDate: String,
    val notes: String
)

data class RejectSkillRequest(
    val reason: String
)
