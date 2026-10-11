package com.zyproo.devlink.data.repository

import android.net.Uri
import com.zyproo.devlink.common.ApiResult

interface FileRepository {
    suspend fun uploadFile(uri: Uri): ApiResult<String>
}
