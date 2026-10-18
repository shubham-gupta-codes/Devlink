package com.zyproo.devlink.ui.home

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.OpenInNew
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil3.compose.AsyncImage
import com.zyproo.devlink.common.toFullMediaUrl
import com.zyproo.devlink.common.formatDisplayDateTime
import com.zyproo.devlink.common.formatMeetingDateTime
import com.zyproo.devlink.data.remote.dto.*
import com.zyproo.devlink.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TeacherCertificateReviewList(
    queue: List<VerificationRequestResponse>,
    initialSelectedRequestId: Long? = null,
    onEvaluate: (Long, VerificationStatus, String?, Int?, String?) -> Unit,
    onMessageStudent: (String) -> Unit = {},
    onViewStudentProfile: (String) -> Unit = {},
    onClearSelectedRequest: () -> Unit = {},
    rankings: List<ProfileResponse> = emptyList()
) {
    val context = LocalContext.current
    val uriHandler = LocalUriHandler.current
    var selectedRequest by remember(initialSelectedRequestId) {
        mutableStateOf(queue.find { it.id == initialSelectedRequestId })
    }

    if (queue.isEmpty()) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color(0xFFF6F7FB)),
            contentAlignment = Alignment.Center
        ) {
            Text("No pending certificates to review.", color = Color.Gray, fontSize = 14.sp)
        }
    } else {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .background(Color(0xFFF6F7FB))
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(queue) { req ->
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { selectedRequest = req },
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    shape = RoundedCornerShape(12.dp),
                    border = BorderStroke(1.dp, DevLinkBorder),
                    elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                val studentProfile = rankings.find { it.username == req.requestedByUsername }
                                val photoUrl = req.certificateDetails?.studentPhotoUrl?.toFullMediaUrl() ?: studentProfile?.photoUrl?.toFullMediaUrl()
                                Box(
                                    modifier = Modifier
                                        .size(38.dp)
                                        .clip(CircleShape)
                                        .background(DevLinkBlueContainer),
                                    contentAlignment = Alignment.Center
                                ) {
                                    if (!photoUrl.isNullOrBlank()) {
                                        AsyncImage(
                                            model = photoUrl,
                                            contentDescription = "Avatar",
                                            modifier = Modifier.fillMaxSize(),
                                            contentScale = ContentScale.Crop
                                        )
                                    } else {
                                        Text(
                                            text = req.requestedByName.take(2).uppercase(),
                                            fontWeight = FontWeight.Bold,
                                            color = DevLinkBlue,
                                            fontSize = 13.sp
                                        )
                                    }
                                }
                                Column {
                                    Text(
                                        text = req.requestedByName,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 14.sp,
                                        color = DevLinkTextPrimary
                                    )
                                    Text(
                                        text = "@${req.requestedByUsername}",
                                        fontSize = 11.sp,
                                        color = DevLinkTextSecondary
                                    )
                                }
                            }

                            val isPdf = req.certificateDetails?.fileType == FileType.PDF ||
                                    (req.certificateDetails?.fileUrl?.endsWith(".pdf", ignoreCase = true) == true)
                            val typeLabel = if (isPdf) "PDF Document" else "Certificate Image"
                            val badgeBg = if (isPdf) Color(0xFFFEF2F2) else Color(0xFFDCFCE7)
                            val badgeColor = if (isPdf) Color(0xFFDC2626) else Color(0xFF16A34A)
                            Surface(
                                color = badgeBg,
                                shape = RoundedCornerShape(6.dp)
                            ) {
                                Text(
                                    text = typeLabel,
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = badgeColor,
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        val certTitle = req.certificateDetails?.title?.ifBlank { null } ?: req.entityName
                        Text(
                            text = certTitle,
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold,
                            color = DevLinkTextPrimary
                        )

                        req.certificateDetails?.let { details ->
                            Spacer(modifier = Modifier.height(4.dp))
                            Text("Issuer: ${details.issuingOrg}", fontSize = 12.sp, color = DevLinkTextSecondary)
                            if (details.purpose.isNotBlank()) {
                                Spacer(modifier = Modifier.height(2.dp))
                                Text("Purpose: ${details.purpose}", fontSize = 12.sp, color = DevLinkTextSecondary)
                            }
                        }

                        if (req.status == VerificationStatus.NEED_TO_TALK && !req.scheduleAt.isNullOrBlank()) {
                            Spacer(modifier = Modifier.height(6.dp))
                            Surface(
                                color = Color(0xFFEFF6FF),
                                shape = RoundedCornerShape(6.dp),
                                border = BorderStroke(1.dp, Color(0xFFBFDBFE))
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Event,
                                        contentDescription = null,
                                        tint = Color(0xFF2563EB),
                                        modifier = Modifier.size(13.dp)
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = "Discussion: ${formatMeetingDateTime(req.scheduleAt)}",
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Medium,
                                        color = Color(0xFF1E40AF)
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            val dateFormatted = formatDisplayDateTime(req.createdAt)
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.AccessTime,
                                    contentDescription = null,
                                    tint = DevLinkTextSecondary,
                                    modifier = Modifier.size(12.dp)
                                )
                                Text(
                                    text = "Uploaded: $dateFormatted",
                                    fontSize = 11.sp,
                                    color = DevLinkTextSecondary
                                )
                            }

                            Text(
                                text = "Tap to Review →",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = DevLinkBlue
                            )
                        }
                    }
                }
            }
        }
    }

    selectedRequest?.let { req ->
        var action by remember { mutableStateOf("Approved") }
        var pointsVal by remember { mutableIntStateOf(10) }
        var pointsStr by remember { mutableStateOf("10") }
        var remarks by remember { mutableStateOf("") }
        var scheduleAtStr by remember { mutableStateOf("2026-08-10T10:00:00Z") }

        AlertDialog(
            onDismissRequest = {
                selectedRequest = null
                onClearSelectedRequest()
            },
            confirmButton = {
                Button(
                    onClick = {
                        onMessageStudent(req.requestedByUsername)
                        selectedRequest = null
                        onClearSelectedRequest()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = DevLinkBlue),
                    shape = RoundedCornerShape(8.dp),
                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 8.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.MailOutline,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Message Student", fontSize = 13.sp, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    OutlinedButton(
                        onClick = {
                            selectedRequest = null
                            onClearSelectedRequest()
                        },
                        shape = RoundedCornerShape(8.dp),
                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 8.dp)
                    ) {
                        Text("Cancel", color = DevLinkTextSecondary, fontSize = 13.sp)
                    }

                    Button(
                        onClick = {
                            val status = when (action) {
                                "Approved" -> VerificationStatus.APPROVED
                                "Rejected" -> VerificationStatus.REJECTED
                                else -> VerificationStatus.NEED_TO_TALK
                            }
                            onEvaluate(
                                req.id,
                                status,
                                remarks.ifBlank { null },
                                if (status == VerificationStatus.APPROVED) pointsVal else null,
                                if (status == VerificationStatus.NEED_TO_TALK) scheduleAtStr else null
                            )
                            selectedRequest = null
                            onClearSelectedRequest()
                        },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (action == "Rejected") Color(0xFFDC2626) else DevLinkBlue
                        ),
                        shape = RoundedCornerShape(8.dp),
                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 8.dp)
                    ) {
                        Text("Submit", fontSize = 13.sp, fontWeight = FontWeight.Bold)
                    }
                }
            },
            title = {
                Column {
                    Text("Review Certificate", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = DevLinkTextPrimary)
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = "Student: @${req.requestedByUsername} (View Profile)",
                        fontSize = 12.sp,
                        color = DevLinkBlue,
                        fontWeight = FontWeight.SemiBold,
                        modifier = Modifier.clickable {
                            onViewStudentProfile(req.requestedByUsername)
                            selectedRequest = null
                            onClearSelectedRequest()
                        }
                    )
                }
            },
            containerColor = Color.White,
            text = {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(max = 520.dp)
                        .verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    // 1. Student Context Row (Avatar + Name + Timestamp)
                    val studentProfile = rankings.find { it.username == req.requestedByUsername }
                    val photoUrl = req.certificateDetails?.studentPhotoUrl?.toFullMediaUrl() ?: studentProfile?.photoUrl?.toFullMediaUrl()
                    val studentName = req.requestedByName
                    val dateFormatted = formatDisplayDateTime(req.createdAt)

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(Color(0xFFF8FAFC), RoundedCornerShape(8.dp))
                            .padding(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .clip(CircleShape)
                                .background(DevLinkBlueContainer),
                            contentAlignment = Alignment.Center
                        ) {
                            if (!photoUrl.isNullOrBlank()) {
                                AsyncImage(
                                    model = photoUrl,
                                    contentDescription = "Avatar",
                                    contentScale = ContentScale.Crop,
                                    modifier = Modifier.fillMaxSize()
                                )
                            } else {
                                Text(
                                    text = studentName.take(2).uppercase(),
                                    fontWeight = FontWeight.Bold,
                                    color = DevLinkBlue,
                                    fontSize = 14.sp
                                )
                            }
                        }

                        Spacer(modifier = Modifier.width(12.dp))

                        Column {
                            Text(
                                text = studentName,
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.sp,
                                color = DevLinkTextPrimary
                            )
                            Text(
                                text = "Submitted on $dateFormatted",
                                fontSize = 11.sp,
                                color = DevLinkTextSecondary
                            )
                        }
                    }

                    // 2. Certificate Details Section
                    req.certificateDetails?.let { details ->
                        Surface(
                            color = Color(0xFFF8FAFC),
                            shape = RoundedCornerShape(10.dp),
                            border = BorderStroke(1.dp, DevLinkBorder),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(
                                modifier = Modifier.padding(12.dp),
                                verticalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Text(
                                    text = details.title.ifBlank { req.entityName },
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = DevLinkTextPrimary
                                )

                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    Surface(
                                        color = DevLinkBlueContainer,
                                        shape = RoundedCornerShape(6.dp)
                                    ) {
                                        Text(
                                            text = "Issuer: ${details.issuingOrg}",
                                            fontSize = 12.sp,
                                            fontWeight = FontWeight.SemiBold,
                                            color = DevLinkBlue,
                                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                                        )
                                    }
                                }

                                if (details.purpose.isNotBlank()) {
                                    Text(
                                        text = details.purpose,
                                        fontSize = 12.sp,
                                        color = DevLinkTextSecondary,
                                        lineHeight = 18.sp
                                    )
                                }

                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.AccessTime,
                                        contentDescription = null,
                                        tint = DevLinkTextSecondary,
                                        modifier = Modifier.size(13.dp)
                                    )
                                    Text(
                                        text = "Uploaded on: $dateFormatted",
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Medium,
                                        color = DevLinkTextSecondary
                                    )
                                }
                            }
                        }

                        // 3. Document / Image Preview Section
                        val rawMediaUrl = details.fileUrl.toFullMediaUrl() ?: details.fileUrl
                        val cleanTitle = (details.title.ifBlank { req.entityName }).replace(Regex("[^a-zA-Z0-9_-]"), "_")
                        val viewUrl = if (rawMediaUrl.contains("?")) "$rawMediaUrl&name=$cleanTitle" else "$rawMediaUrl?name=$cleanTitle"
                        val isImage = details.fileType == FileType.IMAGE ||
                                details.fileUrl.endsWith(".png", ignoreCase = true) ||
                                details.fileUrl.endsWith(".jpg", ignoreCase = true) ||
                                details.fileUrl.endsWith(".jpeg", ignoreCase = true) ||
                                details.fileUrl.endsWith(".webp", ignoreCase = true)

                        if (isImage) {
                            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                                Text(
                                    text = "Certificate Preview:",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = DevLinkTextSecondary
                                )
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(210.dp)
                                        .clip(RoundedCornerShape(10.dp))
                                        .background(Color(0xFFE2E8F0))
                                        .clickable {
                                            try { uriHandler.openUri(viewUrl) } catch (e: Exception) {}
                                        },
                                    contentAlignment = Alignment.Center
                                ) {
                                    AsyncImage(
                                        model = viewUrl,
                                        contentDescription = "Certificate Preview",
                                        modifier = Modifier.fillMaxSize(),
                                        contentScale = ContentScale.Fit
                                    )
                                }
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.End
                                ) {
                                    Text(
                                        text = "Tap image to view in full resolution ↗",
                                        fontSize = 11.sp,
                                        color = DevLinkBlue,
                                        fontWeight = FontWeight.SemiBold,
                                        modifier = Modifier.clickable {
                                            try { uriHandler.openUri(viewUrl) } catch (e: Exception) {}
                                        }
                                    )
                                }
                            }
                        } else {
                            // PDF Document Card
                            Surface(
                                color = Color(0xFFFEF2F2),
                                shape = RoundedCornerShape(10.dp),
                                border = BorderStroke(1.dp, Color(0xFFFECACA)),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Column(modifier = Modifier.padding(14.dp)) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                                    ) {
                                        Surface(
                                            color = Color(0xFFDC2626),
                                            shape = RoundedCornerShape(8.dp),
                                            modifier = Modifier.size(44.dp)
                                        ) {
                                            Box(contentAlignment = Alignment.Center) {
                                                Icon(
                                                    imageVector = Icons.Default.Description,
                                                    contentDescription = "PDF Document",
                                                    tint = Color.White,
                                                    modifier = Modifier.size(24.dp)
                                                )
                                            }
                                        }
                                        Column(modifier = Modifier.weight(1f)) {
                                            Text(
                                                text = "${cleanTitle}.pdf",
                                                fontWeight = FontWeight.Bold,
                                                fontSize = 13.sp,
                                                color = DevLinkTextPrimary,
                                                maxLines = 1
                                            )
                                            Text(
                                                text = "PDF Certificate Document",
                                                fontSize = 11.sp,
                                                color = Color(0xFF991B1B)
                                            )
                                        }
                                    }

                                    Spacer(modifier = Modifier.height(12.dp))

                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                                    ) {
                                        // Button 1: Open with PDF Viewer Intent
                                        Button(
                                            onClick = {
                                                val intent = Intent(Intent.ACTION_VIEW).apply {
                                                    setDataAndType(Uri.parse(viewUrl), "application/pdf")
                                                    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                                                }
                                                try {
                                                    context.startActivity(Intent.createChooser(intent, "Open Certificate PDF with..."))
                                                } catch (e: Exception) {
                                                    try { uriHandler.openUri(viewUrl) } catch (e2: Exception) {}
                                                }
                                            },
                                            modifier = Modifier.weight(1f),
                                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFDC2626)),
                                            shape = RoundedCornerShape(8.dp),
                                            contentPadding = PaddingValues(horizontal = 8.dp, vertical = 8.dp)
                                        ) {
                                            Icon(
                                                imageVector = Icons.AutoMirrored.Filled.OpenInNew,
                                                contentDescription = null,
                                                modifier = Modifier.size(14.dp)
                                            )
                                            Spacer(modifier = Modifier.width(4.dp))
                                            Text("Open PDF", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                        }

                                        // Button 2: Browser Link with human-readable name
                                        OutlinedButton(
                                            onClick = {
                                                try { uriHandler.openUri(viewUrl) } catch (e: Exception) {}
                                            },
                                            modifier = Modifier.weight(1f),
                                            shape = RoundedCornerShape(8.dp),
                                            contentPadding = PaddingValues(horizontal = 8.dp, vertical = 8.dp)
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.Link,
                                                contentDescription = null,
                                                tint = DevLinkTextPrimary,
                                                modifier = Modifier.size(14.dp)
                                            )
                                            Spacer(modifier = Modifier.width(4.dp))
                                            Text("Browser Link", fontSize = 12.sp, color = DevLinkTextPrimary, fontWeight = FontWeight.SemiBold)
                                        }
                                    }
                                }
                            }
                        }
                    }

                    HorizontalDivider(color = DevLinkBorder)

                    // 4. Evaluation Decision Form
                    Text("Evaluation Decision", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = DevLinkTextPrimary)

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        listOf("Approved", "Rejected", "Request More Info").forEach { act ->
                            val isSelected = (act == "Approved" && action == "Approved") ||
                                             (act == "Rejected" && action == "Rejected") ||
                                             (act == "Request More Info" && action == "Request More Info")
                            Box(
                                modifier = Modifier
                                    .weight(1.5f)
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(if (isSelected) DevLinkBlue else Color(0xFFF1F1F1))
                                    .clickable { action = act }
                                    .padding(vertical = 8.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = act,
                                    fontSize = 10.sp,
                                    color = if (isSelected) Color.White else Color.Black,
                                    fontWeight = FontWeight.SemiBold
                                )
                            }
                        }
                    }

                    OutlinedTextField(
                        value = remarks,
                        onValueChange = { remarks = it },
                        label = { Text("Remarks / Feedback") },
                        modifier = Modifier.fillMaxWidth()
                    )

                    if (action == "Approved") {
                        OutlinedTextField(
                            value = pointsStr,
                            onValueChange = { newVal ->
                                if (newVal.all { it.isDigit() } && newVal.length <= 4) {
                                    pointsStr = newVal
                                    pointsVal = newVal.toIntOrNull() ?: 0
                                }
                            },
                            label = { Text("Award Points") },
                            placeholder = { Text("Enter points (e.g. 50)") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }

                    if (action == "Request More Info") {
                        com.zyproo.devlink.ui.common.MeetingSchedulePicker(
                            initialIsoString = scheduleAtStr,
                            onIsoStringChange = { scheduleAtStr = it }
                        )
                    }
                }
            }
        )
    }
}
