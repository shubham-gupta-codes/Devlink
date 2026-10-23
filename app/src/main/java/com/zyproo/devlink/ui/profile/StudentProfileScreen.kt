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
import com.zyproo.devlink.common.formatMeetingDateTime
import com.zyproo.devlink.common.formatDisplayDate
import com.zyproo.devlink.common.formatDisplayDateTime
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
    var activeSubTab by remember { mutableStateOf(0) } // 0: Projects, 1: Skills, 2: Certificates
    var selectedStatusFilter by remember { mutableStateOf("ALL") } // ALL, APPROVED, PENDING, REJECTED
    var filterDropdownExpanded by remember { mutableStateOf(false) }

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
                Surface(
                    color = Color.White,
                    shadowElevation = 1.dp,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .statusBarsPadding()
                            .padding(horizontal = 16.dp, vertical = 14.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        IconButton(
                            onClick = onBackClick,
                            modifier = Modifier.size(36.dp)
                        ) {
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
                            text = "No skills available yet.",
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

            // Teacher Reviews Section (Placed below Verified Skills)
            // Only visible to Teachers and Recruiters
            if (viewerRole == com.zyproo.devlink.common.Role.TEACHER || viewerRole == com.zyproo.devlink.common.Role.RECRUITER) {
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
                            if (viewerRole == com.zyproo.devlink.common.Role.TEACHER && onAddFeedback != null) {
                                TextButton(
                                    onClick = { showAddFeedbackDialog = true },
                                    contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Edit,
                                        contentDescription = "Review",
                                        tint = DevLinkBlue,
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(
                                        text = if (teacherFeedbacks.isNotEmpty()) "Edit" else "Add Review",
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = DevLinkBlue
                                    )
                                }
                            }
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
                                                text = formatDisplayDate(feedback.createdAt),
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

            // Sub-Status Tabs Section (Below Teacher Reviews): Project, Skill, Certificate with Dropdown Filter
            // Only visible to the Teacher and the Student who owns this profile
            val canViewVerificationStatus = isOwnProfile || viewerRole == com.zyproo.devlink.common.Role.TEACHER
            if (canViewVerificationStatus) {
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
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    // Header Row with Tabs and Filter Dropdown
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Verification Status",
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp,
                            color = DevLinkTextPrimary
                        )

                        // Status Filter Dropdown (All, Approved, Pending, Rejected)
                        Box {
                            Surface(
                                color = Color(0xFFF1F5F9),
                                shape = RoundedCornerShape(8.dp),
                                border = BorderStroke(1.dp, DevLinkBorder),
                                modifier = Modifier
                                    .clip(RoundedCornerShape(8.dp))
                                    .clickable { filterDropdownExpanded = true }
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                                ) {
                                    val filterColor = when (selectedStatusFilter) {
                                        "APPROVED" -> Color(0xFF059669)
                                        "PENDING" -> Color(0xFFD97706)
                                        "REJECTED" -> Color(0xFFDC2626)
                                        else -> DevLinkBlue
                                    }
                                    Box(
                                        modifier = Modifier
                                            .size(7.dp)
                                            .background(filterColor, CircleShape)
                                    )
                                    Text(
                                        text = when (selectedStatusFilter) {
                                            "APPROVED" -> "Approved"
                                            "PENDING" -> "Pending"
                                            "REJECTED" -> "Rejected"
                                            else -> "All Status"
                                        },
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = DevLinkTextPrimary
                                    )
                                    Icon(
                                        imageVector = Icons.Default.ArrowDropDown,
                                        contentDescription = "Filter",
                                        tint = DevLinkTextSecondary,
                                        modifier = Modifier.size(18.dp)
                                    )
                                }
                            }

                            DropdownMenu(
                                expanded = filterDropdownExpanded,
                                onDismissRequest = { filterDropdownExpanded = false }
                            ) {
                                listOf("ALL" to "All Status", "APPROVED" to "Approved", "PENDING" to "Pending", "REJECTED" to "Rejected").forEach { (key, label) ->
                                    DropdownMenuItem(
                                        text = {
                                            Row(
                                                verticalAlignment = Alignment.CenterVertically,
                                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                                            ) {
                                                val dotColor = when (key) {
                                                    "APPROVED" -> Color(0xFF059669)
                                                    "PENDING" -> Color(0xFFD97706)
                                                    "REJECTED" -> Color(0xFFDC2626)
                                                    else -> DevLinkBlue
                                                }
                                                Box(modifier = Modifier.size(8.dp).background(dotColor, CircleShape))
                                                Text(
                                                    text = label,
                                                    fontWeight = if (selectedStatusFilter == key) FontWeight.Bold else FontWeight.Normal,
                                                    color = if (selectedStatusFilter == key) DevLinkBlue else DevLinkTextPrimary
                                                )
                                            }
                                        },
                                        onClick = {
                                            selectedStatusFilter = key
                                            filterDropdownExpanded = false
                                        }
                                    )
                                }
                            }
                        }
                    }

                    // 3 Sub Tabs: Projects, Skills, Certificates
                    val tabs = listOf("Projects", "Skills", "Certificates")
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(Color(0xFFF1F5F9), RoundedCornerShape(10.dp))
                            .padding(4.dp),
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        tabs.forEachIndexed { index, tabTitle ->
                            val isSelected = activeSubTab == index
                            Surface(
                                modifier = Modifier
                                    .weight(1f)
                                    .clip(RoundedCornerShape(8.dp))
                                    .clickable { activeSubTab = index },
                                color = if (isSelected) Color.White else Color.Transparent,
                                shadowElevation = if (isSelected) 1.dp else 0.dp,
                                shape = RoundedCornerShape(8.dp)
                            ) {
                                Row(
                                    modifier = Modifier.padding(vertical = 8.dp),
                                    horizontalArrangement = Arrangement.Center,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    val tabIcon = when (index) {
                                        0 -> Icons.Default.Folder
                                        1 -> Icons.Default.Build
                                        else -> Icons.Default.CardMembership
                                    }
                                    Icon(
                                        imageVector = tabIcon,
                                        contentDescription = null,
                                        tint = if (isSelected) DevLinkBlue else DevLinkTextSecondary,
                                        modifier = Modifier.size(15.dp)
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = tabTitle,
                                        fontSize = 12.sp,
                                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                        color = if (isSelected) DevLinkBlue else DevLinkTextSecondary
                                    )
                                }
                            }
                        }
                    }

                    // Content for Selected Sub Tab with Status Filter applied
                    when (activeSubTab) {
                        0 -> {
                            // PROJECTS TAB
                            val filteredProjects = projects.filter { proj ->
                                when (selectedStatusFilter) {
                                    "APPROVED" -> proj.verificationStatus == VerificationStatus.APPROVED
                                    "PENDING" -> proj.verificationStatus == VerificationStatus.PENDING
                                    "REJECTED" -> proj.verificationStatus == VerificationStatus.REJECTED
                                    else -> true
                                }
                            }

                            if (filteredProjects.isEmpty()) {
                                Surface(
                                    modifier = Modifier.fillMaxWidth(),
                                    color = Color(0xFFF8FAFC),
                                    shape = RoundedCornerShape(10.dp),
                                    border = BorderStroke(1.dp, DevLinkBorder)
                                ) {
                                    Column(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(20.dp),
                                        horizontalAlignment = Alignment.CenterHorizontally
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.FolderOpen,
                                            contentDescription = null,
                                            tint = Color(0xFF94A3B8),
                                            modifier = Modifier.size(32.dp)
                                        )
                                        Spacer(modifier = Modifier.height(6.dp))
                                        Text(
                                            text = if (selectedStatusFilter == "ALL") "No projects uploaded yet." else "No $selectedStatusFilter projects found.",
                                            fontSize = 13.sp,
                                            color = DevLinkTextSecondary
                                        )
                                    }
                                }
                            } else {
                                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                                    filteredProjects.forEach { proj ->
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
                                                    if (!proj.createdAt.isNullOrBlank()) {
                                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                                            Icon(Icons.Default.AccessTime, contentDescription = null, tint = DevLinkTextSecondary, modifier = Modifier.size(13.dp))
                                                            Spacer(modifier = Modifier.width(3.dp))
                                                            Text(formatDisplayDate(proj.createdAt), fontSize = 11.sp, color = DevLinkTextSecondary)
                                                        }
                                                    }
                                                    Spacer(modifier = Modifier.weight(1f))
                                                    Surface(
                                                        color = when (proj.verificationStatus) {
                                                            VerificationStatus.APPROVED -> Color(0xFFD1FAE5)
                                                            VerificationStatus.REJECTED -> Color(0xFFFEE2E2)
                                                            else -> Color(0xFFFEF3C7)
                                                        },
                                                        shape = RoundedCornerShape(6.dp)
                                                    ) {
                                                        Text(
                                                            text = proj.verificationStatus.name,
                                                            fontSize = 10.sp,
                                                            fontWeight = FontWeight.Bold,
                                                            color = when (proj.verificationStatus) {
                                                                VerificationStatus.APPROVED -> Color(0xFF065F46)
                                                                VerificationStatus.REJECTED -> Color(0xFFDC2626)
                                                                else -> Color(0xFF92400E)
                                                            },
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
                        1 -> {
                            // SKILLS TAB
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "Skill Requests",
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = DevLinkTextSecondary
                                )
                                if (isOwnProfile) {
                                    TextButton(
                                        onClick = { showAddSkillDialog = true },
                                        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp)
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Add,
                                            contentDescription = null,
                                            tint = DevLinkBlue,
                                            modifier = Modifier.size(16.dp)
                                        )
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text("Request Skill", fontSize = 12.sp, color = DevLinkBlue, fontWeight = FontWeight.Bold)
                                    }
                                }
                            }

                            val filteredSkillRequests = skillRequests.filter { req ->
                                when (selectedStatusFilter) {
                                    "APPROVED" -> req.status == SkillStatus.ACCEPTED
                                    "PENDING" -> req.status == SkillStatus.PENDING || req.status == SkillStatus.MEET
                                    "REJECTED" -> req.status == SkillStatus.REJECTED
                                    else -> true
                                }
                            }

                            if (filteredSkillRequests.isEmpty()) {
                                Surface(
                                    modifier = Modifier.fillMaxWidth(),
                                    color = Color(0xFFF8FAFC),
                                    shape = RoundedCornerShape(10.dp),
                                    border = BorderStroke(1.dp, DevLinkBorder)
                                ) {
                                    Column(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(20.dp),
                                        horizontalAlignment = Alignment.CenterHorizontally
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Build,
                                            contentDescription = null,
                                            tint = Color(0xFF94A3B8),
                                            modifier = Modifier.size(32.dp)
                                        )
                                        Spacer(modifier = Modifier.height(6.dp))
                                        Text(
                                            text = if (selectedStatusFilter == "ALL") "No skill requests submitted yet." else "No $selectedStatusFilter skill requests found.",
                                            fontSize = 13.sp,
                                            color = DevLinkTextSecondary
                                        )
                                    }
                                }
                            } else {
                                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                                    filteredSkillRequests.forEach { req ->
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
                                                        Column(modifier = Modifier.padding(10.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                                            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                                                Icon(
                                                                    imageVector = Icons.Default.Event,
                                                                    contentDescription = null,
                                                                    tint = Color(0xFF7C3AED),
                                                                    modifier = Modifier.size(14.dp)
                                                                )
                                                                Text(
                                                                    text = "Meeting Scheduled",
                                                                    fontSize = 12.sp,
                                                                    fontWeight = FontWeight.Bold,
                                                                    color = Color(0xFF5B21B6)
                                                                )
                                                            }
                                                            Text(
                                                                text = formatMeetingDateTime(req.meetingDate),
                                                                fontSize = 12.sp,
                                                                fontWeight = FontWeight.SemiBold,
                                                                color = Color(0xFF6D28D9)
                                                            )
                                                            if (!req.meetingNotes.isNullOrBlank()) {
                                                                Text(
                                                                    text = "Topic: ${req.meetingNotes}",
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
                                                        text = "Requested on ${formatDisplayDate(req.createdAt)}",
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
                        2 -> {
                            // CERTIFICATES TAB
                            val filteredCertificates = certificates.filter { cert ->
                                when (selectedStatusFilter) {
                                    "APPROVED" -> cert.verificationStatus == VerificationStatus.APPROVED
                                    "PENDING" -> cert.verificationStatus == VerificationStatus.PENDING
                                    "REJECTED" -> cert.verificationStatus == VerificationStatus.REJECTED
                                    else -> true
                                }
                            }

                            if (filteredCertificates.isEmpty()) {
                                Surface(
                                    modifier = Modifier.fillMaxWidth(),
                                    color = Color(0xFFF8FAFC),
                                    shape = RoundedCornerShape(10.dp),
                                    border = BorderStroke(1.dp, DevLinkBorder)
                                ) {
                                    Column(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(20.dp),
                                        horizontalAlignment = Alignment.CenterHorizontally
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.CardMembership,
                                            contentDescription = null,
                                            tint = Color(0xFF94A3B8),
                                            modifier = Modifier.size(32.dp)
                                        )
                                        Spacer(modifier = Modifier.height(6.dp))
                                        Text(
                                            text = if (selectedStatusFilter == "ALL") "No certificates uploaded yet." else "No $selectedStatusFilter certificates found.",
                                            fontSize = 13.sp,
                                            color = DevLinkTextSecondary
                                        )
                                    }
                                }
                            } else {
                                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                                    filteredCertificates.forEach { cert ->
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
                                                        color = when (cert.verificationStatus) {
                                                            VerificationStatus.APPROVED -> Color(0xFFD1FAE5)
                                                            VerificationStatus.REJECTED -> Color(0xFFFEE2E2)
                                                            else -> Color(0xFFFEF3C7)
                                                        },
                                                        shape = RoundedCornerShape(6.dp)
                                                    ) {
                                                        Text(
                                                            text = cert.verificationStatus.name,
                                                            fontSize = 10.sp,
                                                            fontWeight = FontWeight.Bold,
                                                            color = when (cert.verificationStatus) {
                                                                VerificationStatus.APPROVED -> Color(0xFF065F46)
                                                                VerificationStatus.REJECTED -> Color(0xFFDC2626)
                                                                else -> Color(0xFF92400E)
                                                            },
                                                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                                        )
                                                    }
                                                }
                                                Spacer(modifier = Modifier.height(2.dp))
                                                Text("Issued By: ${cert.issuingOrg}", fontSize = 12.sp, color = DevLinkTextSecondary, fontWeight = FontWeight.Medium)
                                                if (cert.verificationStatus == VerificationStatus.REJECTED) {
                                                    Spacer(modifier = Modifier.height(6.dp))
                                                    Row(
                                                        verticalAlignment = Alignment.CenterVertically,
                                                        modifier = Modifier
                                                            .fillMaxWidth()
                                                            .background(Color(0xFFFEF2F2), RoundedCornerShape(6.dp))
                                                            .padding(horizontal = 8.dp, vertical = 4.dp)
                                                    ) {
                                                        Icon(
                                                            imageVector = Icons.Default.Cancel,
                                                            contentDescription = null,
                                                            tint = Color(0xFFDC2626),
                                                            modifier = Modifier.size(14.dp)
                                                        )
                                                        Spacer(modifier = Modifier.width(6.dp))
                                                        Text(
                                                            text = if (!cert.teacherRemarks.isNullOrBlank()) "Feedback: ${cert.teacherRemarks}" else "Rejected by faculty • Tap to view details",
                                                            fontSize = 11.sp,
                                                            color = Color(0xFFB91C1C),
                                                            fontWeight = FontWeight.Medium,
                                                            maxLines = 1,
                                                            overflow = TextOverflow.Ellipsis
                                                        )
                                                    }
                                                }
                                                if (cert.verificationStatus == VerificationStatus.NEED_TO_TALK) {
                                                    Spacer(modifier = Modifier.height(6.dp))
                                                    Row(
                                                        verticalAlignment = Alignment.CenterVertically,
                                                        modifier = Modifier
                                                            .fillMaxWidth()
                                                            .background(Color(0xFFEFF6FF), RoundedCornerShape(6.dp))
                                                            .padding(horizontal = 8.dp, vertical = 4.dp)
                                                    ) {
                                                        Icon(
                                                            imageVector = Icons.Default.Chat,
                                                            contentDescription = null,
                                                            tint = Color(0xFF2563EB),
                                                            modifier = Modifier.size(14.dp)
                                                        )
                                                        Spacer(modifier = Modifier.width(6.dp))
                                                        Text(
                                                            text = if (!cert.teacherRemarks.isNullOrBlank()) "Discussion: ${cert.teacherRemarks}" else "Discussion / Viva requested by faculty",
                                                            fontSize = 11.sp,
                                                            color = Color(0xFF1D4ED8),
                                                            fontWeight = FontWeight.Medium,
                                                            maxLines = 1,
                                                            overflow = TextOverflow.Ellipsis
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
                }
            }
        } else {
                // Verified Projects Showcase Section (For Recruiters and Visiting Students)
                val approvedProjects = projects.filter { it.verificationStatus == VerificationStatus.APPROVED }
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
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.FolderOpen,
                                contentDescription = null,
                                tint = DevLinkBlue,
                                modifier = Modifier.size(20.dp)
                            )
                            Text(
                                text = "Verified Projects",
                                fontWeight = FontWeight.Bold,
                                fontSize = 16.sp,
                                color = DevLinkTextPrimary
                            )
                            if (approvedProjects.isNotEmpty()) {
                                Surface(
                                    color = DevLinkBlueContainer,
                                    shape = CircleShape
                                ) {
                                    Text(
                                        text = approvedProjects.size.toString(),
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = DevLinkBlue,
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                                    )
                                }
                            }
                        }

                        if (approvedProjects.isEmpty()) {
                            Surface(
                                modifier = Modifier.fillMaxWidth(),
                                color = Color(0xFFF8FAFC),
                                shape = RoundedCornerShape(10.dp),
                                border = BorderStroke(1.dp, DevLinkBorder)
                            ) {
                                Column(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(20.dp),
                                    horizontalAlignment = Alignment.CenterHorizontally
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.FolderOpen,
                                        contentDescription = null,
                                        tint = Color(0xFF94A3B8),
                                        modifier = Modifier.size(32.dp)
                                    )
                                    Spacer(modifier = Modifier.height(6.dp))
                                    Text(
                                        text = "No verified projects available yet.",
                                        fontSize = 13.sp,
                                        color = DevLinkTextSecondary
                                    )
                                }
                            }
                        } else {
                            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                                approvedProjects.forEach { proj ->
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
                                                Text(
                                                    text = proj.name,
                                                    fontWeight = FontWeight.Bold,
                                                    fontSize = 14.sp,
                                                    color = DevLinkTextPrimary
                                                )
                                                Surface(
                                                    color = Color(0xFFD1FAE5),
                                                    shape = RoundedCornerShape(6.dp)
                                                ) {
                                                    Text(
                                                        text = "Verified",
                                                        fontSize = 10.sp,
                                                        fontWeight = FontWeight.Bold,
                                                        color = Color(0xFF065F46),
                                                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                                    )
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
                                                if (!proj.createdAt.isNullOrBlank()) {
                                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                                        Icon(Icons.Default.AccessTime, contentDescription = null, tint = DevLinkTextSecondary, modifier = Modifier.size(13.dp))
                                                        Spacer(modifier = Modifier.width(3.dp))
                                                        Text(formatDisplayDate(proj.createdAt), fontSize = 11.sp, color = DevLinkTextSecondary)
                                                    }
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }

                // Also display Verified Certifications if available
                val approvedCertificates = certificates.filter { it.verificationStatus == VerificationStatus.APPROVED }
                if (approvedCertificates.isNotEmpty()) {
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
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.WorkspacePremium,
                                    contentDescription = null,
                                    tint = DevLinkBlue,
                                    modifier = Modifier.size(20.dp)
                                )
                                Text(
                                    text = "Verified Certifications",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 16.sp,
                                    color = DevLinkTextPrimary
                                )
                                Surface(
                                    color = DevLinkBlueContainer,
                                    shape = CircleShape
                                ) {
                                    Text(
                                        text = approvedCertificates.size.toString(),
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = DevLinkBlue,
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                                    )
                                }
                            }

                            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                                approvedCertificates.forEach { cert ->
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
                                                Text(
                                                    text = cert.title,
                                                    fontWeight = FontWeight.Bold,
                                                    fontSize = 14.sp,
                                                    color = DevLinkTextPrimary
                                                )
                                                Surface(
                                                    color = Color(0xFFD1FAE5),
                                                    shape = RoundedCornerShape(6.dp)
                                                ) {
                                                    Text(
                                                        text = "Verified",
                                                        fontSize = 10.sp,
                                                        fontWeight = FontWeight.Bold,
                                                        color = Color(0xFF065F46),
                                                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                                    )
                                                }
                                            }
                                            Spacer(modifier = Modifier.height(2.dp))
                                            Text(
                                                text = "Issued By: ${cert.issuingOrg}",
                                                fontSize = 12.sp,
                                                color = DevLinkTextSecondary,
                                                fontWeight = FontWeight.Medium
                                            )
                                        }
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
