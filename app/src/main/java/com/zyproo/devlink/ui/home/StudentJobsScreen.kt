package com.zyproo.devlink.ui.home

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil3.compose.AsyncImage
import com.zyproo.devlink.common.formatRelativeTime
import com.zyproo.devlink.common.toFullMediaUrl
import com.zyproo.devlink.data.remote.dto.*
import com.zyproo.devlink.ui.job.JobViewModel
import com.zyproo.devlink.ui.theme.*

@OptIn(ExperimentalLayoutApi::class, ExperimentalMaterial3Api::class)
@Composable
fun StudentJobsScreen(
    jobViewModel: JobViewModel,
    approvedSkills: List<StudentSkillResponse>,
    projects: List<ProjectResponse>,
    onBackClick: (() -> Unit)? = null
) {
    val state = jobViewModel.state
    
    // Load student applications & open jobs when screen opens
    LaunchedEffect(Unit) {
        jobViewModel.loadOpenJobs()
        jobViewModel.loadStudentApplications()
    }

    var selectedJobForDetails by remember { mutableStateOf<JobResponse?>(null) }
    var activeSubTab by remember { mutableStateOf(0) } // 0 = Open Jobs, 1 = My Applications
    
    // Filters State
    var selectedType by remember { mutableStateOf<JobType?>(null) }
    var locationFilter by remember { mutableStateOf("") }
    var skillFilter by remember { mutableStateOf("") }

    var showApplyDialog by remember { mutableStateOf(false) }
    var applyErrorMsg by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(selectedJobForDetails) {
        applyErrorMsg = null
    }



    if (selectedJobForDetails == null) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(Color(0xFFF6F7FB))
        ) {
            // Sub Tabs Header
            TabRow(
                selectedTabIndex = activeSubTab,
                containerColor = Color.White,
                contentColor = DevLinkBlue
            ) {
                Tab(
                    selected = activeSubTab == 0,
                    onClick = { activeSubTab = 0 },
                    text = { Text("Open Positions") }
                )
                Tab(
                    selected = activeSubTab == 1,
                    onClick = { activeSubTab = 1 },
                    text = { Text("My Applications (${state.studentApplications.size})") }
                )
            }

            if (activeSubTab == 0) {
                // Filters Row
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Color.White)
                        .padding(horizontal = 16.dp, vertical = 8.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    var showTypeMenu by remember { mutableStateOf(false) }
                    
                    Box {
                        FilterChip(
                            selected = selectedType != null,
                            onClick = { showTypeMenu = true },
                            label = { Text(selectedType?.name ?: "Job Type") }
                        )
                        DropdownMenu(expanded = showTypeMenu, onDismissRequest = { showTypeMenu = false }) {
                            DropdownMenuItem(text = { Text("All Types") }, onClick = { selectedType = null; showTypeMenu = false })
                            JobType.values().forEach { type ->
                                DropdownMenuItem(text = { Text(type.name) }, onClick = { selectedType = type; showTypeMenu = false })
                            }
                        }
                    }

                    var showLocationDialog by remember { mutableStateOf(false) }
                    FilterChip(
                        selected = locationFilter.isNotEmpty(),
                        onClick = { showLocationDialog = true },
                        label = { Text(if (locationFilter.isEmpty()) "Location" else locationFilter) }
                    )

                    if (showLocationDialog) {
                        var tempLoc by remember { mutableStateOf(locationFilter) }
                        AlertDialog(
                            onDismissRequest = { showLocationDialog = false },
                            containerColor = Color.White,
                            shape = RoundedCornerShape(20.dp),
                            confirmButton = {
                                Button(
                                    onClick = { locationFilter = tempLoc; showLocationDialog = false },
                                    colors = ButtonDefaults.buttonColors(containerColor = DevLinkBlue)
                                ) {
                                    Text("Apply", color = Color.White)
                                }
                            },
                            dismissButton = {
                                TextButton(onClick = { locationFilter = ""; showLocationDialog = false }) {
                                    Text("Clear", color = Color.Gray)
                                }
                            },
                            title = { Text("Filter Location", fontWeight = FontWeight.Bold, color = DevLinkTextPrimary) },
                            text = {
                                OutlinedTextField(
                                    value = tempLoc,
                                    onValueChange = { tempLoc = it },
                                    label = { Text("e.g. Remote or Bangalore") },
                                    shape = RoundedCornerShape(10.dp),
                                    colors = OutlinedTextFieldDefaults.colors(
                                        focusedBorderColor = DevLinkBlue,
                                        unfocusedBorderColor = DevLinkBorder
                                    )
                                )
                            }
                        )
                    }

                    var showSkillDialog by remember { mutableStateOf(false) }
                    FilterChip(
                        selected = skillFilter.isNotEmpty(),
                        onClick = { showSkillDialog = true },
                        label = { Text(if (skillFilter.isEmpty()) "Required Skill" else skillFilter) }
                    )

                    if (showSkillDialog) {
                        var tempSkill by remember { mutableStateOf(skillFilter) }
                        AlertDialog(
                            onDismissRequest = { showSkillDialog = false },
                            containerColor = Color.White,
                            shape = RoundedCornerShape(20.dp),
                            confirmButton = {
                                Button(
                                    onClick = { skillFilter = tempSkill; showSkillDialog = false },
                                    colors = ButtonDefaults.buttonColors(containerColor = DevLinkBlue)
                                ) {
                                    Text("Apply", color = Color.White)
                                }
                            },
                            dismissButton = {
                                TextButton(onClick = { skillFilter = ""; showSkillDialog = false }) {
                                    Text("Clear", color = Color.Gray)
                                }
                            },
                            title = { Text("Filter Skill", fontWeight = FontWeight.Bold, color = DevLinkTextPrimary) },
                            text = {
                                OutlinedTextField(
                                    value = tempSkill,
                                    onValueChange = { tempSkill = it },
                                    label = { Text("e.g. Kotlin") },
                                    shape = RoundedCornerShape(10.dp),
                                    colors = OutlinedTextFieldDefaults.colors(
                                        focusedBorderColor = DevLinkBlue,
                                        unfocusedBorderColor = DevLinkBorder
                                    )
                                )
                            }
                        )
                    }
                }

                // Filter Logic
                val filteredJobs = state.openJobs.filter { job ->
                    val matchesType = selectedType == null || job.jobType == selectedType
                    val matchesLoc = locationFilter.isEmpty() || job.location.contains(locationFilter, ignoreCase = true)
                    val matchesSkill = skillFilter.isEmpty() || job.requiredSkills.any { it.contains(skillFilter, ignoreCase = true) }
                    matchesType && matchesLoc && matchesSkill
                }

                if (filteredJobs.isEmpty()) {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Text("No open positions match your search filters.", color = Color.Gray)
                    }
                } else {
                    LazyColumn(
                        contentPadding = PaddingValues(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp),
                        modifier = Modifier.fillMaxSize()
                    ) {
                        items(filteredJobs) { job ->
                            // Check for skill matches
                            val isSkillMatched = job.requiredSkills.any { reqSkill ->
                                approvedSkills.any { it.skillName.equals(reqSkill, ignoreCase = true) }
                            }

                            StudentJobItem(
                                job = job,
                                isSkillMatched = isSkillMatched,
                                onClick = { selectedJobForDetails = job }
                            )
                        }
                    }
                }
            } else {
                // My Applications List
                if (state.studentApplications.isEmpty()) {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Text("You haven't applied to any jobs yet.", color = Color.Gray)
                    }
                } else {
                    LazyColumn(
                        contentPadding = PaddingValues(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp),
                        modifier = Modifier.fillMaxSize()
                    ) {
                        items(state.studentApplications) { app ->
                            StudentApplicationItemCard(
                                application = app,
                                onClick = {
                                    val jobForDetails = state.openJobs.find { it.id == app.jobId }
                                    if (jobForDetails != null) {
                                        selectedJobForDetails = jobForDetails
                                    } else {
                                        // Mock/create a JobResponse so details screen still opens
                                        selectedJobForDetails = JobResponse(
                                            id = app.jobId,
                                            recruiterId = 0L,
                                            recruiterCompanyName = app.recruiterCompanyName,
                                            recruiterCompanyLogo = null,
                                            title = app.jobTitle,
                                            description = "Application is currently in ${app.status.name} state.",
                                            jobType = JobType.INTERNSHIP,
                                            requiredSkills = emptyList(),
                                            location = "N/A",
                                            salaryRange = null,
                                            deadline = "N/A",
                                            status = JobStatus.OPEN,
                                            createdAt = "2026-08-26T12:00:00Z",
                                            applicantCount = 1,
                                            hasApplied = true
                                        )
                                    }
                                }
                            )
                        }
                    }
                }
            }
        }
    } else {
        // Job Details View
        val job = selectedJobForDetails!!
        val hasApplied = state.studentApplications.any { it.jobId == job.id }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(Color(0xFFF6F7FB))
        ) {
            // Header Row with Recruiter Logo
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color.White)
                    .padding(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = { selectedJobForDetails = null }) {
                    Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                }
                Spacer(modifier = Modifier.width(8.dp))

                val logoUrl = job.recruiterCompanyLogo.toFullMediaUrl()
                Surface(
                    shape = CircleShape,
                    color = DevLinkBlueContainer,
                    modifier = Modifier.size(40.dp),
                    border = BorderStroke(1.dp, DevLinkBorder)
                ) {
                    if (!logoUrl.isNullOrBlank()) {
                        AsyncImage(
                            model = logoUrl,
                            contentDescription = job.recruiterCompanyName,
                            contentScale = ContentScale.Crop,
                            modifier = Modifier.fillMaxSize()
                        )
                    } else {
                        Box(contentAlignment = Alignment.Center) {
                            Text(
                                text = job.recruiterCompanyName.take(1).uppercase(),
                                fontWeight = FontWeight.Bold,
                                fontSize = 16.sp,
                                color = DevLinkBlue
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.width(12.dp))

                Column {
                    Text(text = job.title, fontWeight = FontWeight.Bold, fontSize = 16.sp, color = DevLinkTextPrimary)
                    Text(text = job.recruiterCompanyName, fontSize = 13.sp, color = DevLinkTextSecondary, fontWeight = FontWeight.SemiBold)
                }
            }

            Column(
                modifier = Modifier
                    .weight(1f)
                    .verticalScroll(rememberScrollState())
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Job Details Info Card
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    border = BorderStroke(1.dp, DevLinkBorder)
                ) {
                    Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Business, contentDescription = null, tint = DevLinkBlue, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(text = job.recruiterCompanyName, fontSize = 14.sp, fontWeight = FontWeight.Bold)
                        }
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.LocationOn, contentDescription = null, tint = DevLinkBlue, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(text = "${job.location} (${job.jobType})", fontSize = 13.sp, color = DevLinkTextSecondary)
                        }
                        if (!job.salaryRange.isNullOrBlank()) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.Payment, contentDescription = null, tint = DevLinkBlue, modifier = Modifier.size(18.dp))
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(text = job.salaryRange ?: "", fontSize = 13.sp, color = DevLinkTextSecondary)
                            }
                        }
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.DateRange, contentDescription = null, tint = DevLinkBlue, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(text = "Apply before: ${job.deadline.take(10)}", fontSize = 13.sp, color = DevLinkTextSecondary)
                        }
                        val timeAgo = formatRelativeTime(job.createdAt)
                        if (timeAgo.isNotEmpty()) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.Schedule, contentDescription = null, tint = DevLinkBlue, modifier = Modifier.size(18.dp))
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(text = "Uploaded: $timeAgo", fontSize = 13.sp, color = DevLinkTextSecondary)
                            }
                        }
                    }
                }

                // Background Skill Eligibility Calculation
                val matchingSkillsCount = job.requiredSkills.count { reqSkill ->
                    approvedSkills.any { it.skillName.equals(reqSkill, ignoreCase = true) }
                }
                val minRequiredSkills = if (job.requiredSkills.size >= 3) 3 else job.requiredSkills.size.coerceAtMost(1)
                val isEligibleToApply = job.requiredSkills.isEmpty() || matchingSkillsCount >= minRequiredSkills

                // Description Card
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(text = "Job Description", fontWeight = FontWeight.Bold, fontSize = 14.sp, color = DevLinkTextPrimary)
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(text = job.description, fontSize = 13.sp, color = DevLinkTextSecondary, lineHeight = 18.sp)
                    }
                }

                // Required Skills Card
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(text = "Required Skills", fontWeight = FontWeight.Bold, fontSize = 14.sp, color = DevLinkTextPrimary)
                        Spacer(modifier = Modifier.height(12.dp))
                        FlowRow(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            job.requiredSkills.forEach { skill ->
                                val isMatched = approvedSkills.any { it.skillName.equals(skill, ignoreCase = true) }
                                Surface(
                                    color = if (isMatched) Color(0xFFDCFCE7) else Color(0xFFF1F5F9),
                                    shape = RoundedCornerShape(6.dp),
                                    border = BorderStroke(1.dp, if (isMatched) Color(0xFF86EFAC) else Color.Transparent)
                                ) {
                                    Row(
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                                    ) {
                                        if (isMatched) {
                                            Icon(Icons.Default.Check, contentDescription = null, tint = Color(0xFF15803D), modifier = Modifier.size(12.dp))
                                        }
                                        Text(
                                            text = skill,
                                            fontSize = 11.sp,
                                            color = if (isMatched) Color(0xFF15803D) else Color.Gray,
                                            fontWeight = FontWeight.Bold
                                        )
                                    }
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                // Apply Button
                if (hasApplied) {
                    Button(
                        onClick = {},
                        enabled = false,
                        colors = ButtonDefaults.buttonColors(containerColor = Color.Gray),
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Icon(Icons.Default.Check, contentDescription = null, tint = Color.White)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Applied", color = Color.White, fontWeight = FontWeight.Bold)
                    }
                } else {
                    Button(
                        onClick = {
                            if (isEligibleToApply) {
                                applyErrorMsg = null
                                showApplyDialog = true
                            } else {
                                applyErrorMsg = "You are not eligible to apply for this position. Minimum $minRequiredSkills matching approved skill(s) required on your profile (you have $matchingSkillsCount/${job.requiredSkills.size})."
                            }
                        },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = DevLinkBlue
                        ),
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Send,
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Apply to Position",
                            color = Color.White,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    if (applyErrorMsg != null) {
                        Spacer(modifier = Modifier.height(10.dp))
                        Surface(
                            modifier = Modifier.fillMaxWidth(),
                            color = Color(0xFFFEF2F2),
                            shape = RoundedCornerShape(8.dp),
                            border = BorderStroke(1.dp, Color(0xFFFCA5A5))
                        ) {
                            Row(
                                modifier = Modifier.padding(12.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.Default.ErrorOutline,
                                    contentDescription = null,
                                    tint = Color(0xFFDC2626),
                                    modifier = Modifier.size(18.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = applyErrorMsg!!,
                                    color = Color(0xFF991B1B),
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Medium
                                )
                            }
                        }
                    }
                }
            }
        }
    }


    // Apply interactive dialog
    if (showApplyDialog) {
        var note by remember { mutableStateOf("") }
        val selectedProjects = remember { mutableStateMapOf<Long, Boolean>() }
        val selectedSkills = remember { mutableStateMapOf<Long, Boolean>() }

        AlertDialog(
            onDismissRequest = { showApplyDialog = false },
            confirmButton = {
                Button(
                    onClick = {
                        selectedJobForDetails?.let { job ->
                            jobViewModel.applyToJob(job.id) {
                                showApplyDialog = false
                            }
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = DevLinkBlue)
                ) {
                    Text("Submit Application", color = Color.White)
                }
            },
            dismissButton = {
                TextButton(onClick = { showApplyDialog = false }) {
                    Text("Cancel", color = Color.Gray)
                }
            },
            title = { Text("Apply for Role", fontSize = 18.sp, fontWeight = FontWeight.Bold) },
            text = {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Text("Add a short cover message or highlight your verified achievements:", fontSize = 12.sp, color = DevLinkTextSecondary)
                    
                    OutlinedTextField(
                        value = note,
                        onValueChange = { note = it },
                        label = { Text("Cover message (optional)") },
                        maxLines = 3,
                        modifier = Modifier.fillMaxWidth()
                    )

                    // Highlight Projects Checklist
                    if (projects.isNotEmpty()) {
                        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            Text("Highlight Relevant Projects", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = DevLinkTextPrimary)
                            projects.forEach { proj ->
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clickable { selectedProjects[proj.id] = !(selectedProjects[proj.id] ?: false) }
                                        .padding(vertical = 4.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Checkbox(
                                        checked = selectedProjects[proj.id] ?: false,
                                        onCheckedChange = { selectedProjects[proj.id] = it }
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(proj.name, fontSize = 13.sp, color = DevLinkTextPrimary)
                                }
                            }
                        }
                    }

                    // Highlight Skills Checklist
                    if (approvedSkills.isNotEmpty()) {
                        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            Text("Highlight Relevant Skills", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = DevLinkTextPrimary)
                            approvedSkills.forEach { skill ->
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clickable { selectedSkills[skill.id] = !(selectedSkills[skill.id] ?: false) }
                                        .padding(vertical = 4.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Checkbox(
                                        checked = selectedSkills[skill.id] ?: false,
                                        onCheckedChange = { selectedSkills[skill.id] = it }
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(skill.skillName, fontSize = 13.sp, color = DevLinkTextPrimary)
                                }
                            }
                        }
                    }
                }
            }
        )
    }
}

