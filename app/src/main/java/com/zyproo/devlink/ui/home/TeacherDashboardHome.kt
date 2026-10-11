package com.zyproo.devlink.ui.home

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil3.compose.AsyncImage
import com.zyproo.devlink.common.toFullMediaUrl
import androidx.compose.ui.layout.ContentScale
import com.zyproo.devlink.data.remote.dto.*
import com.zyproo.devlink.ui.theme.*

@Composable
fun TeacherDashboardHome(
    skillsCount: Int,
    projectsCount: Int,
    certificatesCount: Int,
    skillsQueue: List<SkillRequestResponse>,
    projectsQueue: List<VerificationRequestResponse>,
    certificatesQueue: List<VerificationRequestResponse>,
    analytics: TeacherAnalyticsResponse?,
    onNavigateToVerificationCenter: (String?) -> Unit,
    onNavigateToStudents: () -> Unit,
    onNavigateToAnalytics: () -> Unit,
    onNavigateToMessages: () -> Unit,
    onReviewProject: ((VerificationRequestResponse) -> Unit)? = null
) {
    val totalPending = skillsCount + projectsCount + certificatesCount

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(DevLinkBackground),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Welcome Header
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                border = BorderStroke(1.dp, DevLinkBorder),
                elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "Welcome back, Professor",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = DevLinkTextPrimary
                    )
                    Text(
                        text = "Here is your department overview and pending verifications queue.",
                        fontSize = 13.sp,
                        color = DevLinkTextSecondary,
                        modifier = Modifier.padding(top = 4.dp)
                    )
                }
            }
        }

        // 1. Pending Verifications Widget (Breakdown)
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                border = BorderStroke(1.dp, DevLinkBorder),
                elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.FactCheck,
                                contentDescription = null,
                                tint = DevLinkBlue,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Pending Verifications",
                                fontWeight = FontWeight.Bold,
                                fontSize = 15.sp,
                                color = DevLinkTextPrimary
                            )
                        }
                        Surface(
                            color = Color(0xFFFFEDD5),
                            shape = CircleShape
                        ) {
                            Text(
                                text = "$totalPending total",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFFC2410C),
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(12.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        // Skills Category Button
                        Card(
                            modifier = Modifier
                                .weight(1f)
                                .clickable { onNavigateToVerificationCenter("SKILL") },
                            shape = RoundedCornerShape(10.dp),
                            colors = CardDefaults.cardColors(containerColor = Color(0xFFFEF3C7)),
                            border = BorderStroke(1.dp, Color(0xFFFDE68A))
                        ) {
                            Column(
                                modifier = Modifier.padding(12.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Text("$skillsCount", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = Color(0xFF92400E))
                                Text("Skills", fontSize = 11.sp, color = Color(0xFFB45309))
                            }
                        }

                        // Projects Category Button
                        Card(
                            modifier = Modifier
                                .weight(1f)
                                .clickable { onNavigateToVerificationCenter("PROJECT") },
                            shape = RoundedCornerShape(10.dp),
                            colors = CardDefaults.cardColors(containerColor = Color(0xFFDBEAFE)),
                            border = BorderStroke(1.dp, Color(0xFFBFDBFE))
                        ) {
                            Column(
                                modifier = Modifier.padding(12.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Text("$projectsCount", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = Color(0xFF1E40AF))
                                Text("Projects", fontSize = 11.sp, color = Color(0xFF1D4ED8))
                            }
                        }

                        // Certificates Category Button
                        Card(
                            modifier = Modifier
                                .weight(1f)
                                .clickable { onNavigateToVerificationCenter("CERTIFICATE") },
                            shape = RoundedCornerShape(10.dp),
                            colors = CardDefaults.cardColors(containerColor = Color(0xFFD1FAE5)),
                            border = BorderStroke(1.dp, Color(0xFFA7F3D0))
                        ) {
                            Column(
                                modifier = Modifier.padding(12.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Text("$certificatesCount", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = Color(0xFF065F46))
                                Text("Certificates", fontSize = 11.sp, color = Color(0xFF047857))
                            }
                        }
                    }
                }
            }
        }


        // 3. Shortcuts
        item {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(
                    text = "Workspace Shortcuts",
                    fontWeight = FontWeight.Bold,
                    fontSize = 15.sp,
                    color = DevLinkTextPrimary
                )
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    // Shortcut 1: Verification
                    Card(
                        modifier = Modifier
                            .weight(1f)
                            .clickable { onNavigateToVerificationCenter(null) },
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(containerColor = Color.White),
                        border = BorderStroke(1.dp, DevLinkBorder)
                    ) {
                        Row(
                            modifier = Modifier.padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(Icons.Default.FactCheck, contentDescription = null, tint = DevLinkBlue, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Verify Queue", fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = DevLinkTextPrimary)
                        }
                    }

                    // Shortcut 2: Analytics
                    Card(
                        modifier = Modifier
                            .weight(1f)
                            .clickable(onClick = onNavigateToAnalytics),
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(containerColor = Color.White),
                        border = BorderStroke(1.dp, DevLinkBorder)
                    ) {
                        Row(
                            modifier = Modifier.padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(Icons.Default.BarChart, contentDescription = null, tint = DevLinkBlue, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("View Analytics", fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = DevLinkTextPrimary)
                        }
                    }

                    // Shortcut 3: Chat/Messages
                    Card(
                        modifier = Modifier
                            .weight(1f)
                            .clickable(onClick = onNavigateToMessages),
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(containerColor = Color.White),
                        border = BorderStroke(1.dp, DevLinkBorder)
                    ) {
                        Row(
                            modifier = Modifier.padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(Icons.Default.MailOutline, contentDescription = null, tint = DevLinkBlue, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Chat Inbox", fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = DevLinkTextPrimary)
                        }
                    }
                }
            }
        }

        // 4. Recent Activity Feed
        item {
            Text(
                text = "Recent Submissions Feed",
                fontWeight = FontWeight.Bold,
                fontSize = 15.sp,
                color = DevLinkTextPrimary
            )
        }

        if (skillsQueue.isEmpty() && projectsQueue.isEmpty() && certificatesQueue.isEmpty()) {
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    border = BorderStroke(1.dp, DevLinkBorder)
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(20.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "No recent submissions yet.",
                            fontSize = 13.sp,
                            color = DevLinkTextSecondary
                        )
                    }
                }
            }
        } else {
            // Recent Skills activity
            items(skillsQueue.take(3)) { item ->
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    border = BorderStroke(1.dp, DevLinkBorder)
                ) {
                    Row(
                        modifier = Modifier
                            .padding(12.dp)
                            .fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .clip(CircleShape)
                                .background(DevLinkBlueContainer),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = item.studentUsername.take(2).uppercase(),
                                fontWeight = FontWeight.Bold,
                                color = DevLinkBlue,
                                fontSize = 13.sp
                            )
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Skill Claim: ${item.skillName}",
                                fontWeight = FontWeight.Bold,
                                fontSize = 13.sp,
                                color = DevLinkTextPrimary
                            )
                            Text(
                                text = "Submitted by @${item.studentUsername}",
                                fontSize = 11.sp,
                                color = DevLinkTextSecondary
                            )
                        }
                        Surface(
                            color = Color(0xFFFEF3C7),
                            shape = RoundedCornerShape(6.dp)
                        ) {
                            Text(
                                text = "SKILL",
                                fontSize = 9.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF92400E),
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                            )
                        }
                    }
                }
            }

            // Recent Projects activity
            items(projectsQueue.take(3)) { item ->
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable {
                            onReviewProject?.invoke(item) ?: onNavigateToVerificationCenter("PROJECT")
                        },
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    border = BorderStroke(1.dp, DevLinkBorder)
                ) {
                    Row(
                        modifier = Modifier
                            .padding(12.dp)
                            .fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        val projectThumb = item.projectDetails?.imageUrls?.firstOrNull()?.toFullMediaUrl()
                        Surface(
                            modifier = Modifier.size(40.dp),
                            shape = RoundedCornerShape(10.dp),
                            color = DevLinkBlueContainer
                        ) {
                            if (!projectThumb.isNullOrBlank()) {
                                AsyncImage(
                                    model = projectThumb,
                                    contentDescription = "Project Logo",
                                    modifier = Modifier.fillMaxSize(),
                                    contentScale = ContentScale.Crop
                                )
                            } else {
                                Box(contentAlignment = Alignment.Center) {
                                    Icon(
                                        imageVector = Icons.Default.Folder,
                                        contentDescription = "Project Logo",
                                        tint = DevLinkBlue,
                                        modifier = Modifier.size(22.dp)
                                    )
                                }
                            }
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Project: ${item.entityName}",
                                fontWeight = FontWeight.Bold,
                                fontSize = 13.sp,
                                color = DevLinkTextPrimary
                            )
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Text(
                                    text = "By ${item.requestedByName}",
                                    fontSize = 11.sp,
                                    color = DevLinkTextSecondary
                                )
                                val dateFormatted = item.createdAt.take(16).replace("T", " ")
                                Text(
                                    text = "•",
                                    fontSize = 11.sp,
                                    color = DevLinkTextSecondary
                                )
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        imageVector = Icons.Default.AccessTime,
                                        contentDescription = null,
                                        tint = DevLinkTextSecondary,
                                        modifier = Modifier.size(11.dp)
                                    )
                                    Spacer(modifier = Modifier.width(3.dp))
                                    Text(
                                        text = dateFormatted,
                                        fontSize = 10.sp,
                                        color = DevLinkTextSecondary
                                    )
                                }
                            }
                        }
                        Surface(
                            color = DevLinkBlueContainer,
                            shape = RoundedCornerShape(6.dp)
                        ) {
                            Text(
                                text = "PROJECT",
                                fontSize = 9.sp,
                                fontWeight = FontWeight.Bold,
                                color = DevLinkBlue,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}
