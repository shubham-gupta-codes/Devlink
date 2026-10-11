package com.zyproo.devlink.ui.home

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.MailOutline
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil3.compose.AsyncImage
import com.zyproo.devlink.common.toFullMediaUrl
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.ui.layout.ContentScale
import com.zyproo.devlink.data.remote.dto.*
import com.zyproo.devlink.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TeacherCertificateReviewList(
    queue: List<VerificationRequestResponse>,
    onEvaluate: (Long, VerificationStatus, String?, Int?, String?) -> Unit,
    onMessageStudent: (String) -> Unit = {},
    onViewStudentProfile: (String) -> Unit = {},
    rankings: List<ProfileResponse> = emptyList()
) {
    val uriHandler = LocalUriHandler.current
    var selectedRequest by remember { mutableStateOf<VerificationRequestResponse?>(null) }

    if (queue.isEmpty()) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color(0xFFF6F7FB)),
            contentAlignment = Alignment.Center
        ) {
            Text("No pending certificates to review.", color = Color.Gray)
        }
    } else {
        val grouped = queue.groupBy { it.requestedByName }
        
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(Color(0xFFF6F7FB))
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            grouped.forEach { (studentName, requests) ->
                Text(studentName, fontWeight = FontWeight.Bold, fontSize = 16.sp, color = DevLinkBlue)
                requests.forEach { req ->
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { selectedRequest = req }
                            .padding(vertical = 4.dp),
                        colors = CardDefaults.cardColors(containerColor = Color.White),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text(
                                text = req.entityName,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold,
                                color = DevLinkTextPrimary
                            )
                            req.certificateDetails?.let { details ->
                                Spacer(modifier = Modifier.height(4.dp))
                                Text("Issuer: ${details.issuingOrg}", fontSize = 12.sp, color = Color.Gray)
                                Spacer(modifier = Modifier.height(2.dp))
                                Text("Purpose: ${details.purpose}", fontSize = 13.sp, color = Color.DarkGray)
                            }
                        }
                    }
                }
                Spacer(modifier = Modifier.height(8.dp))
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
            onDismissRequest = { selectedRequest = null },
            confirmButton = {
                Button(
                    onClick = {
                        onMessageStudent(req.requestedByUsername)
                        selectedRequest = null
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
                        onClick = { selectedRequest = null },
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
                        }
                    )
                }
            },
            containerColor = Color.White,
            text = {
                Column(
                    modifier = Modifier.fillMaxWidth().verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    // Student Context Row (Avatar + Name + Timestamp)
                    val studentProfile = rankings.find { it.username == req.requestedByUsername }
                    val photoUrl = req.certificateDetails?.studentPhotoUrl?.toFullMediaUrl() ?: studentProfile?.photoUrl?.toFullMediaUrl()
                    val studentName = req.requestedByName
                    val dateFormatted = req.createdAt.take(16).replace("T", " ")

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

                    HorizontalDivider(color = DevLinkBorder)

                    // Certificate preview
                    req.certificateDetails?.let { details ->
                        Text("Certificate Preview:", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = DevLinkTextPrimary)
                        if (details.fileType == FileType.IMAGE) {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(200.dp)
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(Color(0xFFF1F5F9)),
                                contentAlignment = Alignment.Center
                            ) {
                                AsyncImage(
                                    model = details.fileUrl.toFullMediaUrl() ?: details.fileUrl,
                                    contentDescription = "Certificate Preview",
                                    modifier = Modifier.fillMaxSize(),
                                    contentScale = androidx.compose.ui.layout.ContentScale.Fit
                                )
                            }
                        } else {
                            Surface(
                                color = Color(0xFFEFF6FF),
                                shape = RoundedCornerShape(8.dp),
                                border = BorderStroke(1.dp, Color(0xFFBFDBFE)),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable {
                                        try {
                                            val fullUrl = details.fileUrl.toFullMediaUrl() ?: details.fileUrl
                                            uriHandler.openUri(fullUrl)
                                        } catch (e: Exception) {}
                                    }
                            ) {
                                Row(
                                    modifier = Modifier.padding(12.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Description,
                                        contentDescription = null,
                                        tint = DevLinkBlue,
                                        modifier = Modifier.size(20.dp)
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = "Open PDF Document",
                                        color = DevLinkBlue,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 13.sp
                                    )
                                }
                            }
                        }
                        
                        Spacer(modifier = Modifier.height(4.dp))
                        Text("Title: ${details.title}", fontSize = 13.sp, fontWeight = FontWeight.Medium, color = DevLinkTextPrimary)
                        Text("Issuer: ${details.issuingOrg}", fontSize = 13.sp, color = DevLinkTextSecondary)
                        Text("Purpose: ${details.purpose}", fontSize = 13.sp, color = DevLinkTextSecondary)
                        HorizontalDivider(color = DevLinkBorder)
                    }

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
