package com.zyproo.devlink.data.remote

import com.zyproo.devlink.data.remote.dto.*
import retrofit2.Response
import retrofit2.http.*

interface CertificateApi {
    @POST("api/certificates")
    suspend fun uploadCertificate(@Body request: UploadCertificateRequest): Response<CertificateResponse>

    @GET("api/certificates/student/{studentId}")
    suspend fun getStudentCertificates(@Path("studentId") studentId: Long): Response<List<CertificateResponse>>
}
