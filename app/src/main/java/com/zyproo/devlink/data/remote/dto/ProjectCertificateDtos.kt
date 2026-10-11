package com.zyproo.devlink.data.remote.dto

enum class VerificationStatus {
    PENDING,
    APPROVED,
    REJECTED,
    NEED_TO_TALK
}

enum class EntityType {
    SKILL,
    PROJECT,
    CERTIFICATE
}

enum class FileType {
    PDF,
    IMAGE
}

enum class PointSource {
    VERIFIED_SKILL,
    VERIFIED_PROJECT,
    VERIFIED_CERTIFICATE
}

data class UploadProjectRequest(
    val name: String,
    val description: String,
    val techStack: String,
    val githubUrl: String?,
    val demoUrl: String?,
    val imageUrls: List<String>? = null
)

data class ProjectCommentRequest(
    val content: String,
    val parentCommentId: Long? = null
)

data class UploadCertificateRequest(
    val title: String,
    val issuingOrg: String,
    val purpose: String,
    val fileUrl: String,
    val fileType: FileType
)

data class EvaluateVerificationRequest(
    val status: VerificationStatus,
    val remarks: String? = null,
    val points: Int? = null,
    val scheduleAt: String? = null
)

data class ProjectResponse(
    val id: Long,
    val studentUserId: Long,
    val studentUsername: String,
    val studentName: String,
    val studentPhotoUrl: String? = null,
    val name: String,
    val description: String,
    val techStack: String,
    val githubUrl: String?,
    val demoUrl: String?,
    val verificationStatus: VerificationStatus,
    val likeCount: Long,
    val commentCount: Long,
    val createdAt: String,
    val imageUrls: List<String>,
    val likedByCurrentUser: Boolean = false
)

data class ProjectCommentResponse(
    val id: Long,
    val projectId: Long,
    val userId: Long,
    val username: String,
    val name: String,
    val photoUrl: String? = null,
    val content: String,
    val createdAt: String,
    val replies: List<ProjectCommentResponse> = emptyList()
)

data class CertificateResponse(
    val id: Long,
    val studentUserId: Long,
    val studentUsername: String,
    val studentName: String,
    val studentPhotoUrl: String? = null,
    val title: String,
    val issuingOrg: String,
    val purpose: String,
    val fileUrl: String,
    val fileType: FileType,
    val verificationStatus: VerificationStatus,
    val teacherRemarks: String? = null
)

data class VerificationRequestResponse(
    val id: Long,
    val entityType: EntityType,
    val entityId: Long,
    val entityName: String,
    val requestedByUserId: Long,
    val requestedByUsername: String,
    val requestedByName: String,
    val status: VerificationStatus,
    val reviewedByUserId: Long,
    val reviewedByUsername: String,
    val reviewedByName: String,
    val reviewedAt: String?,
    val remarks: String?,
    val scheduleAt: String?,
    val projectDetails: ProjectResponse? = null,
    val certificateDetails: CertificateResponse? = null
) {
    val createdAt: String
        get() = projectDetails?.createdAt ?: "2026-08-25T14:30:00Z"
}

data class TeacherAnalyticsResponse(
    val languageCounts: Map<String, Long>,
    val studentCountsByLanguage: List<StudentCountByLanguage>
)

data class StudentCountByLanguage(
    val language: String,
    val count: Long
)
