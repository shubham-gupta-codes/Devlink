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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil3.compose.AsyncImage
import com.zyproo.devlink.common.toFullMediaUrl
import com.zyproo.devlink.data.remote.dto.*
import com.zyproo.devlink.ui.job.JobViewModel
import com.zyproo.devlink.ui.job.JobUiState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import com.zyproo.devlink.ui.common.MeetingSchedulePicker
import com.zyproo.devlink.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RecruiterFlow(
    activeTab: Int,
    profile: ProfileResponse?,
    searchResults: List<ProfileResponse>,
    isSearching: Boolean,
    searchError: String?,
    onSearch: (String?, Course?, Branch?, Int?, Int?, String?, String?) -> Unit,
    onUserClick: (ProfileResponse) -> Unit,
    onEditProfile: () -> Unit,
    jobViewModel: JobViewModel,
    onStartDirectChat: (String) -> Unit,
    onLogoutClick: (() -> Unit)? = null
) {
    val jobState = jobViewModel.state
    
    // Load recruiter jobs when screen opens
    LaunchedEffect(Unit) {
        jobViewModel.loadRecruiterJobs()
        jobViewModel.loadSavedCandidates()
    }

    var selectedJobForDetails by remember { mutableStateOf<JobResponse?>(null) }
    var showPostJobDialog by remember { mutableStateOf(false) }
    var jobToEdit by remember { mutableStateOf<JobResponse?>(null) }
    var jobToDelete by remember { mutableStateOf<JobResponse?>(null) }
    var studentForShortlistDialog by remember { mutableStateOf<ProfileResponse?>(null) }
    var showSavedCandidatesList by remember { mutableStateOf(false) }

    when (activeTab) {
        0 -> { // Recruiter Home Screen (Search View + Saved Candidates Toggle)
            Column(modifier = Modifier.fillMaxSize()) {
                // Toggle row for search vs saved shortlist
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Color.White)
                        .padding(horizontal = 16.dp, vertical = 8.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    FilterChip(
                        selected = !showSavedCandidatesList,
                        onClick = { showSavedCandidatesList = false },
                        label = { Text("Find Talent") },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = DevLinkBlue,
                            selectedLabelColor = Color.White
                        )
                    )
                    FilterChip(
                        selected = showSavedCandidatesList,
                        onClick = { showSavedCandidatesList = true },
                        label = { Text("Saved shortlists") },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = DevLinkBlue,
                            selectedLabelColor = Color.White
                        )
                    )
                }

                if (showSavedCandidatesList) {
                    // Grouped by Job Role Shortlist View
                    SavedCandidatesGroupedScreen(
                        savedCandidates = jobState.savedCandidates,
                        recruiterJobs = jobState.recruiterJobs,
                        onUserClick = onUserClick,
                        onMessageClick = onStartDirectChat
                    )
                } else {
                    StudentSearchScreen(
                        searchResults = searchResults,
                        isSearching = isSearching,
                        searchError = searchError,
                        onSearch = onSearch,
                        onUserClick = onUserClick,
                        recruiterMode = true,
                        onShortlistClick = { studentForShortlistDialog = it },
                        onMessageClick = { onStartDirectChat(it.username) }
                    )
                }
            }
        }
        1 -> { // Jobs Tab
            if (selectedJobForDetails == null) {
                // Job Board List View
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Color(0xFFF6F7FB))
                ) {
                    if (jobState.recruiterJobs.isEmpty()) {
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(24.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(12.dp),
                                colors = CardDefaults.cardColors(containerColor = Color.White)
                            ) {
                                Column(
                                    modifier = Modifier.padding(24.dp),
                                    horizontalAlignment = Alignment.CenterHorizontally
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.WorkOutline,
                                        contentDescription = null,
                                        tint = DevLinkBlue,
                                        modifier = Modifier.size(48.dp)
                                    )
                                    Spacer(modifier = Modifier.height(16.dp))
                                    Text(
                                        text = "No Job Postings Yet",
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 16.sp,
                                        color = DevLinkTextPrimary
                                    )
                                    Spacer(modifier = Modifier.height(8.dp))
                                    Text(
                                        text = "You haven't posted any jobs yet — tap + to create one.",
                                        color = DevLinkTextSecondary,
                                        fontSize = 13.sp,
                                        textAlign = TextAlign.Center
                                    )
                                }
                            }
                        }
                    } else {
                        LazyColumn(
                            contentPadding = PaddingValues(16.dp),
                            verticalArrangement = Arrangement.spacedBy(12.dp),
                            modifier = Modifier.fillMaxSize()
                        ) {
                            items(jobState.recruiterJobs) { job ->
                                JobListItem(
                                    job = job,
                                    onClick = {
                                        selectedJobForDetails = job
                                        jobViewModel.loadJobApplications(job.id)
                                        jobViewModel.loadRecommendedCandidates(job.id)
                                    },
                                    onEdit = { jobToEdit = job },
                                    onDelete = { jobToDelete = job }
                                )
                            }
                        }
                    }

                    FloatingActionButton(
                        onClick = { showPostJobDialog = true },
                        containerColor = DevLinkBlue,
                        contentColor = Color.White,
                        shape = CircleShape,
                        modifier = Modifier
                            .align(Alignment.BottomEnd)
                            .padding(16.dp)
                            .padding(bottom = 70.dp)
                    ) {
                        Icon(Icons.Default.Add, contentDescription = "Post a Job")
                    }
                }
            } else {
                // Job Details & Applicant Dashboard View
                JobDetailDashboard(
                    job = selectedJobForDetails!!,
                    jobState = jobState,
                    jobViewModel = jobViewModel,
                    onBackClick = { selectedJobForDetails = null },
                    onUserClick = onUserClick,
                    onMessageClick = onStartDirectChat,
                    onEditJob = { jobToEdit = selectedJobForDetails },
                    onDeleteJob = { jobToDelete = selectedJobForDetails }
                )
            }
        }
        4 -> { // Recruiter Profile View (Premium & Polished)
            val activeJobsCount = jobState.recruiterJobs.filter { it.status == JobStatus.OPEN }.size
            val shortlistedCount = jobState.savedCandidates.map { it.studentProfile.userId }.distinct().size

            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color(0xFFF6F7FB))
            ) {
                item {
                    // Header banner card
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(120.dp)
                            .background(
                                Brush.horizontalGradient(
                                    listOf(DevLinkBlue, DevLinkBlueLight)
                                )
                            )
                    )
                }

                item {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        // Company logo centered, overlapping the header
                        Card(
                            shape = CircleShape,
                            modifier = Modifier
                                .size(80.dp)
                                .offset(y = (-40).dp),
                            colors = CardDefaults.cardColors(containerColor = Color.White),
                            elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
                        ) {
                            Box(
                                modifier = Modifier.fillMaxSize(),
                                contentAlignment = Alignment.Center
                            ) {
                                val companyLogo = profile?.companyLogo?.toFullMediaUrl()
                                if (!companyLogo.isNullOrBlank()) {
                                    AsyncImage(
                                        model = companyLogo,
                                        contentDescription = "Company Logo",
                                        contentScale = ContentScale.Crop,
                                        modifier = Modifier.fillMaxSize()
                                    )
                                } else {
                                    Text(
                                        text = (profile?.companyName ?: "CO").split(" ").filter { it.isNotEmpty() }
                                            .take(2).map { it.first().uppercase() }.joinToString(""),
                                        fontWeight = FontWeight.Bold,
                                        color = DevLinkBlue,
                                        fontSize = 24.sp
                                    )
                                }
                            }
                        }

                        Column(
                            modifier = Modifier
                                .offset(y = (-20).dp)
                                .fillMaxWidth(),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.Center
                            ) {
                                Text(
                                    text = profile?.companyName ?: "Company Name",
                                    fontSize = 20.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.Black
                                )
                                if (profile?.verified == true) {
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Surface(
                                        color = Color(0xFFE0F2FE),
                                        shape = RoundedCornerShape(4.dp)
                                    ) {
                                        Text(
                                            text = "Verified",
                                            fontSize = 9.sp,
                                            color = DevLinkBlue,
                                            fontWeight = FontWeight.Bold,
                                            modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp)
                                        )
                                    }
                                }
                            }
                            Text(
                                text = profile?.designation ?: "Recruiter",
                                fontSize = 14.sp,
                                color = Color.Gray,
                                fontWeight = FontWeight.Medium
                            )
                            Spacer(modifier = Modifier.height(16.dp))

                            // Stats Row
                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(12.dp),
                                colors = CardDefaults.cardColors(containerColor = Color.White),
                                border = BorderStroke(1.dp, DevLinkBorder)
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(16.dp),
                                    horizontalArrangement = Arrangement.SpaceAround
                                ) {
                                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                        Text(
                                            text = activeJobsCount.toString(),
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 18.sp,
                                            color = DevLinkBlue
                                        )
                                        Text(
                                            text = "Active Jobs",
                                            fontSize = 12.sp,
                                            color = Color.Gray
                                        )
                                    }
                                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                        Text(
                                            text = shortlistedCount.toString(),
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 18.sp,
                                            color = DevLinkBlue
                                        )
                                        Text(
                                            text = "Shortlisted",
                                            fontSize = 12.sp,
                                            color = Color.Gray
                                        )
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.height(16.dp))

                            // About Company Box
                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(12.dp),
                                colors = CardDefaults.cardColors(containerColor = Color.White)
                            ) {
                                Column(modifier = Modifier.padding(16.dp)) {
                                    Text(
                                        text = "About Company",
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 14.sp,
                                        color = DevLinkTextPrimary
                                    )
                                    Spacer(modifier = Modifier.height(8.dp))
                                    Text(
                                        text = profile?.about ?: "No company description provided.",
                                        fontSize = 13.sp,
                                        color = if (profile?.about.isNullOrBlank()) Color.Gray else DevLinkTextPrimary,
                                        lineHeight = 18.sp
                                    )
                                    if (!profile?.website.isNullOrBlank()) {
                                        Spacer(modifier = Modifier.height(12.dp))
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Icon(Icons.Default.Language, contentDescription = null, tint = DevLinkBlue, modifier = Modifier.size(16.dp))
                                            Spacer(modifier = Modifier.width(8.dp))
                                            Text(
                                                text = profile.website ?: "",
                                                color = DevLinkBlue,
                                                fontSize = 13.sp,
                                                fontWeight = FontWeight.SemiBold
                                            )
                                        }
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.height(16.dp))

                            // Recruiter Details Box
                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(12.dp),
                                colors = CardDefaults.cardColors(containerColor = Color.White)
                            ) {
                                Column(
                                    modifier = Modifier.padding(16.dp),
                                    verticalArrangement = Arrangement.spacedBy(12.dp)
                                ) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Icon(Icons.Default.Person, contentDescription = null, tint = DevLinkBlue, modifier = Modifier.size(20.dp))
                                        Spacer(modifier = Modifier.width(12.dp))
                                        Text(text = "@${profile?.username}", fontSize = 14.sp, fontWeight = FontWeight.SemiBold, color = DevLinkTextPrimary)
                                    }
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Icon(Icons.Default.Email, contentDescription = null, tint = DevLinkBlue, modifier = Modifier.size(20.dp))
                                        Spacer(modifier = Modifier.width(12.dp))
                                        Text(text = profile?.email ?: "", fontSize = 14.sp, color = DevLinkTextPrimary)
                                    }
                                    if (!profile?.contact.isNullOrBlank()) {
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Icon(Icons.Default.Phone, contentDescription = null, tint = DevLinkBlue, modifier = Modifier.size(20.dp))
                                            Spacer(modifier = Modifier.width(12.dp))
                                            Text(text = profile.contact ?: "", fontSize = 14.sp, color = DevLinkTextPrimary)
                                        }
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.height(24.dp))

                            Button(
                                onClick = onEditProfile,
                                colors = ButtonDefaults.buttonColors(containerColor = DevLinkBlue),
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(8.dp)
                            ) {
                                Text("Edit Profile", color = Color.White, fontWeight = FontWeight.Bold)
                            }

                            if (onLogoutClick != null) {
                                Spacer(modifier = Modifier.height(12.dp))
                                OutlinedButton(
                                    onClick = onLogoutClick,
                                    colors = ButtonDefaults.outlinedButtonColors(contentColor = Color(0xFFEF4444)),
                                    border = BorderStroke(1.dp, Color(0xFFEF4444)),
                                    modifier = Modifier.fillMaxWidth(),
                                    shape = RoundedCornerShape(8.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.ExitToApp,
                                        contentDescription = "Sign Out",
                                        tint = Color(0xFFEF4444),
                                        modifier = Modifier.size(18.dp)
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text("Sign Out", fontWeight = FontWeight.Bold, color = Color(0xFFEF4444))
                                }
                            }

                            Spacer(modifier = Modifier.height(48.dp))
                        }
                    }
                }
            }
        }
    }

    // Shortlist dropdown Dialog
    studentForShortlistDialog?.let { student ->
        AlertDialog(
            onDismissRequest = { studentForShortlistDialog = null },
            confirmButton = {},
            dismissButton = {
                TextButton(onClick = { studentForShortlistDialog = null }) {
                    Text("Cancel", color = Color.Gray)
                }
            },
            title = { Text("Shortlist Candidate", fontSize = 16.sp, fontWeight = FontWeight.Bold) },
            text = {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = "Select a job role to assign ${student.name ?: student.username} to:",
                        fontSize = 13.sp,
                        color = DevLinkTextSecondary
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    
                    // Option for General Interest / Unassigned
                    Surface(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                jobViewModel.shortlistCandidate(student.userId, null)
                                studentForShortlistDialog = null
                            }
                            .padding(vertical = 12.dp, horizontal = 8.dp),
                        shape = RoundedCornerShape(8.dp),
                        color = Color(0xFFF1F5FE)
                    ) {
                        Text(
                            text = "General Interest (Unassigned)",
                            color = DevLinkBlue,
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp,
                            modifier = Modifier.padding(8.dp)
                        )
                    }

                    // Display open jobs
                    jobState.recruiterJobs.forEach { job ->
                        Surface(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    jobViewModel.shortlistCandidate(student.userId, job.id)
                                    studentForShortlistDialog = null
                                }
                                .padding(vertical = 4.dp),
                            shape = RoundedCornerShape(8.dp),
                            color = Color(0xFFF6F7FB)
                        ) {
                            Text(
                                text = job.title,
                                color = DevLinkTextPrimary,
                                fontSize = 14.sp,
                                modifier = Modifier.padding(12.dp)
                            )
                        }
                    }
                }
            }
        )
    }

    // Post a Job Form dialog
    if (showPostJobDialog) {
        var title by remember { mutableStateOf("") }
        var description by remember { mutableStateOf("") }
        var jobType by remember { mutableStateOf(JobType.INTERNSHIP) }
        var skillsStr by remember { mutableStateOf("") }
        var location by remember { mutableStateOf("") }
        var salaryRange by remember { mutableStateOf("") }
        var startDateIso by remember { mutableStateOf("") }
        var endDateIso by remember { mutableStateOf("") }

        AlertDialog(
            onDismissRequest = { showPostJobDialog = false },
            containerColor = Color.White,
            shape = RoundedCornerShape(20.dp),
            confirmButton = {
                Button(
                    onClick = {
                        if (title.isNotBlank() && description.isNotBlank() && location.isNotBlank()) {
                            val skillsList = skillsStr.split(",").map { it.trim() }.filter { it.isNotBlank() }
                            val resolvedDeadline = endDateIso.ifBlank { "2026-12-31T23:59:59Z" }
                            val resolvedStartDate = startDateIso.ifBlank { null }
                            jobViewModel.createJob(
                                title = title,
                                description = description,
                                jobType = jobType,
                                requiredSkills = skillsList,
                                location = location,
                                salaryRange = salaryRange.takeIf { it.isNotBlank() },
                                deadline = resolvedDeadline,
                                startDate = resolvedStartDate
                            )
                            showPostJobDialog = false
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = DevLinkBlue)
                ) {
                    Text("Post Job", color = Color.White)
                }
            },
            dismissButton = {
                TextButton(onClick = { showPostJobDialog = false }) {
                    Text("Cancel", color = Color.Gray)
                }
            },
            title = { Text("Post a New Job", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = DevLinkTextPrimary) },
            text = {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    OutlinedTextField(
                        value = title,
                        onValueChange = { title = it },
                        label = { Text("Job Title *") },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(10.dp)
                    )
                    OutlinedTextField(
                        value = description,
                        onValueChange = { description = it },
                        label = { Text("Description *") },
                        maxLines = 3,
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(10.dp)
                    )
                    
                    // Job Type Selection
                    Column {
                        Text("Job Type", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = DevLinkTextSecondary)
                        Spacer(modifier = Modifier.height(4.dp))
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            JobType.values().forEach { type ->
                                FilterChip(
                                    selected = jobType == type,
                                    onClick = { jobType = type },
                                    label = { Text(type.name, fontSize = 10.sp) },
                                    colors = FilterChipDefaults.filterChipColors(
                                        selectedContainerColor = DevLinkBlue,
                                        selectedLabelColor = Color.White
                                    )
                                )
                            }
                        }
                    }

                    OutlinedTextField(
                        value = skillsStr,
                        onValueChange = { skillsStr = it },
                        label = { Text("Required Skills (comma separated)") },
                        placeholder = { Text("Kotlin, Compose, Retrofit") },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(10.dp)
                    )
                    OutlinedTextField(
                        value = location,
                        onValueChange = { location = it },
                        label = { Text("Location (or 'Remote') *") },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(10.dp)
                    )
                    OutlinedTextField(
                        value = salaryRange,
                        onValueChange = { salaryRange = it },
                        label = { Text("Stipend / Salary range (optional)") },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(10.dp)
                    )

                    // Active visibility dates section
                    Surface(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        color = Color(0xFFF8FAFC),
                        border = BorderStroke(1.dp, DevLinkBorder)
                    ) {
                        Column(
                            modifier = Modifier.padding(12.dp),
                            verticalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Text(
                                text = "Job Visibility Window",
                                fontWeight = FontWeight.Bold,
                                fontSize = 13.sp,
                                color = DevLinkBlue
                            )
                            Text(
                                text = "Visible to students starting from:",
                                fontSize = 11.sp,
                                color = DevLinkTextSecondary
                            )
                            MeetingSchedulePicker(
                                initialIsoString = startDateIso,
                                onIsoStringChange = { startDateIso = it }
                            )

                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "Until application deadline (end date):",
                                fontSize = 11.sp,
                                color = DevLinkTextSecondary
                            )
                            MeetingSchedulePicker(
                                initialIsoString = endDateIso,
                                onIsoStringChange = { endDateIso = it }
                            )
                        }
                    }
                }
            }
        )
    }

    // Edit Job Form dialog
    jobToEdit?.let { currentJob ->
        var editTitle by remember { mutableStateOf(currentJob.title) }
        var editDescription by remember { mutableStateOf(currentJob.description) }
        var editJobType by remember { mutableStateOf(currentJob.jobType) }
        var editSkillsStr by remember { mutableStateOf(currentJob.requiredSkills.joinToString(", ")) }
        var editLocation by remember { mutableStateOf(currentJob.location) }
        var editSalaryRange by remember { mutableStateOf(currentJob.salaryRange ?: "") }
        var editStartDateIso by remember { mutableStateOf(currentJob.startDate ?: currentJob.createdAt) }
        var editEndDateIso by remember { mutableStateOf(currentJob.deadline) }

        AlertDialog(
            onDismissRequest = { jobToEdit = null },
            containerColor = Color.White,
            shape = RoundedCornerShape(20.dp),
            confirmButton = {
                Button(
                    onClick = {
                        if (editTitle.isNotBlank() && editDescription.isNotBlank() && editLocation.isNotBlank()) {
                            val skillsList = editSkillsStr.split(",").map { it.trim() }.filter { it.isNotBlank() }
                            val resolvedDeadline = editEndDateIso.ifBlank { currentJob.deadline }
                            val resolvedStartDate = editStartDateIso.ifBlank { null }
                            jobViewModel.updateJob(
                                jobId = currentJob.id,
                                title = editTitle,
                                description = editDescription,
                                jobType = editJobType,
                                requiredSkills = skillsList,
                                location = editLocation,
                                salaryRange = editSalaryRange.takeIf { it.isNotBlank() },
                                deadline = resolvedDeadline,
                                startDate = resolvedStartDate,
                                onSuccess = {
                                    if (selectedJobForDetails?.id == currentJob.id) {
                                        selectedJobForDetails = currentJob.copy(
                                            title = editTitle,
                                            description = editDescription,
                                            jobType = editJobType,
                                            requiredSkills = skillsList,
                                            location = editLocation,
                                            salaryRange = editSalaryRange.takeIf { it.isNotBlank() },
                                            deadline = resolvedDeadline,
                                            startDate = resolvedStartDate
                                        )
                                    }
                                    jobToEdit = null
                                }
                            )
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = DevLinkBlue)
                ) {
                    Text("Save Changes", color = Color.White)
                }
            },
            dismissButton = {
                TextButton(onClick = { jobToEdit = null }) {
                    Text("Cancel", color = Color.Gray)
                }
            },
            title = { Text("Edit Job Posting", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = DevLinkTextPrimary) },
            text = {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    OutlinedTextField(
                        value = editTitle,
                        onValueChange = { editTitle = it },
                        label = { Text("Job Title *") },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(10.dp)
                    )
                    OutlinedTextField(
                        value = editDescription,
                        onValueChange = { editDescription = it },
                        label = { Text("Description *") },
                        maxLines = 3,
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(10.dp)
                    )

                    // Job Type Selection
                    Column {
                        Text("Job Type", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = DevLinkTextSecondary)
                        Spacer(modifier = Modifier.height(4.dp))
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            JobType.values().forEach { type ->
                                FilterChip(
                                    selected = editJobType == type,
                                    onClick = { editJobType = type },
                                    label = { Text(type.name, fontSize = 10.sp) },
                                    colors = FilterChipDefaults.filterChipColors(
                                        selectedContainerColor = DevLinkBlue,
                                        selectedLabelColor = Color.White
                                    )
                                )
                            }
                        }
                    }

                    OutlinedTextField(
                        value = editSkillsStr,
                        onValueChange = { editSkillsStr = it },
                        label = { Text("Required Skills (comma separated)") },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(10.dp)
                    )
                    OutlinedTextField(
                        value = editLocation,
                        onValueChange = { editLocation = it },
                        label = { Text("Location (or 'Remote') *") },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(10.dp)
                    )
                    OutlinedTextField(
                        value = editSalaryRange,
                        onValueChange = { editSalaryRange = it },
                        label = { Text("Stipend / Salary range (optional)") },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(10.dp)
                    )

                    // Active visibility dates section
                    Surface(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        color = Color(0xFFF8FAFC),
                        border = BorderStroke(1.dp, DevLinkBorder)
                    ) {
                        Column(
                            modifier = Modifier.padding(12.dp),
                            verticalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Text(
                                text = "Job Visibility Window",
                                fontWeight = FontWeight.Bold,
                                fontSize = 13.sp,
                                color = DevLinkBlue
                            )
                            Text(
                                text = "Visible to students starting from:",
                                fontSize = 11.sp,
                                color = DevLinkTextSecondary
                            )
                            MeetingSchedulePicker(
                                initialIsoString = editStartDateIso,
                                onIsoStringChange = { editStartDateIso = it }
                            )

                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "Until application deadline (end date):",
                                fontSize = 11.sp,
                                color = DevLinkTextSecondary
                            )
                            MeetingSchedulePicker(
                                initialIsoString = editEndDateIso,
                                onIsoStringChange = { editEndDateIso = it }
                            )
                        }
                    }
                }
            }
        )
    }

    // Delete confirmation dialog
    jobToDelete?.let { jobDel ->
        AlertDialog(
            onDismissRequest = { jobToDelete = null },
            containerColor = Color.White,
            shape = RoundedCornerShape(20.dp),
            confirmButton = {
                Button(
                    onClick = {
                        jobViewModel.deleteJob(jobDel.id) {
                            if (selectedJobForDetails?.id == jobDel.id) {
                                selectedJobForDetails = null
                            }
                            jobToDelete = null
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFEF4444))
                ) {
                    Text("Delete", color = Color.White, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { jobToDelete = null }) {
                    Text("Cancel", color = Color.Gray)
                }
            },
            title = {
                Text(
                    text = "Delete Job Posting?",
                    fontWeight = FontWeight.Bold,
                    fontSize = 17.sp,
                    color = Color(0xFFEF4444)
                )
            },
            text = {
                Text(
                    text = "Are you sure you want to delete '${jobDel.title}'? This action cannot be undone and will remove all student applications submitted for this job.",
                    fontSize = 13.sp,
                    color = DevLinkTextPrimary
                )
            }
        )
    }
}

