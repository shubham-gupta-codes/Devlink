package com.zyproo.devlink.data.repository

import com.zyproo.devlink.common.ApiResult
import com.zyproo.devlink.data.remote.dto.*

interface CertificateRepository {
    suspend fun uploadCertificate(request: UploadCertificateRequest): ApiResult<CertificateResponse>
    suspend fun getStudentCertificates(studentId: Long): ApiResult<List<CertificateResponse>>
}
