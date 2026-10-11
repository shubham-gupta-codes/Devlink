package com.zyproo.devlink.data.repository

import com.zyproo.devlink.data.remote.SkillApi
import com.zyproo.devlink.data.remote.dto.*
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SkillRepositoryImpl @Inject constructor(
    private val skillApi: SkillApi
) : SkillRepository {

    override suspend fun getAllSkills(): Result<List<SkillResponse>> = runCatching {
        skillApi.getAllSkills()
    }

    override suspend fun requestSkillVerification(skillId: Long): Result<SkillRequestResponse> = runCatching {
        skillApi.requestSkillVerification(SkillRequestDto(skillId))
    }

    override suspend fun getStudentRequests(): Result<List<SkillRequestResponse>> = runCatching {
        skillApi.getStudentRequests()
    }

    override suspend fun getStudentApprovedSkills(): Result<List<StudentSkillResponse>> = runCatching {
        skillApi.getStudentApprovedSkills()
    }

    override suspend fun getStudentApprovedSkillsById(studentId: Long): Result<List<StudentSkillResponse>> = runCatching {
        skillApi.getStudentApprovedSkillsById(studentId)
    }

    override suspend fun getTeacherPendingRequests(): Result<List<SkillRequestResponse>> = runCatching {
        skillApi.getTeacherPendingRequests()
    }

    override suspend fun scheduleMeeting(
        requestId: Long,
        meetingDate: String,
        notes: String
    ): Result<SkillRequestResponse> = runCatching {
        skillApi.scheduleMeeting(requestId, ScheduleMeetingRequest(meetingDate, notes))
    }

    override suspend fun approveRequest(
        requestId: Long,
        level: SkillLevel,
        points: Int
    ): Result<SkillRequestResponse> = runCatching {
        skillApi.approveRequest(requestId, ApproveSkillRequest(level, points))
    }

    override suspend fun rejectRequest(
        requestId: Long,
        reason: String
    ): Result<SkillRequestResponse> = runCatching {
        skillApi.rejectRequest(requestId, RejectSkillRequest(reason))
    }
}
