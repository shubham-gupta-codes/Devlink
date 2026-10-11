package com.zyproo.devlink.data.remote.dto

enum class JobType {
    INTERNSHIP,
    PART_TIME,
    FULL_TIME,
    CONTRACT
}

enum class JobStatus {
    OPEN,
    CLOSED
}

enum class JobApplicationStatus {
    PENDING,
    SHORTLISTED,
    REJECTED
}

data class JobResponse(
    val id: Long,
    val recruiterId: Long,
    val recruiterCompanyName: String,
    val recruiterCompanyLogo: String?,
    val title: String,
    val description: String,
    val jobType: JobType,
    val requiredSkills: List<String>,
    val location: String,
    val salaryRange: String?,
    val deadline: String,
    val startDate: String? = null,
    val status: JobStatus,
    val createdAt: String,
    val applicantCount: Long = 0,
    val hasApplied: Boolean = false
)

data class JobApplicationResponse(
    val id: Long,
    val jobId: Long,
    val jobTitle: String,
    val recruiterCompanyName: String,
    val studentProfile: ProfileResponse,
    val status: JobApplicationStatus,
    val createdAt: String
)

data class SavedCandidateResponse(
    val id: Long,
    val studentProfile: ProfileResponse,
    val jobId: Long?,
    val jobTitle: String?,
    val createdAt: String
)

data class CreateJobRequest(
    val title: String,
    val description: String,
    val jobType: JobType,
    val requiredSkills: List<String>,
    val location: String,
    val salaryRange: String? = null,
    val deadline: String,
    val startDate: String? = null
)

data class UpdateJobRequest(
    val title: String,
    val description: String,
    val jobType: JobType,
    val requiredSkills: List<String>,
    val location: String,
    val salaryRange: String? = null,
    val deadline: String,
    val startDate: String? = null,
    val status: JobStatus? = null
)

data class EvaluateApplicationRequest(
    val status: JobApplicationStatus
)
