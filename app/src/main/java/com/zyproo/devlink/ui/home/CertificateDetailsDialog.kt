package com.zyproo.devlink.ui.home

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.zyproo.devlink.common.toFullMediaUrl
import com.zyproo.devlink.data.remote.dto.*
import com.zyproo.devlink.ui.theme.DevLinkBlue

@Composable
fun CertificateDetailsDialog(
    certificate: CertificateResponse,
    onDismiss: () -> Unit
) {
    val uriHandler = LocalUriHandler.current

    AlertDialog(
        onDismissRequest = onDismiss,
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("Close", color = DevLinkBlue, fontWeight = FontWeight.Bold)
            }
        },
        title = { Text(certificate.title, fontWeight = FontWeight.Bold, fontSize = 16.sp) },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text("Issued By: ${certificate.issuingOrg}", fontWeight = FontWeight.SemiBold, fontSize = 13.sp)
                Text("Purpose: ${certificate.purpose}", fontSize = 13.sp, color = Color.DarkGray)
                Text("File Type: ${certificate.fileType.name}", fontSize = 12.sp, color = Color.Gray)
                Text(
                    text = "File URL: ${certificate.fileUrl}",
                    fontSize = 12.sp,
                    color = DevLinkBlue,
                    modifier = Modifier.clickable {
                        try {
                            val fullUrl = certificate.fileUrl.toFullMediaUrl() ?: certificate.fileUrl
                            uriHandler.openUri(fullUrl)
                        } catch (e: Exception) {}
                    }
                )
                Text("Verification Status: ${certificate.verificationStatus.name}", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                
                if (!certificate.teacherRemarks.isNullOrBlank()) {
                    Spacer(modifier = Modifier.height(4.dp))
                    Text("Teacher Remarks: ${certificate.teacherRemarks}", fontSize = 13.sp, color = Color.Red)
                }
            }
        }
    )
}
