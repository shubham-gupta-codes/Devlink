package com.zyproo.devlink.data.repository

import com.zyproo.devlink.data.remote.dto.*

interface SkillRepository {
    suspend fun getAllSkills(): Result<List<SkillResponse>>
    suspend fun requestSkillVerification(skillId: Long): Result<SkillRequestResponse>
    suspend fun getStudentRequests(): Result<List<SkillRequestResponse>>
    suspend fun getStudentApprovedSkills(): Result<List<StudentSkillResponse>>
    suspend fun getStudentApprovedSkillsById(studentId: Long): Result<List<StudentSkillResponse>>
    suspend fun getTeacherPendingRequests(): Result<List<SkillRequestResponse>>
    suspend fun scheduleMeeting(requestId: Long, meetingDate: String, notes: String): Result<SkillRequestResponse>
    suspend fun approveRequest(requestId: Long, level: SkillLevel, points: Int): Result<SkillRequestResponse>
    suspend fun rejectRequest(requestId: Long, reason: String): Result<SkillRequestResponse>
}