@OptIn(ExperimentalLayoutApi::class, ExperimentalMaterial3Api::class)
@Composable
fun StudentJobItem(
    job: JobResponse,
    isSkillMatched: Boolean,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        border = BorderStroke(
            width = if (isSkillMatched) 2.dp else 1.dp,
            color = if (isSkillMatched) DevLinkBlue else DevLinkBorder
        )
        
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Recruiter Profile Icon / Company Logo Avatar
                val logoUrl = job.recruiterCompanyLogo.toFullMediaUrl()
                Surface(
                    shape = CircleShape,
                    color = DevLinkBlueContainer,
                    modifier = Modifier.size(42.dp),
                    border = BorderStroke(1.dp, DevLinkBorder)
                ) {
                    if (!logoUrl.isNullOrBlank()) {
                        AsyncImage(
                            model = logoUrl,
                            contentDescription = job.recruiterCompanyName,
                            contentScale = ContentScale.Crop,
                            modifier = Modifier.fillMaxSize()
                        )
                    } else {
                        Box(contentAlignment = Alignment.Center) {
                            Text(
                                text = job.recruiterCompanyName.take(1).uppercase(),
                                fontWeight = FontWeight.Bold,
                                fontSize = 16.sp,
                                color = DevLinkBlue
                            )
                        }
                    }
                }

                Column(modifier = Modifier.weight(1f)) {
                    Text(text = job.title, fontWeight = FontWeight.Bold, fontSize = 15.sp, color = DevLinkTextPrimary)
                    Text(text = job.recruiterCompanyName, fontSize = 12.sp, color = DevLinkTextSecondary, fontWeight = FontWeight.Medium)
                }
                
                if (isSkillMatched) {
                    Surface(
                        color = DevLinkBlueContainer,
                        shape = RoundedCornerShape(4.dp)
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Icon(Icons.Default.Star, contentDescription = null, tint = DevLinkBlue, modifier = Modifier.size(10.dp))
                            Text(text = "Skills Match", fontSize = 9.sp, color = DevLinkBlue, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(text = "${job.jobType} · ${job.location}", fontSize = 12.sp, color = DevLinkTextSecondary)

                val timeAgo = formatRelativeTime(job.createdAt)
                if (timeAgo.isNotEmpty()) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Schedule,
                            contentDescription = "Uploaded time",
                            tint = DevLinkTextSecondary,
                            modifier = Modifier.size(12.dp)
                        )
                        Text(
                            text = timeAgo,
                            fontSize = 11.sp,
                            color = DevLinkTextSecondary,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
            }

            if (job.requiredSkills.isNotEmpty()) {

                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {

                    job.requiredSkills.forEach { skill ->
                        Surface(
                            color = Color(0xFFF1F5F9),
                            shape = RoundedCornerShape(4.dp)
                        ) {
                            Text(
                                text = skill,
                                fontSize = 10.sp,
                                color = Color.Gray,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}


@Composable
fun StudentApplicationItemCard(
    application: JobApplicationResponse,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        border = BorderStroke(1.dp, DevLinkBorder)
    ) {
        Row(
            modifier = Modifier.padding(16.dp).fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(text = application.jobTitle, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                Text(text = application.recruiterCompanyName, fontSize = 12.sp, color = Color.Gray)
            }
            Surface(
                color = when (application.status) {
                    JobApplicationStatus.SHORTLISTED -> Color(0xFFDCFCE7)
                    JobApplicationStatus.REJECTED -> Color(0xFFFEE2E2)
                    else -> Color(0xFFFEF9C3)
                },
                shape = RoundedCornerShape(4.dp)
            ) {
                Text(
                    text = application.status.name,
                    fontSize = 9.sp,
                    fontWeight = FontWeight.Bold,
                    color = when (application.status) {
                        JobApplicationStatus.SHORTLISTED -> Color(0xFF15803D)
                        JobApplicationStatus.REJECTED -> Color(0xFFB91C1C)
                        else -> Color(0xFF854D0E)
                    },
                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                )
            }
        }
    }
}
