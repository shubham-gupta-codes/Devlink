package com.zyproo.devlink.data.repository

import android.content.Context
import android.net.Uri
import com.google.gson.Gson
import com.zyproo.devlink.common.ApiResult
import com.zyproo.devlink.common.toFullMediaUrl
import com.zyproo.devlink.data.remote.FileApi
import com.zyproo.devlink.data.remote.dto.ApiError
import dagger.hilt.android.qualifiers.ApplicationContext
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.toRequestBody
import java.io.InputStream
import javax.inject.Inject

class FileRepositoryImpl @Inject constructor(
    @ApplicationContext private val context: Context,
    private val fileApi: FileApi,
    private val gson: Gson
) : FileRepository {

    override suspend fun uploadFile(uri: Uri): ApiResult<String> {
        return try {
            val contentResolver = context.contentResolver
            val inputStream: InputStream = contentResolver.openInputStream(uri)
                ?: return ApiResult.Error("Unable to open selected file")

            val bytes = inputStream.readBytes()
            inputStream.close()

            val mimeType = contentResolver.getType(uri) ?: "image/jpeg"
            val ext = when {
                mimeType.contains("png", ignoreCase = true) -> "png"
                mimeType.contains("webp", ignoreCase = true) -> "webp"
                mimeType.contains("pdf", ignoreCase = true) -> "pdf"
                mimeType.contains("gif", ignoreCase = true) -> "gif"
                else -> "jpg"
            }
            val requestBody = bytes.toRequestBody(mimeType.toMediaTypeOrNull())
            val part = MultipartBody.Part.createFormData("file", "upload_${System.currentTimeMillis()}.$ext", requestBody)

            val response = fileApi.uploadFile(part)
            val body = response.body()

            if (response.isSuccessful && body != null) {
                val fullUrl = body.url.toFullMediaUrl() ?: body.url
                ApiResult.Success(fullUrl)
            } else {
                val errorMsg = parseErrorMessage(response)
                ApiResult.Error(errorMsg)
            }
        } catch (e: Exception) {
            ApiResult.Error(e.localizedMessage ?: "File upload failed")
        }
    }

    private fun parseErrorMessage(response: retrofit2.Response<*>): String {
        val errorBody = response.errorBody()?.string()
        return try {
            val apiError = gson.fromJson(errorBody, ApiError::class.java)
            apiError?.message ?: "Upload failed (HTTP ${response.code()})"
        } catch (e: Exception) {
            "Upload failed (HTTP ${response.code()})"
        }
    }
}
