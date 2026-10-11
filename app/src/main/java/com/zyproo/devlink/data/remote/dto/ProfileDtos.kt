package com.zyproo.devlink.data.remote.dto

enum class Course {
    BSC_IT,
    BTECH,
    MTECH,
    MCA,
    BCA,
    MSC
}

enum class Branch {
    IT,
    CS,
    CYBER_SECURITY,
    AI,
    DATA_SCIENCE
}

enum class AcademicStatus {
    ACTIVE,
    GRADUATED
}

enum class SocialPlatform {
    GITHUB,
    LINKEDIN,
    PORTFOLIO,
    LEETCODE,
    CODEFORCES,
    HACKERRANK,
    KAGGLE,
    YOUTUBE,
    INSTAGRAM,
    TWITTER,
    MEDIUM,
    DEVTO,
    BEHANCE,
    DRIBBBLE
}

data class ProfileResponse(
    val userId: Long,
    val role: String,
    val username: String,
    val email: String,

    // Student fields
    val name: String? = null,
    val photoUrl: String? = null,
    val course: Course? = null,
    val branch: Branch? = null,
    val admissionYear: Int? = null,
    val semester: Int? = null,
    val status: AcademicStatus? = null,
    val bio: String? = null,
    val totalPoints: Int? = null,
    val contact: String? = null,
    val socialLinks: List<SocialLinkResponse>? = null,
    val rank: Int? = null,
    val badges: List<String> = emptyList(),
    val latestFeedback: TeacherFeedbackResponse? = null,

    // Teacher fields
    val employeeId: Long? = null,
    val department: String? = null,
    val designation: String? = null,

    // Recruiter fields
    val companyName: String? = null,
    val companyLogo: String? = null,
    val about: String? = null,
    val website: String? = null,
    val verified: Boolean? = null
)

data class TeacherFeedbackResponse(
    val id: Long,
    val teacherUsername: String,
    val teacherName: String,
    val department: String,
    val content: String,
    val createdAt: String
)

data class SocialLinkResponse(
    val platform: SocialPlatform,
    val url: String
)

data class UpdateProfileRequest(
    val username: String? = null,
    val password: String? = null,
    val name: String? = null,
    val course: Course? = null,
    val branch: Branch? = null,
    val admissionYear: Int? = null,
    val semester: Int? = null,
    val bio: String? = null,
    val contact: String? = null,
    val photoUrl: String? = null,
    val employeeId: Long? = null,
    val department: String? = null,
    val designation: String? = null,
    val companyName: String? = null,
    val companyLogo: String? = null,
    val about: String? = null,
    val website: String? = null,
    val socialLinks: List<SocialLinkRequest>? = null
)

data class SocialLinkRequest(
    val platform: SocialPlatform,
    val url: String
)
