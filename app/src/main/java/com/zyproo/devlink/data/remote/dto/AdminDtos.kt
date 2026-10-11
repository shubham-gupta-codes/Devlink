package com.zyproo.devlink.data.remote.dto

import java.time.Instant

data class AdminDashboardStats(
    val totalStudents: Long,
    val totalTeachers: Long,
    val totalRecruiters: Long,
    val totalProjects: Long,
    val totalVerifiedCertificates: Long,
    val pendingRecruiterApprovals: Long
)

data class AdminUserListItem(
    val userId: Long,
    val username: String,
    val email: String,
    val role: String,
    val enabled: Boolean,
    val createdAt: String,
    val displayName: String?,
    val department: String?,
    val companyName: String?,
    val verified: Boolean?
)

data class PendingRecruiterResponse(
    val userId: Long,
    val username: String,
    val email: String,
    val companyName: String,
    val designation: String?,
    val contact: String?,
    val website: String?,
    val joinedAt: String
)

data class RejectUserRequest(
    val reason: String
)

data class CreateUserAdminRequest(
    val username: String,
    val email: String,
    val password: String,
    val role: String,
    // Student fields
    val name: String? = null,
    val course: String? = null,
    val branch: String? = null,
    val admissionYear: Int? = null,
    val semester: Int? = null,
    // Teacher fields
    val employeeId: Long? = null,
    val department: String? = null,
    val designation: String? = null,
    // Recruiter fields
    val companyName: String? = null
)