@Composable
fun JobListItem(
    job: JobResponse,
    onClick: () -> Unit,
    onEdit: (() -> Unit)? = null,
    onDelete: (() -> Unit)? = null
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        border = BorderStroke(1.dp, DevLinkBorder)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = job.title,
                    fontWeight = FontWeight.Bold,
                    fontSize = 15.sp,
                    color = DevLinkTextPrimary,
                    modifier = Modifier.weight(1f)
                )
                Surface(
                    color = if (job.status == JobStatus.OPEN) Color(0xFFE0F2FE) else Color(0xFFF1F1F1),
                    shape = RoundedCornerShape(4.dp)
                ) {
                    Text(
                        text = job.status.name,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (job.status == JobStatus.OPEN) DevLinkBlue else Color.Gray,
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                    )
                }
            }
            Spacer(modifier = Modifier.height(4.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(text = "${job.jobType} · ${job.location}", fontSize = 12.sp, color = DevLinkTextSecondary)
            }
            Spacer(modifier = Modifier.height(10.dp))

            // Visibility / Active dates row
            val fromDate = (job.startDate ?: job.createdAt).take(10)
            val toDate = job.deadline.take(10)
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.CalendarToday,
                    contentDescription = null,
                    tint = DevLinkBlue,
                    modifier = Modifier.size(13.dp)
                )
                Text(
                    text = "Visible: $fromDate to $toDate",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Medium,
                    color = DevLinkBlue
                )
            }

            Spacer(modifier = Modifier.height(8.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "${job.applicantCount} applicants",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = DevLinkBlue
                )
                if (onEdit != null && onDelete != null) {
                    Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                        IconButton(
                            onClick = onEdit,
                            modifier = Modifier.size(28.dp)
                        ) {
                            Icon(Icons.Default.Edit, contentDescription = "Edit Job", tint = DevLinkBlue, modifier = Modifier.size(16.dp))
                        }
                        IconButton(
                            onClick = onDelete,
                            modifier = Modifier.size(28.dp)
                        ) {
                            Icon(Icons.Default.Delete, contentDescription = "Delete Job", tint = Color(0xFFEF4444), modifier = Modifier.size(16.dp))
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun SavedCandidatesGroupedScreen(
    savedCandidates: List<SavedCandidateResponse>,
    recruiterJobs: List<JobResponse>,
    onUserClick: (ProfileResponse) -> Unit,
    onMessageClick: (String) -> Unit
) {
    if (savedCandidates.isEmpty()) {
        Box(modifier = Modifier.fillMaxSize().padding(32.dp), contentAlignment = Alignment.Center) {
            Text("No shortlisted candidates found. Bookmark someone from Find Talent!", color = Color.Gray, fontSize = 14.sp, textAlign = TextAlign.Center)
        }
        return
    }

    LazyColumn(
        modifier = Modifier.fillMaxSize().background(Color(0xFFF6F7FB)),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // 1. General Interest bucket
        val generalSaved = savedCandidates.filter { it.jobId == null }
        if (generalSaved.isNotEmpty()) {
            item {
                Text(text = "General Interest", fontWeight = FontWeight.Bold, fontSize = 16.sp, color = DevLinkTextPrimary)
            }
            items(generalSaved) { saved ->
                SavedCandidateItem(student = saved.studentProfile, onUserClick = onUserClick, onMessageClick = onMessageClick)
            }
        }

        // 2. Per Job groupings
        recruiterJobs.forEach { job ->
            val jobSaved = savedCandidates.filter { it.jobId == job.id }
            if (jobSaved.isNotEmpty()) {
                item {
                    Text(text = job.title, fontWeight = FontWeight.Bold, fontSize = 16.sp, color = DevLinkTextPrimary)
                }
                items(jobSaved) { saved ->
                    SavedCandidateItem(student = saved.studentProfile, onUserClick = onUserClick, onMessageClick = onMessageClick)
                }
            }
        }
    }
}

@Composable
fun SavedCandidateItem(
    student: ProfileResponse,
    onUserClick: (ProfileResponse) -> Unit,
    onMessageClick: (String) -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onUserClick(student) },
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        border = BorderStroke(1.dp, DevLinkBorder)
    ) {
        Row(
            modifier = Modifier.padding(12.dp).fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier.size(40.dp).clip(CircleShape).background(DevLinkBlueContainer),
                contentAlignment = Alignment.Center
            ) {
                if (!student.photoUrl.isNullOrBlank()) {
                    AsyncImage(
                        model = student.photoUrl.toFullMediaUrl(),
                        contentDescription = null,
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxSize()
                    )
                } else {
                    Text(text = student.name?.take(2)?.uppercase() ?: "ST", color = DevLinkBlue, fontWeight = FontWeight.Bold)
                }
            }
            Spacer(modifier = Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(text = student.name ?: student.username, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                Text(text = "@${student.username}", fontSize = 11.sp, color = Color.Gray)
            }
            IconButton(onClick = { onMessageClick(student.username) }) {
                Icon(Icons.Default.Chat, contentDescription = "Message", tint = DevLinkBlue, modifier = Modifier.size(20.dp))
            }
        }
    }
}

@Composable
fun JobDetailDashboard(
    job: JobResponse,
    jobState: JobUiState,
    jobViewModel: JobViewModel,
    onBackClick: () -> Unit,
    onUserClick: (ProfileResponse) -> Unit,
    onMessageClick: (String) -> Unit,
    onEditJob: () -> Unit = {},
    onDeleteJob: () -> Unit = {}
) {
    var activeSubTab by remember { mutableStateOf(0) } // 0 = Applicants, 1 = Recommended

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF6F7FB))
    ) {
        // Header Row
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(Color.White)
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onBackClick) {
                Icon(Icons.Default.ArrowBack, contentDescription = "Back")
            }
            Spacer(modifier = Modifier.width(4.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(text = job.title, fontWeight = FontWeight.Bold, fontSize = 16.sp, color = DevLinkTextPrimary, maxLines = 1)
                Text(text = "${job.jobType} · ${job.location}", fontSize = 12.sp, color = DevLinkTextSecondary, maxLines = 1)
            }
            IconButton(onClick = onEditJob) {
                Icon(Icons.Default.Edit, contentDescription = "Edit Job", tint = DevLinkBlue)
            }
            IconButton(onClick = onDeleteJob) {
                Icon(Icons.Default.Delete, contentDescription = "Delete Job", tint = Color(0xFFEF4444))
            }
        }

        // Sub Tabs
        TabRow(
            selectedTabIndex = activeSubTab,
            containerColor = Color.White,
            contentColor = DevLinkBlue
        ) {
            Tab(
                selected = activeSubTab == 0,
                onClick = { activeSubTab = 0 },
                text = { Text("Applicants (${jobState.jobApplications.size})") }
            )
            Tab(
                selected = activeSubTab == 1,
                onClick = { activeSubTab = 1 },
                text = { Text("Recommended") }
            )
        }

        if (activeSubTab == 0) {
            // Applicants List
            var statusFilter by remember { mutableStateOf("ALL") } // ALL, PENDING, SHORTLISTED, REJECTED
            val filteredApps = when (statusFilter) {
                "ALL" -> jobState.jobApplications
                else -> jobState.jobApplications.filter { it.status.name == statusFilter }
            }

            Column(modifier = Modifier.fillMaxSize()) {
                // Status Filter Chips
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 8.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    listOf("ALL" to "All", "PENDING" to "Pending", "SHORTLISTED" to "Shortlisted", "REJECTED" to "Rejected").forEach { (key, label) ->
                        FilterChip(
                            selected = statusFilter == key,
                            onClick = { statusFilter = key },
                            label = { Text(label, fontSize = 11.sp) }
                        )
                    }
                }

                if (filteredApps.isEmpty()) {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Text("No applicants found matching this status.", color = Color.Gray, fontSize = 14.sp)
                    }
                } else {
                    LazyColumn(
                        contentPadding = PaddingValues(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        items(filteredApps) { app ->
                            ApplicantItemCard(
                                application = app,
                                onUserClick = onUserClick,
                                onShortlist = { jobViewModel.evaluateApplication(app.id, job.id, JobApplicationStatus.SHORTLISTED) },
                                onReject = { jobViewModel.evaluateApplication(app.id, job.id, JobApplicationStatus.REJECTED) },
                                onMessage = { onMessageClick(app.studentProfile.username) }
                            )
                        }
                    }
                }
            }
        } else {
            // Recommended candidates
            if (jobState.recommendedCandidates.isEmpty()) {
                Box(modifier = Modifier.fillMaxSize().padding(32.dp), contentAlignment = Alignment.Center) {
                    Text("No match found. Recommended candidates are matched automatically based on verified skills.", color = Color.Gray, fontSize = 13.sp, textAlign = TextAlign.Center)
                }
            } else {
                LazyColumn(
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(jobState.recommendedCandidates) { candidate ->
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { onUserClick(candidate) },
                            shape = RoundedCornerShape(12.dp),
                            colors = CardDefaults.cardColors(containerColor = Color.White),
                            border = BorderStroke(1.dp, DevLinkBorder)
                        ) {
                            Row(
                                modifier = Modifier.padding(16.dp).fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Box(
                                    modifier = Modifier.size(44.dp).clip(CircleShape).background(DevLinkBlueContainer),
                                    contentAlignment = Alignment.Center
                                ) {
                                    if (!candidate.photoUrl.isNullOrBlank()) {
                                        AsyncImage(model = candidate.photoUrl.toFullMediaUrl(), contentDescription = null, modifier = Modifier.fillMaxSize(), contentScale = ContentScale.Crop)
                                    } else {
                                        Text(text = candidate.name?.take(2)?.uppercase() ?: "ST", color = DevLinkBlue, fontWeight = FontWeight.Bold)
                                    }
                                }
                                Spacer(modifier = Modifier.width(12.dp))
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(text = candidate.name ?: candidate.username, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                                    Text(text = "${candidate.course ?: ""} · Sem ${candidate.semester ?: 1}", fontSize = 11.sp, color = Color.Gray)
                                    Spacer(modifier = Modifier.height(4.dp))
                                    // Highlight skill matches
                                    val matched = candidate.socialLinks?.map { it.platform.name } ?: emptyList() // or dummy match highlight
                                    Text(text = "Points: ${candidate.totalPoints ?: 0}", color = DevLinkBlue, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                }
                                Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                                    IconButton(onClick = { jobViewModel.shortlistCandidate(candidate.userId, job.id) }) {
                                        Icon(Icons.Default.Bookmark, contentDescription = "Shortlist", tint = DevLinkBlue)
                                    }
                                    IconButton(onClick = { onMessageClick(candidate.username) }) {
                                        Icon(Icons.Default.Chat, contentDescription = "Message", tint = DevLinkBlue)
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

@Composable
fun ApplicantItemCard(
    application: JobApplicationResponse,
    onUserClick: (ProfileResponse) -> Unit,
    onShortlist: () -> Unit,
    onReject: () -> Unit,
    onMessage: () -> Unit
) {
    val student = application.studentProfile
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onUserClick(student) },
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        border = BorderStroke(1.dp, DevLinkBorder)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier.size(44.dp).clip(CircleShape).background(DevLinkBlueContainer),
                    contentAlignment = Alignment.Center
                ) {
                    if (!student.photoUrl.isNullOrBlank()) {
                        AsyncImage(model = student.photoUrl.toFullMediaUrl(), contentDescription = null, modifier = Modifier.fillMaxSize(), contentScale = ContentScale.Crop)
                    } else {
                        Text(text = student.name?.take(2)?.uppercase() ?: "ST", color = DevLinkBlue, fontWeight = FontWeight.Bold)
                    }
                }
                Spacer(modifier = Modifier.width(12.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(text = student.name ?: student.username, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                    Text(text = "${student.course ?: ""} · Sem ${student.semester ?: 1} · ${student.totalPoints ?: 0} pts", fontSize = 12.sp, color = Color.Gray)
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
            
            Spacer(modifier = Modifier.height(16.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                TextButton(onClick = { onUserClick(student) }) {
                    Text("View Profile", fontSize = 12.sp, color = DevLinkBlue, fontWeight = FontWeight.Bold)
                }
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    IconButton(onClick = onMessage, modifier = Modifier.size(32.dp)) {
                        Icon(Icons.Default.Chat, contentDescription = "Message", tint = DevLinkBlue)
                    }
                    if (application.status == JobApplicationStatus.PENDING) {
                        IconButton(onClick = onReject, modifier = Modifier.size(32.dp)) {
                            Icon(Icons.Default.Close, contentDescription = "Reject", tint = Color.Red)
                        }
                        IconButton(onClick = onShortlist, modifier = Modifier.size(32.dp)) {
                            Icon(Icons.Default.Check, contentDescription = "Shortlist", tint = Color(0xFF15803D))
                        }
                    }
                }
            }
        }
    }
}
