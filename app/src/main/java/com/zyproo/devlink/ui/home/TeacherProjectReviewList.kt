package com.zyproo.devlink.ui.home

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccessTime
import androidx.compose.material.icons.filled.Code
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.Link
import androidx.compose.material.icons.automirrored.filled.OpenInNew
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
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.zyproo.devlink.data.remote.dto.*
import com.zyproo.devlink.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
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

                        val displayTitle = req.projectDetails?.name?.ifBlank { null } ?: req.entityName
                        Text(
                            text = "Project: $displayTitle",
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold,
                            color = DevLinkTextPrimary
                        )

                        req.projectDetails?.let { details ->
                            if (details.description.isNotBlank()) {
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = details.description,
                                    maxLines = 2,
                                    fontSize = 12.sp,
                                    color = DevLinkTextSecondary
                                )
                            }

                            if (details.techStack.isNotBlank()) {
                                Spacer(modifier = Modifier.height(6.dp))
                                FlowRow(
                                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                                    verticalArrangement = Arrangement.spacedBy(4.dp)
                                ) {
                                    details.techStack.split(",").take(4).forEach { tag ->
                                        Surface(
                                            color = DevLinkBlueContainer,
                                            shape = RoundedCornerShape(6.dp)
                                        ) {
                                            Text(
                                                text = tag.trim(),
                                                fontSize = 10.sp,
                                                fontWeight = FontWeight.Medium,
                                                color = DevLinkBlue,
                                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                            )
                                        }
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.height(8.dp))
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(
                                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    if (details.imageUrls.isNotEmpty()) {
                                        Surface(
                                            color = Color(0xFFF1F5F9),
                                            shape = RoundedCornerShape(6.dp)
                                        ) {
                                            Row(
                                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp),
                                                verticalAlignment = Alignment.CenterVertically
                                            ) {
                                                Icon(
                                                    imageVector = Icons.Default.Image,
                                                    contentDescription = null,
                                                    tint = DevLinkTextSecondary,
                                                    modifier = Modifier.size(12.dp)
                                                )
                                                Spacer(modifier = Modifier.width(4.dp))
                                                Text(
                                                    text = "${details.imageUrls.size} photos",
                                                    fontSize = 10.sp,
                                                    color = DevLinkTextSecondary,
                                                    fontWeight = FontWeight.Medium
                                                )
                                            }
                                        }
                                    }

                                    if (!details.githubUrl.isNullOrBlank()) {
                                        Surface(
                                            color = Color(0xFFF1F5F9),
                                            shape = RoundedCornerShape(6.dp)
                                        ) {
                                            Row(
                                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp),
                                                verticalAlignment = Alignment.CenterVertically
                                            ) {
                                                Icon(
                                                    imageVector = Icons.Default.Code,
                                                    contentDescription = null,
                                                    tint = DevLinkTextSecondary,
                                                    modifier = Modifier.size(12.dp)
                                                )
                                                Spacer(modifier = Modifier.width(4.dp))
                                                Text(
                                                    text = "GitHub",
                                                    fontSize = 10.sp,
                                                    color = DevLinkTextSecondary,
                                                    fontWeight = FontWeight.Medium
                                                )
                                            }
                                        }
                                    }

                                    if (!details.demoUrl.isNullOrBlank()) {
                                        Surface(
                                            color = DevLinkBlueContainer,
                                            shape = RoundedCornerShape(6.dp)
                                        ) {
                                            Row(
                                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp),
                                                verticalAlignment = Alignment.CenterVertically
                                            ) {
                                                Icon(
                                                    imageVector = Icons.Default.Link,
                                                    contentDescription = null,
                                                    tint = DevLinkBlue,
                                                    modifier = Modifier.size(12.dp)
                                                )
                                                Spacer(modifier = Modifier.width(4.dp))
                                                Text(
                                                    text = "Demo",
                                                    fontSize = 10.sp,
                                                    color = DevLinkBlue,
                                                    fontWeight = FontWeight.Medium
                                                )
                                            }
                                        }
                                    }
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
                    Text("Review Project Submission", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = DevLinkTextPrimary)
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

                    // Project Details Section
                    val details = req.projectDetails
                    val projName = details?.name?.ifBlank { null } ?: req.entityName
                    val projDesc = details?.description
                    val techStack = details?.techStack
                    val githubUrl = details?.githubUrl
                    val demoUrl = details?.demoUrl
                    val images = details?.imageUrls ?: emptyList()

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
                            // Project Title
                            Text(
                                text = projName,
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold,
                                color = DevLinkTextPrimary
                            )

                            // Uploaded Time
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

                            // Project Description
                            if (!projDesc.isNullOrBlank()) {
                                Text(
                                    text = projDesc,
                                    fontSize = 13.sp,
                                    color = DevLinkTextSecondary,
                                    lineHeight = 18.sp
                                )
                            }

                            // Tech Stack Chips
                            if (!techStack.isNullOrBlank()) {
                                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                    Text(
                                        text = "Tech Stack:",
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = DevLinkTextSecondary
                                    )
                                    FlowRow(
                                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                                        verticalArrangement = Arrangement.spacedBy(4.dp)
                                    ) {
                                        techStack.split(",").map { it.trim() }.filter { it.isNotEmpty() }.forEach { tag ->
                                            Surface(
                                                color = DevLinkBlueContainer,
                                                shape = RoundedCornerShape(6.dp)
                                            ) {
                                                Text(
                                                    text = tag,
                                                    fontSize = 11.sp,
                                                    fontWeight = FontWeight.Medium,
                                                    color = DevLinkBlue,
                                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                                                )
                                            }
                                        }
                                    }
                                }
                            }

                            // Project Links (GitHub & Live Demo)
                            if (!githubUrl.isNullOrBlank() || !demoUrl.isNullOrBlank()) {
                                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                    Text(
                                        text = "Project Links:",
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = DevLinkTextSecondary
                                    )
                                    Row(
                                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                                        modifier = Modifier.fillMaxWidth()
                                    ) {
                                        if (!githubUrl.isNullOrBlank()) {
                                            Surface(
                                                color = Color(0xFF1E293B),
                                                shape = RoundedCornerShape(8.dp),
                                                modifier = Modifier.clickable {
                                                    val formatted = if (githubUrl.startsWith("http://") || githubUrl.startsWith("https://")) githubUrl else "https://$githubUrl"
                                                    try { uriHandler.openUri(formatted) } catch (e: Exception) {}
                                                }
                                            ) {
                                                Row(
                                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                                                    verticalAlignment = Alignment.CenterVertically
                                                ) {
                                                    Icon(
                                                        imageVector = Icons.Default.Code,
                                                        contentDescription = "GitHub",
                                                        tint = Color.White,
                                                        modifier = Modifier.size(14.dp)
                                                    )
                                                    Spacer(modifier = Modifier.width(6.dp))
                                                    Text(
                                                        text = "GitHub Repo",
                                                        fontSize = 11.sp,
                                                        fontWeight = FontWeight.Bold,
                                                        color = Color.White
                                                    )
                                                    Spacer(modifier = Modifier.width(4.dp))
                                                    Icon(
                                                        imageVector = Icons.AutoMirrored.Filled.OpenInNew,
                                                        contentDescription = null,
                                                        tint = Color.White.copy(alpha = 0.7f),
                                                        modifier = Modifier.size(12.dp)
                                                    )
                                                }
                                            }
                                        }

                                        if (!demoUrl.isNullOrBlank()) {
                                            Surface(
                                                color = DevLinkBlue,
                                                shape = RoundedCornerShape(8.dp),
                                                modifier = Modifier.clickable {
                                                    val formatted = if (demoUrl.startsWith("http://") || demoUrl.startsWith("https://")) demoUrl else "https://$demoUrl"
                                                    try { uriHandler.openUri(formatted) } catch (e: Exception) {}
                                                }
                                            ) {
                                                Row(
                                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                                                    verticalAlignment = Alignment.CenterVertically
                                                ) {
                                                    Icon(
                                                        imageVector = Icons.Default.Link,
                                                        contentDescription = "Live Demo",
                                                        tint = Color.White,
                                                        modifier = Modifier.size(14.dp)
                                                    )
                                                    Spacer(modifier = Modifier.width(6.dp))
                                                    Text(
                                                        text = "Live Demo",
                                                        fontSize = 11.sp,
                                                        fontWeight = FontWeight.Bold,
                                                        color = Color.White
                                                    )
                                                    Spacer(modifier = Modifier.width(4.dp))
                                                    Icon(
                                                        imageVector = Icons.AutoMirrored.Filled.OpenInNew,
                                                        contentDescription = null,
                                                        tint = Color.White.copy(alpha = 0.7f),
                                                        modifier = Modifier.size(12.dp)
                                                    )
                                                }
                                            }
                                        }
                                    }
                                }
                            }

                            // Project Screenshots Gallery
                            if (images.isNotEmpty()) {
                                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                    Text(
                                        text = "Project Photos (${images.size}):",
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = DevLinkTextSecondary
                                    )
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .horizontalScroll(rememberScrollState()),
                                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                                    ) {
                                        images.forEachIndexed { index, imgUrl ->
                                            val fullMediaUrl = imgUrl.toFullMediaUrl()
                                            if (!fullMediaUrl.isNullOrBlank()) {
                                                Box(
                                                    modifier = Modifier
                                                        .width(180.dp)
                                                        .height(115.dp)
                                                        .clip(RoundedCornerShape(8.dp))
                                                        .background(Color(0xFFE2E8F0))
                                                        .clickable {
                                                            try { uriHandler.openUri(fullMediaUrl) } catch (e: Exception) {}
                                                        }
                                                ) {
                                                    AsyncImage(
                                                        model = fullMediaUrl,
                                                        contentDescription = "Project Photo ${index + 1}",
                                                        contentScale = ContentScale.Crop,
                                                        modifier = Modifier.fillMaxSize()
                                                    )
                                                    Surface(
                                                        color = Color.Black.copy(alpha = 0.65f),
                                                        shape = RoundedCornerShape(4.dp),
                                                        modifier = Modifier
                                                            .align(Alignment.BottomEnd)
                                                            .padding(4.dp)
                                                    ) {
                                                        Text(
                                                            text = "${index + 1}/${images.size}",
                                                            color = Color.White,
                                                            fontSize = 9.sp,
                                                            fontWeight = FontWeight.Bold,
                                                            modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp)
                                                        )
                                                    }
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }

                    HorizontalDivider(color = DevLinkBorder)

                    // Evaluation Decision
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

                    // Input field with input number type
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
