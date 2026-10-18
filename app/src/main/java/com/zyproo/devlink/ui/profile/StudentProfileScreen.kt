package com.zyproo.devlink.ui.profile

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil3.compose.AsyncImage
import com.zyproo.devlink.common.toFullMediaUrl
import com.zyproo.devlink.data.remote.dto.*
import com.zyproo.devlink.ui.theme.*
import com.zyproo.devlink.ui.home.StudentUploadScreen

@OptIn(ExperimentalLayoutApi::class, ExperimentalMaterial3Api::class)
@Composable
fun StudentProfileScreen(
    profile: ProfileResponse,
    showContact: Boolean = false,
    isOwnProfile: Boolean = true,
    approvedSkills: List<StudentSkillResponse> = emptyList(),
    skillsCatalog: List<SkillResponse> = emptyList(),
    skillRequests: List<SkillRequestResponse> = emptyList(),
    projects: List<ProjectResponse> = emptyList(),
    certificates: List<CertificateResponse> = emptyList(),
    onRequestSkill: (Long) -> Unit = {},
    onDeleteProject: (Long) -> Unit = {},
    onProjectClick: (ProjectResponse) -> Unit = {},
    onCertificateClick: (CertificateResponse) -> Unit = {},
    onBackClick: (() -> Unit)? = null,
    onMessageClick: ((String) -> Unit)? = null,
    onUploadProject: (String, String, String, String?, String?, List<String>?) -> Unit = { _, _, _, _, _, _ -> },
    onUploadCertificate: (String, String, String, String, FileType) -> Unit = { _, _, _, _, _ -> },
    viewerRole: com.zyproo.devlink.common.Role? = null,
    teacherFeedbacks: List<TeacherFeedbackResponse> = emptyList(),
    onAddFeedback: ((String) -> Unit)? = null,
    onLogoutClick: (() -> Unit)? = null,
    onEditClick: () -> Unit
) {
    var showAddSkillDialog by remember { mutableStateOf(false) }
    var showUploadSheet by remember { mutableStateOf(false) }
    var showAddFeedbackDialog by remember { mutableStateOf(false) }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(DevLinkBackground)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(bottom = 32.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Header Bar when navigating back
            if (onBackClick != null) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Color.White)
                        .padding(horizontal = 16.dp, vertical = 12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(onClick = onBackClick) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Close profile",
                            tint = DevLinkTextPrimary
                        )
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "${profile.name ?: profile.username}'s Profile",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = DevLinkTextPrimary
                    )
                }
            }

            // Hero Profile Header Card
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                border = BorderStroke(1.dp, DevLinkBorder),
                elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
            ) {
                Column {
                    // Banner background gradient
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(80.dp)
                            .background(
                                Brush.horizontalGradient(
                                    listOf(DevLinkBlue, DevLinkBlueLight)
                                )
                            )
                    ) {
                        if (isOwnProfile) {
                            Row(
                                modifier = Modifier
                                    .align(Alignment.TopEnd)
                                    .padding(12.dp),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Surface(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(20.dp))
                                        .clickable { showUploadSheet = true },
                                    color = Color.White.copy(alpha = 0.9f)
                                ) {
                                    Row(
                                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Add,
                                            contentDescription = "Upload",
                                            tint = DevLinkBlue,
                                            modifier = Modifier.size(14.dp)
                                        )
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text(
                                            text = "Upload",
                                            color = DevLinkBlue,
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 12.sp
                                        )
                                    }
                                }

                                Surface(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(20.dp))
                                        .clickable { onEditClick() },
                                    color = Color.White.copy(alpha = 0.9f)
                                ) {
                                    Row(
                                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Edit,
                                            contentDescription = "Edit Profile",
                                            tint = DevLinkBlue,
                                            modifier = Modifier.size(14.dp)
                                        )
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text(
                                            text = "Edit",
                                            color = DevLinkBlue,
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 12.sp
                                        )
                                    }
                                }
                            }
                        } else if (viewerRole == com.zyproo.devlink.common.Role.TEACHER) {
                            val buttonText = if (teacherFeedbacks.isNotEmpty()) "Edit Review" else "Give Review"
                            Surface(
                                modifier = Modifier
                                    .align(Alignment.TopEnd)
                                    .padding(12.dp)
                                    .clip(RoundedCornerShape(20.dp))
                                    .clickable { showAddFeedbackDialog = true },
                                color = Color.White.copy(alpha = 0.9f)
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.RateReview,
                                        contentDescription = buttonText,
                                        tint = DevLinkBlue,
                                        modifier = Modifier.size(14.dp)
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(
                                        text = buttonText,
                                        color = DevLinkBlue,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 12.sp
                                    )
                                }
                            }
                        }
                    }

                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp)
                    ) {
                        // Avatar overlapping banner
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .offset(y = (-36).dp),
                            verticalAlignment = Alignment.Bottom,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            val photoUrl = profile.photoUrl.toFullMediaUrl()
                            Card(
                                shape = CircleShape,
                                border = BorderStroke(3.dp, Color.White),
                                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                                modifier = Modifier.size(76.dp)
                            ) {
                                com.zyproo.devlink.ui.common.UserAvatar(
                                    model = profile.photoUrl,
                                    name = profile.name ?: profile.username,
                                    modifier = Modifier.fillMaxSize(),
                                    fontSize = 24.sp
                                )
                            }

                            if (profile.rank != null && profile.rank > 0) {
                                Surface(
                                    color = Color(0xFFFEF3C7),
                                    shape = RoundedCornerShape(16.dp),
                                    border = BorderStroke(1.dp, Color(0xFFFDE68A)),
                                    modifier = Modifier.offset(y = (8).dp)
                                ) {
                                    Row(
                                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.EmojiEvents,
                                            contentDescription = "Rank",
                                            tint = Color(0xFFD97706),
                                            modifier = Modifier.size(14.dp)
                                        )
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text(
                                            text = "#${profile.rank} Rank",
                                            fontSize = 12.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = Color(0xFFB45309)
                                        )
                                    }
                                }
                            }
                        }

                        Column(modifier = Modifier.offset(y = (-24).dp)) {
                            Text(
                                text = profile.name ?: profile.username,
                                fontSize = 22.sp,
                                fontWeight = FontWeight.Bold,
                                color = DevLinkTextPrimary
                            )
                            Text(
                                text = "@${profile.username}",
                                fontSize = 13.sp,
                                color = DevLinkTextSecondary
                            )
                            Spacer(modifier = Modifier.height(8.dp))

                            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                Surface(
                                    color = DevLinkBlueContainer,
                                    shape = RoundedCornerShape(8.dp)
                                ) {
                                    Text(
                                        text = "${profile.course ?: "Course"} · ${profile.branch ?: "Branch"}",
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.SemiBold,
                                        color = DevLinkBlue,
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                    )
                                }
                                Surface(
                                    color = Color(0xFFF1F5F9),
                                    shape = RoundedCornerShape(8.dp)
                                ) {
                                    Text(
                                        text = "Year ${profile.admissionYear ?: "N/A"} · Sem ${profile.semester ?: 1}",
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Medium,
                                        color = DevLinkTextSecondary,
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                    )
                                }
                            }
                            if (!isOwnProfile && onMessageClick != null) {
                                Spacer(modifier = Modifier.height(12.dp))
                                Button(
                                    onClick = { onMessageClick(profile.username) },
                                    modifier = Modifier.fillMaxWidth().height(40.dp),
                                    colors = ButtonDefaults.buttonColors(containerColor = DevLinkBlue),
                                    shape = RoundedCornerShape(10.dp)
                                ) {
                                    Icon(Icons.Default.MailOutline, contentDescription = null, modifier = Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text("Message Student", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                }
                            }
                        }

                        // Stats Summary Row
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .offset(y = (-8).dp)
                                .clip(RoundedCornerShape(12.dp))
                                .background(Color(0xFFF8FAFC))
                                .padding(vertical = 12.dp),
                            horizontalArrangement = Arrangement.SpaceEvenly
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text(
                                    text = "${profile.totalPoints ?: 0}",
                                    fontSize = 18.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = DevLinkBlue
                                )
                                Text("Points", fontSize = 11.sp, color = DevLinkTextSecondary)
                            }
                            Box(
                                modifier = Modifier
                                    .width(1.dp)
                                    .height(24.dp)
                                    .background(DevLinkBorder)
                            )
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text(
                                    text = "${approvedSkills.size}",
                                    fontSize = 18.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = DevLinkTextPrimary
                                )
                                Text("Skills", fontSize = 11.sp, color = DevLinkTextSecondary)
                            }
                            Box(
                                modifier = Modifier
                                    .width(1.dp)
                                    .height(24.dp)
                                    .background(DevLinkBorder)
                            )
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text(
                                    text = "${projects.size}",
                                    fontSize = 18.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = DevLinkTextPrimary
                                )
                                Text("Projects", fontSize = 11.sp, color = DevLinkTextSecondary)
                            }
                        }
                    }
                }
            }

            // Bio & Contact Details Card
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                border = BorderStroke(1.dp, DevLinkBorder),
                elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Text(
                        text = "About",
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp,
                        color = DevLinkTextPrimary
                    )
                    Text(
                        text = profile.bio ?: "No bio provided.",
                        fontSize = 13.sp,
                        color = DevLinkTextSecondary,
                        lineHeight = 18.sp
                    )

                    HorizontalDivider(color = DevLinkBorder, modifier = Modifier.padding(vertical = 4.dp))

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Email,
                            contentDescription = "Email",
                            tint = DevLinkTextSecondary,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(text = profile.email, fontSize = 13.sp, color = DevLinkTextSecondary)
                    }

                    if (showContact && !profile.contact.isNullOrBlank()) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.Phone,
                                contentDescription = "Phone",
                                tint = DevLinkTextSecondary,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(text = profile.contact, fontSize = 13.sp, color = DevLinkTextSecondary)
                        }
                    }
                }
            }

            // Social & Professional Profiles
            SocialLinksSection(
                socialLinks = profile.socialLinks ?: emptyList(),
                isOwnProfile = isOwnProfile,
                onAddClick = { onEditClick() }
            )

            // Verified Skills Card
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                border = BorderStroke(1.dp, DevLinkBorder),
                elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Verified Skills",
                            fontWeight = FontWeight.Bold,
                            fontSize = 15.sp,
                            color = DevLinkTextPrimary
                        )
                        if (isOwnProfile) {
                            IconButton(
                                onClick = { showAddSkillDialog = true },
                                modifier = Modifier.size(28.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Add,
                                    contentDescription = "Add Skill",
                                    tint = DevLinkBlue
                                )
                            }
                        }
                    }

                    if (approvedSkills.isEmpty()) {
                        Text(
                            text = "No skills verified yet. Click '+' to request evaluation.",
                            fontSize = 13.sp,
                            color = DevLinkTextSecondary
                        )
                    } else {
                        FlowRow(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            approvedSkills.forEach { skill ->
                                Surface(
                                    color = DevLinkBlueContainer,
                                    shape = RoundedCornerShape(20.dp),
                                    border = BorderStroke(1.dp, Color(0xFFBFDBFE))
                                ) {
                                    Row(
                                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text(
                                            text = skill.skillName,
                                            fontSize = 13.sp,
                                            fontWeight = FontWeight.SemiBold,
                                            color = DevLinkBlue
                                        )
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Surface(
                                            color = DevLinkBlue,
                                            shape = CircleShape
                                        ) {
                                            Text(
                                                text = skill.level.name,
                                                fontSize = 9.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = Color.White,
                                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }

            // Skill Verification Requests Section (Below Verified Skills)
            if (isOwnProfile || skillRequests.isNotEmpty()) {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    border = BorderStroke(1.dp, DevLinkBorder),
                    elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.FactCheck,
                                    contentDescription = null,
                                    tint = DevLinkBlue,
                                    modifier = Modifier.size(18.dp)
                                )
                                Text(
                                    text = "Skill Request Status",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 15.sp,
                                    color = DevLinkTextPrimary
                                )
                            }
                            if (isOwnProfile) {
                                IconButton(
                                    onClick = { showAddSkillDialog = true },
                                    modifier = Modifier.size(28.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Add,
                                        contentDescription = "Request Skill",
                                        tint = DevLinkBlue
                                    )
                                }
                            }
                        }

                        if (skillRequests.isEmpty()) {
                            Text(
                                text = "No active skill requests. Tap '+' to request an evaluation from a teacher.",
                                fontSize = 13.sp,
                                color = DevLinkTextSecondary
                            )
                        } else {
                            skillRequests.forEach { req ->
                                Card(
                                    modifier = Modifier.fillMaxWidth(),
                                    shape = RoundedCornerShape(12.dp),
                                    colors = CardDefaults.cardColors(containerColor = Color(0xFFF8FAFC)),
                                    border = BorderStroke(1.dp, DevLinkBorder)
                                ) {
                                    Column(
                                        modifier = Modifier.padding(12.dp),
                                        verticalArrangement = Arrangement.spacedBy(6.dp)
                                    ) {
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Text(
                                                text = req.skillName,
                                                fontWeight = FontWeight.Bold,
                                                fontSize = 14.sp,
                                                color = DevLinkTextPrimary
                                            )

                                            val (statusBg, statusFg, statusLabel) = when (req.status) {
                                                SkillStatus.PENDING -> Triple(Color(0xFFFEF3C7), Color(0xFF92400E), "PENDING")
                                                SkillStatus.MEET -> Triple(Color(0xFFEDE9FE), Color(0xFF5B21B6), "MEETING SCHEDULED")
                                                SkillStatus.ACCEPTED -> Triple(Color(0xFFD1FAE5), Color(0xFF065F46), "APPROVED")
                                                SkillStatus.REJECTED -> Triple(Color(0xFFFEE2E2), Color(0xFF991B1B), "REJECTED")
                                            }

                                            Surface(
                                                color = statusBg,
                                                shape = RoundedCornerShape(6.dp)
                                            ) {
                                                Text(
                                                    text = statusLabel,
                                                    fontSize = 10.sp,
                                                    fontWeight = FontWeight.Bold,
                                                    color = statusFg,
                                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                                )
                                            }
                                        }

                                        if (req.teacherName.isNotBlank()) {
                                            Row(verticalAlignment = Alignment.CenterVertically) {
                                                Icon(
                                                    imageVector = Icons.Default.Person,
                                                    contentDescription = null,
                                                    tint = DevLinkTextSecondary,
                                                    modifier = Modifier.size(13.dp)
                                                )
                                                Spacer(modifier = Modifier.width(4.dp))
                                                Text(
                                                    text = "Assigned Teacher: ${req.teacherName}",
                                                    fontSize = 12.sp,
                                                    color = DevLinkTextSecondary
                                                )
                                            }
                                        }

                                        if (req.status == SkillStatus.MEET && !req.meetingDate.isNullOrBlank()) {
                                            Surface(
                                                color = Color(0xFFF5F3FF),
                                                shape = RoundedCornerShape(8.dp),
                                                border = BorderStroke(1.dp, Color(0xFFDDD6FE)),
                                                modifier = Modifier.fillMaxWidth()
                                            ) {
                                                Column(modifier = Modifier.padding(8.dp)) {
                                                    Text(
                                                        text = "Meeting Scheduled: ${req.meetingDate}",
                                                        fontSize = 11.sp,
                                                        fontWeight = FontWeight.SemiBold,
                                                        color = Color(0xFF5B21B6)
                                                    )
                                                    if (!req.meetingNotes.isNullOrBlank()) {
                                                        Text(
                                                            text = "Notes: ${req.meetingNotes}",
                                                            fontSize = 11.sp,
                                                            color = DevLinkTextSecondary
                                                        )
                                                    }
                                                }
                                            }
                                        }

                                        if (req.status == SkillStatus.REJECTED && !req.rejectionReason.isNullOrBlank()) {
                                            Surface(
                                                color = Color(0xFFFEF2F2),
                                                shape = RoundedCornerShape(8.dp),
                                                border = BorderStroke(1.dp, Color(0xFFFECACA)),
                                                modifier = Modifier.fillMaxWidth()
                                            ) {
                                                Text(
                                                    text = "Rejection Reason: ${req.rejectionReason}",
                                                    fontSize = 11.sp,
                                                    color = Color(0xFF991B1B),
                                                    modifier = Modifier.padding(8.dp)
                                                )
                                            }
                                        }

                                        if (!req.createdAt.isNullOrBlank()) {
                                            Text(
                                                text = "Requested on ${req.createdAt.take(10)}",
                                                fontSize = 10.sp,
                                                color = Color(0xFF94A3B8)
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }

            // Badges Section
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                border = BorderStroke(1.dp, DevLinkBorder),
                elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Text(
                        text = "Earned Badges",
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp,
                        color = DevLinkTextPrimary
                    )

                    val badgeList = profile.badges
                    if (badgeList.isEmpty()) {
                        Text(
                            text = "No badges earned yet. Complete verifications to unlock badges!",
                            fontSize = 13.sp,
                            color = DevLinkTextSecondary
                        )
                    } else {
                        FlowRow(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            badgeList.forEach { badge ->
                                val badgeBg = when (badge) {
                                    "Android Developer" -> Color(0xFFECFDF5)
                                    "Top Contributor" -> Color(0xFFFEF3C7)
                                    "Hackathon Winner" -> Color(0xFFF3E8FF)
                                    else -> Color(0xFFF1F5F9)
                                }
                                val badgeText = when (badge) {
                                    "Android Developer" -> Color(0xFF047857)
                                    "Top Contributor" -> Color(0xFFB45309)
                                    "Hackathon Winner" -> Color(0xFF6B21A8)
                                    else -> DevLinkTextPrimary
                                }
                                Surface(
                                    color = badgeBg,
                                    shape = RoundedCornerShape(12.dp)
                                ) {
                                    Text(
                                        text = badge,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 12.sp,
                                        color = badgeText,
                                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // Projects Section
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                border = BorderStroke(1.dp, DevLinkBorder),
                elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Text(
                        text = "Projects",
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp,
                        color = DevLinkTextPrimary
                    )

                    if (projects.isEmpty()) {
                        Text("No projects uploaded yet.", fontSize = 13.sp, color = DevLinkTextSecondary)
                    } else {
                        projects.forEach { proj ->
                            Card(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable { onProjectClick(proj) },
                                shape = RoundedCornerShape(12.dp),
                                colors = CardDefaults.cardColors(containerColor = Color(0xFFF8FAFC)),
                                border = BorderStroke(1.dp, DevLinkBorder)
                            ) {
                                Column(modifier = Modifier.padding(12.dp)) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text(proj.name, fontWeight = FontWeight.Bold, fontSize = 14.sp, color = DevLinkTextPrimary)
                                        
                                        if (isOwnProfile && proj.studentUserId == profile.userId) {
                                            var menuExpanded by remember { mutableStateOf(false) }
                                            Box {
                                                IconButton(
                                                    onClick = { menuExpanded = true },
                                                    modifier = Modifier.size(24.dp)
                                                ) {
                                                    Icon(
                                                        imageVector = Icons.Default.MoreVert,
                                                        contentDescription = "Options",
                                                        tint = DevLinkTextSecondary
                                                    )
                                                }
                                                DropdownMenu(
                                                    expanded = menuExpanded,
                                                    onDismissRequest = { menuExpanded = false }
                                                ) {
                                                    DropdownMenuItem(
                                                        text = { Text("Delete") },
                                                        onClick = {
                                                            onDeleteProject(proj.id)
                                                            menuExpanded = false
                                                        }
                                                    )
                                                }
                                            }
                                        }
                                    }
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(
                                        text = proj.description,
                                        maxLines = 2,
                                        overflow = TextOverflow.Ellipsis,
                                        fontSize = 12.sp,
                                        color = DevLinkTextSecondary
                                    )
                                    Spacer(modifier = Modifier.height(8.dp))
                                    Row(
                                        horizontalArrangement = Arrangement.spacedBy(16.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Icon(Icons.Default.Favorite, contentDescription = null, tint = Color(0xFFEF4444), modifier = Modifier.size(14.dp))
                                            Spacer(modifier = Modifier.width(4.dp))
                                            Text(proj.likeCount.toString(), fontSize = 12.sp, color = DevLinkTextSecondary)
                                        }
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Icon(Icons.Default.Comment, contentDescription = null, tint = DevLinkBlue, modifier = Modifier.size(14.dp))
                                            Spacer(modifier = Modifier.width(4.dp))
                                            Text(proj.commentCount.toString(), fontSize = 12.sp, color = DevLinkTextSecondary)
                                        }
                                        Spacer(modifier = Modifier.weight(1f))
                                        Surface(
                                            color = if (proj.verificationStatus == VerificationStatus.APPROVED) Color(0xFFD1FAE5) else Color(0xFFFEF3C7),
                                            shape = RoundedCornerShape(6.dp)
                                        ) {
                                            Text(
                                                text = proj.verificationStatus.name,
                                                fontSize = 10.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = if (proj.verificationStatus == VerificationStatus.APPROVED) Color(0xFF065F46) else Color(0xFF92400E),
                                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }

            // Certificates Section
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                border = BorderStroke(1.dp, DevLinkBorder),
                elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Text(
                        text = "Certificates",
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp,
                        color = DevLinkTextPrimary
                    )

                    if (certificates.isEmpty()) {
                        Text("No certificates uploaded yet.", fontSize = 13.sp, color = DevLinkTextSecondary)
                    } else {
                        certificates.forEach { cert ->
                            Card(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable { onCertificateClick(cert) },
                                shape = RoundedCornerShape(12.dp),
                                colors = CardDefaults.cardColors(containerColor = Color(0xFFF8FAFC)),
                                border = BorderStroke(1.dp, DevLinkBorder)
                            ) {
                                Column(modifier = Modifier.padding(12.dp)) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text(cert.title, fontWeight = FontWeight.Bold, fontSize = 14.sp, color = DevLinkTextPrimary)
                                        Surface(
                                            color = if (cert.verificationStatus == VerificationStatus.APPROVED) Color(0xFFD1FAE5) else Color(0xFFFEF3C7),
                                            shape = RoundedCornerShape(6.dp)
                                        ) {
                                            Text(
                                                text = cert.verificationStatus.name,
                                                fontSize = 10.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = if (cert.verificationStatus == VerificationStatus.APPROVED) Color(0xFF065F46) else Color(0xFF92400E),
                                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                            )
                                        }
                                    }
                                    Spacer(modifier = Modifier.height(2.dp))
                                    Text("Issued By: ${cert.issuingOrg}", fontSize = 12.sp, color = DevLinkTextSecondary, fontWeight = FontWeight.Medium)
                                }
                            }
                        }
                    }
                }
            }

            // Teacher Reviews Section (VISIBLE TO TEACHERS AND RECRUITERS)
            if (viewerRole == com.zyproo.devlink.common.Role.RECRUITER || viewerRole == com.zyproo.devlink.common.Role.TEACHER) {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    border = BorderStroke(1.dp, DevLinkBorder),
                    elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.RateReview,
                                contentDescription = null,
                                tint = DevLinkBlue,
                                modifier = Modifier.size(18.dp)
                            )
                            Text(
                                text = "Teacher Reviews & Endorsements",
                                fontWeight = FontWeight.Bold,
                                fontSize = 15.sp,
                                color = DevLinkTextPrimary
                            )
                        }

                        if (teacherFeedbacks.isEmpty()) {
                            Text(
                                text = "No teacher reviews submitted for this student yet.",
                                fontSize = 13.sp,
                                color = DevLinkTextSecondary
                            )
                        } else {
                            teacherFeedbacks.forEach { feedback ->
                                Card(
                                    modifier = Modifier.fillMaxWidth(),
                                    shape = RoundedCornerShape(12.dp),
                                    colors = CardDefaults.cardColors(containerColor = Color(0xFFF8FAFC)),
                                    border = BorderStroke(1.dp, DevLinkBorder)
                                ) {
                                    Column(
                                        modifier = Modifier.padding(12.dp),
                                        verticalArrangement = Arrangement.spacedBy(6.dp)
                                    ) {
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Text(
                                                text = feedback.teacherName,
                                                fontWeight = FontWeight.Bold,
                                                fontSize = 13.sp,
                                                color = DevLinkTextPrimary
                                            )
                                            Text(
                                                text = feedback.createdAt.take(10),
                                                fontSize = 11.sp,
                                                color = DevLinkTextSecondary
                                            )
                                        }
                                        if (!feedback.department.isNullOrBlank()) {
                                            Text(
                                                text = feedback.department ?: "",
                                                fontSize = 11.sp,
                                                color = DevLinkBlue,
                                                fontWeight = FontWeight.SemiBold
                                            )
                                        }
                                        Text(
                                            text = feedback.content,
                                            fontSize = 13.sp,
                                            color = DevLinkTextPrimary,
                                            lineHeight = 18.sp
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }

            if (isOwnProfile && onLogoutClick != null) {
                Spacer(modifier = Modifier.height(16.dp))
                OutlinedButton(
                    onClick = onLogoutClick,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp)
                        .height(48.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = Color(0xFFEF4444)),
                    border = BorderStroke(1.dp, Color(0xFFFCA5A5))
                ) {
                    Icon(imageVector = Icons.Default.ExitToApp, contentDescription = "Logout", modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Log Out", fontWeight = FontWeight.Bold)
                }
                Spacer(modifier = Modifier.height(24.dp))
            }
        }

        // Request Skill Dialog Overlay
        if (showAddSkillDialog) {
            RequestSkillDialog(
                skillsCatalog = skillsCatalog,
                onDismiss = { showAddSkillDialog = false },
                onSubmit = { skillId ->
                    onRequestSkill(skillId)
                    showAddSkillDialog = false
                }
            )
        }

        // Teacher Give / Edit Review Dialog Overlay
        if (showAddFeedbackDialog) {
            val initialText = remember(teacherFeedbacks) { teacherFeedbacks.firstOrNull()?.content ?: "" }
            var feedbackText by remember(showAddFeedbackDialog) { mutableStateOf(initialText) }
            val isEditing = initialText.isNotBlank()

            AlertDialog(
                onDismissRequest = { showAddFeedbackDialog = false },
                confirmButton = {
                    Button(
                        onClick = {
                            if (feedbackText.isNotBlank()) {
                                onAddFeedback?.invoke(feedbackText)
                                showAddFeedbackDialog = false
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = DevLinkBlue),
                        enabled = feedbackText.isNotBlank()
                    ) {
                        Text(if (isEditing) "Update Review" else "Submit Review", color = Color.White)
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showAddFeedbackDialog = false }) {
                        Text("Cancel", color = Color.Gray)
                    }
                },
                title = { Text(if (isEditing) "Edit Student Review" else "Give Student Review", fontSize = 16.sp, fontWeight = FontWeight.Bold) },
                text = {
                    Column(modifier = Modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        Text(
                            text = "Write or update your review for ${profile.name ?: profile.username}. This review will be visible to Teachers and Recruiters when viewing this student's profile.",
                            fontSize = 12.sp,
                            color = DevLinkTextSecondary
                        )
                        OutlinedTextField(
                            value = feedbackText,
                            onValueChange = { feedbackText = it },
                            label = { Text("Teacher Review / Endorsement") },
                            placeholder = { Text("e.g. Excellent work ethic, solid domain knowledge in backend systems...") },
                            modifier = Modifier.fillMaxWidth(),
                            maxLines = 5
                        )
                    }
                }
            )
        }

        // Upload Project / Certificate Sheet Overlay
        if (showUploadSheet) {
            ModalBottomSheet(
                onDismissRequest = { showUploadSheet = false },
                sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(bottom = 24.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 8.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("Upload Project / Certificate", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                        IconButton(onClick = { showUploadSheet = false }) {
                            Icon(Icons.Default.Close, contentDescription = "Close")
                        }
                    }
                    Box(modifier = Modifier.weight(1f)) {
                        StudentUploadScreen(
                            onUploadProject = { name, desc, tech, github, demo, images ->
                                onUploadProject(name, desc, tech, github, demo, images)
                                showUploadSheet = false
                            },
                            onUploadCertificate = { title, org, purp, fileUrl, fileType ->
                                onUploadCertificate(title, org, purp, fileUrl, fileType)
                                showUploadSheet = false
                            }
                        )
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RequestSkillDialog(
    skillsCatalog: List<SkillResponse>,
    onDismiss: () -> Unit,
    onSubmit: (Long) -> Unit
) {
    var selectedSkillId by remember { mutableStateOf(skillsCatalog.firstOrNull()?.id ?: 0L) }
    var expanded by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = onDismiss,
        confirmButton = {
            TextButton(
                onClick = {
                    if (selectedSkillId != 0L) {
                        onSubmit(selectedSkillId)
                    }
                },
                enabled = selectedSkillId != 0L
            ) {
                Text("Request", color = DevLinkBlue, fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel", color = Color.Gray)
            }
        },
        title = { Text("Request Skill Verification", fontSize = 16.sp, fontWeight = FontWeight.Bold) },
        text = {
            Column(modifier = Modifier.fillMaxWidth()) {
                Text(
                    text = "Choose a skill to send for evaluation. The system will automatically route it to a department teacher in strict sequence.",
                    fontSize = 13.sp,
                    color = Color.Gray
                )
                Spacer(modifier = Modifier.height(16.dp))

                val selectedSkillName = skillsCatalog.find { it.id == selectedSkillId }?.name ?: "Select Skill"
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { expanded = true }
                ) {
                    OutlinedTextField(
                        value = selectedSkillName,
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Skill") },
                        modifier = Modifier.fillMaxWidth(),
                        enabled = false,
                        colors = OutlinedTextFieldDefaults.colors(
                            disabledTextColor = MaterialTheme.colorScheme.onSurface,
                            disabledBorderColor = MaterialTheme.colorScheme.outline,
                            disabledLabelColor = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    )
                    DropdownMenu(
                        expanded = expanded,
                        onDismissRequest = { expanded = false }
                    ) {
                        skillsCatalog.forEach { skill ->
                            DropdownMenuItem(
                                text = { Text(skill.name) },
                                onClick = {
                                    selectedSkillId = skill.id
                                    expanded = false
                                }
                            )
                        }
                    }
                }
            }
        }
    )
}
