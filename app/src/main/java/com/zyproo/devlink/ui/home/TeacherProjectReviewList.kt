package com.zyproo.devlink.ui.home

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccessTime
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.MailOutline
import androidx.compose.material3.*
import coil3.compose.AsyncImage
import com.zyproo.devlink.common.toFullMediaUrl
import androidx.compose.ui.layout.ContentScale
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.zyproo.devlink.data.remote.dto.*
import com.zyproo.devlink.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TeacherProjectReviewList(
    queue: List<VerificationRequestResponse>,
    initialSelectedRequestId: Long? = null,
    onEvaluate: (Long, VerificationStatus, String?, Int?, String?) -> Unit,
    onMessageStudent: (String) -> Unit = {},
    onViewStudentProfile: (String) -> Unit = {},
    onClearSelectedRequest: () -> Unit = {},
    rankings: List<ProfileResponse> = emptyList()
) {
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
            Text("No pending projects to review.", color = Color.Gray, fontSize = 14.sp)
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
                                val thumb = req.projectDetails?.imageUrls?.firstOrNull()?.toFullMediaUrl()
                                Surface(
                                    modifier = Modifier.size(38.dp),
                                    shape = RoundedCornerShape(8.dp),
                                    color = DevLinkBlueContainer
                                ) {
                                    if (!thumb.isNullOrBlank()) {
                                        AsyncImage(
                                            model = thumb,
                                            contentDescription = null,
                                            modifier = Modifier.fillMaxSize(),
                                            contentScale = ContentScale.Crop
                                        )
                                    } else {
                                        Box(contentAlignment = Alignment.Center) {
                                            Icon(
                                                imageVector = Icons.Default.Folder,
                                                contentDescription = null,
                                                tint = DevLinkBlue,
                                                modifier = Modifier.size(20.dp)
                                            )
                                        }
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
                                val dateFormatted = req.createdAt.take(16).replace("T", " ")
                                Text(
                                    text = dateFormatted,
                                    fontSize = 11.sp,
                                    color = DevLinkTextSecondary
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        Text(
                            text = "Project: \"${req.entityName}\"",
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold,
                            color = DevLinkTextPrimary
                        )

                        req.projectDetails?.let { details ->
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = details.description,
                                maxLines = 2,
                                fontSize = 12.sp,
                                color = DevLinkTextSecondary
                            )
                            if (!details.githubUrl.isNullOrBlank()) {
                                Spacer(modifier = Modifier.height(6.dp))
                                Text(
                                    text = "GitHub: ${details.githubUrl}",
                                    fontSize = 11.sp,
                                    color = DevLinkBlue,
                                    fontWeight = FontWeight.Medium
                                )
                            }
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
                // ii) In the place of Submit button: chat icon with 'Message Student'
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
                    // iii) In the place of Message Student: 'Cancel' button
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

                    // iv) In the place of Cancel: 'Submit' button
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
                    Text("Review Project", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = DevLinkTextPrimary)
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
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    // Student Context Row (Avatar + Name + Timestamp)
                    val studentProfile = rankings.find { it.username == req.requestedByUsername }
                    val photoUrl = req.projectDetails?.studentPhotoUrl?.toFullMediaUrl() ?: studentProfile?.photoUrl?.toFullMediaUrl()
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

                    // i) Input field with input number type
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
