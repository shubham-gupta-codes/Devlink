package com.zyproo.devlink.ui.home

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.text.KeyboardOptions
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
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.hilt.navigation.compose.hiltViewModel
import coil3.compose.AsyncImage
import androidx.compose.foundation.BorderStroke
import com.zyproo.devlink.common.Role
import com.zyproo.devlink.common.toFullMediaUrl
import com.zyproo.devlink.data.remote.dto.*
import com.zyproo.devlink.ui.profile.EditProfileScreen
import com.zyproo.devlink.ui.profile.ProfileViewModel
import com.zyproo.devlink.ui.profile.StudentProfileScreen
import com.zyproo.devlink.ui.profile.SkillViewModel
import com.zyproo.devlink.ui.profile.ProjectViewModel
import com.zyproo.devlink.ui.profile.CertificateViewModel
import com.zyproo.devlink.ui.profile.TeacherVerificationViewModel
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Build
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.CardMembership
import androidx.compose.material.icons.filled.List
import androidx.compose.material.icons.filled.Star
import com.zyproo.devlink.ui.theme.*
import com.zyproo.devlink.ui.chat.ChatViewModel
import com.zyproo.devlink.ui.chat.TeamViewModel
import com.zyproo.devlink.ui.chat.ChatListScreen
import com.zyproo.devlink.ui.chat.ChatThreadScreen
import com.zyproo.devlink.ui.chat.TeamSettingsScreen
import com.zyproo.devlink.ui.chat.TeamInviteScreen
import com.zyproo.devlink.ui.chat.StartDirectChatDialog
import com.zyproo.devlink.ui.job.JobViewModel
import com.zyproo.devlink.ui.profile.VisitedProfileViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DashboardScreen(
    role: Role,
    onLogoutClick: () -> Unit = {}
) {
    val profileViewModel: ProfileViewModel = hiltViewModel()
    val profileState by profileViewModel.uiState.collectAsState()

    val visitedProfileViewModel: VisitedProfileViewModel = hiltViewModel()
    val visitedProfileState by visitedProfileViewModel.uiState.collectAsState()

    val jobViewModel: JobViewModel = hiltViewModel()
    val jobState = jobViewModel.state

    val skillViewModel: SkillViewModel = hiltViewModel()
    val skillState = skillViewModel.state

    val projectViewModel: ProjectViewModel = hiltViewModel()
    val projectState by projectViewModel.uiState.collectAsState()

    val certificateViewModel: CertificateViewModel = hiltViewModel()
    val certificateState by certificateViewModel.uiState.collectAsState()

    val verificationViewModel: TeacherVerificationViewModel = hiltViewModel()
    val verificationState by verificationViewModel.uiState.collectAsState()

    val chatViewModel: ChatViewModel = hiltViewModel()
    val chatState by chatViewModel.conversations.collectAsState()
    val activeMessages by chatViewModel.activeMessages.collectAsState()
    val chatCurrentUserId by chatViewModel.currentUserId.collectAsState()
    val chatThreadError by chatViewModel.threadError.collectAsState()

    val teamViewModel: TeamViewModel = hiltViewModel()
    val myTeams by teamViewModel.myTeams.collectAsState()
    val activeTeam by teamViewModel.activeTeam.collectAsState()
    val pendingInvites by teamViewModel.pendingInvites.collectAsState()

    var activeChatConversationId by remember { mutableStateOf<Long?>(null) }
    var activeTeamSettingsId by remember { mutableStateOf<Long?>(null) }
    var showTeamInviteScreen by remember { mutableStateOf<Long?>(null) }
    var showCreateTeamDialog by remember { mutableStateOf(false) }
    var showStartDirectChatDialog by remember { mutableStateOf(false) }

    var selectedProjectForDetails by remember { mutableStateOf<ProjectResponse?>(null) }
    var selectedCertificateForDetails by remember { mutableStateOf<CertificateResponse?>(null) }

    var activeTab by remember { mutableStateOf(0) }
    var isEditingProfile by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        profileViewModel.loadProfile()
        profileViewModel.loadStudentRankings()
        skillViewModel.loadSkillsCatalog()
        skillViewModel.loadStudentSkills()
        skillViewModel.loadStudentRequests()
        skillViewModel.loadTeacherQueue()
        skillViewModel.loadNotifications()
        projectViewModel.loadFeed("all")
        verificationViewModel.loadQueues()
        teamViewModel.loadPendingInvites()
        teamViewModel.loadMyTeams()
        jobViewModel.loadRecruiterJobs()
        jobViewModel.loadOpenJobs()
        jobViewModel.loadSavedCandidates()
    }

    LaunchedEffect(profileState.profile) {
        profileState.profile?.let {
            projectViewModel.loadStudentProjects(it.userId)
            certificateViewModel.loadStudentCertificates(it.userId)
        }
    }

    LaunchedEffect(projectState.uploadSuccess) {
        if (projectState.uploadSuccess) {
            projectViewModel.resetUploadSuccess()
            profileState.profile?.let { projectViewModel.loadStudentProjects(it.userId) }
            projectViewModel.loadFeed("all")
            activeTab = 0
        }
    }

    LaunchedEffect(certificateState.uploadSuccess) {
        if (certificateState.uploadSuccess) {
            certificateViewModel.resetUploadSuccess()
            profileState.profile?.let { certificateViewModel.loadStudentCertificates(it.userId) }
            activeTab = 4
        }
    }

    LaunchedEffect(verificationState.evaluationSuccess) {
        if (verificationState.evaluationSuccess) {
            verificationViewModel.resetEvaluationSuccess()
            profileViewModel.loadStudentRankings()
            verificationViewModel.loadQueues()
        }
    }

    LaunchedEffect(profileState.saveSuccess) {
        if (profileState.saveSuccess) {
            isEditingProfile = false
            profileViewModel.resetSaveSuccess()
            profileViewModel.loadProfile()
            profileViewModel.loadStudentRankings()
            projectViewModel.loadFeed("all")
            profileState.profile?.let {
                projectViewModel.loadStudentProjects(it.userId)
                certificateViewModel.loadStudentCertificates(it.userId)
            }
        }
    }

    LaunchedEffect(skillState.requestSuccess, skillState.decisionSuccess) {
        if (skillState.requestSuccess) {
            skillViewModel.resetRequestSuccess()
            profileViewModel.loadProfile()
            profileViewModel.loadStudentRankings()
            skillViewModel.loadStudentSkills()
            skillViewModel.loadStudentRequests()
        }
        if (skillState.decisionSuccess) {
            skillViewModel.resetDecisionSuccess()
            profileViewModel.loadStudentRankings()
            skillViewModel.loadTeacherQueue()
            skillViewModel.loadNotifications()
            verificationViewModel.loadQueues()
        }
    }

    var homeSubScreen by remember { mutableStateOf("feed") }
    var selectedStudentProfileForDetails by remember { mutableStateOf<ProfileResponse?>(null) }

    LaunchedEffect(selectedStudentProfileForDetails) {
        selectedStudentProfileForDetails?.let {
            visitedProfileViewModel.loadVisitedProfileData(it.userId)
        } ?: run {
            visitedProfileViewModel.clearVisitedProfileData()
        }
    }

    LaunchedEffect(activeTab) {
        homeSubScreen = "feed"
    }

    val currentProfile = profileState.profile
    val currentUsername = currentProfile?.username.orEmpty()
    val currentEffectiveUserId = if (chatCurrentUserId != 0L) chatCurrentUserId else (currentProfile?.userId ?: 0L)

    val navigateToSelfProfile: () -> Unit = {
        selectedStudentProfileForDetails = null
        selectedProjectForDetails = null
        selectedCertificateForDetails = null
        activeChatConversationId = null
        when (role) {
            Role.STUDENT -> {
                homeSubScreen = "feed"
                activeTab = 4
            }
            Role.TEACHER -> {
                isEditingProfile = true
            }
            Role.RECRUITER -> {
                activeTab = 4
            }
            Role.ADMIN -> {
                activeTab = 4
            }
        }
    }

    val handleUserClick: (ProfileResponse) -> Unit = { clickedStudent ->
        val isSelf = (clickedStudent.userId != 0L && clickedStudent.userId == currentEffectiveUserId) ||
                     (currentUsername.isNotBlank() && clickedStudent.username.equals(currentUsername, ignoreCase = true))
        if (isSelf) {
            navigateToSelfProfile()
        } else {
            selectedProjectForDetails = null
            selectedCertificateForDetails = null
            selectedStudentProfileForDetails = clickedStudent
        }
    }

    val handleUsernameClick: (String) -> Unit = { username ->
        if (currentUsername.isNotBlank() && username.equals(currentUsername, ignoreCase = true)) {
            navigateToSelfProfile()
        } else {
            val student = profileState.rankings.find { it.username.equals(username, ignoreCase = true) }
                ?: profileState.searchResults.find { it.username.equals(username, ignoreCase = true) }
            if (student != null) {
                selectedProjectForDetails = null
                selectedCertificateForDetails = null
                selectedStudentProfileForDetails = student
            } else {
                profileViewModel.fetchProfileByUsername(username) { fetched ->
                    if (fetched != null) {
                        selectedProjectForDetails = null
                        selectedCertificateForDetails = null
                        selectedStudentProfileForDetails = fetched
                    }
                }
            }
        }
    }

    if (isEditingProfile && currentProfile != null) {
        EditProfileScreen(
            profile = currentProfile,
            isSaving = profileState.isSaving,
            saveError = profileState.saveError,
            onBackClick = {
                isEditingProfile = false
                profileViewModel.resetSaveSuccess()
            },
            onSaveClick = { username, name, course, branch, admissionYear, semester, bio, contact, photoUrl, employeeId, department, designation, companyName, companyLogo, socialLinks, about, website ->
                profileViewModel.updateProfile(
                    username = username,
                    name = name,
                    course = course,
                    branch = branch,
                    admissionYear = admissionYear,
                    semester = semester,
                    bio = bio,
                    contact = contact,
                    photoUrl = photoUrl,
                    employeeId = employeeId,
                    department = department,
                    designation = designation,
                    companyName = companyName,
                    companyLogo = companyLogo,
                    socialLinks = socialLinks,
                    about = about,
                    website = website
                )
            }
        )
        return
    }

    androidx.activity.compose.BackHandler(
        enabled = selectedProjectForDetails != null ||
                  selectedStudentProfileForDetails != null ||
                  activeChatConversationId != null ||
                  (role == Role.STUDENT && (homeSubScreen != "feed" || activeTab != 0))
    ) {
        when {
            selectedProjectForDetails != null -> selectedProjectForDetails = null
            selectedStudentProfileForDetails != null -> selectedStudentProfileForDetails = null
            activeChatConversationId != null -> activeChatConversationId = null
            role == Role.STUDENT && homeSubScreen != "feed" -> homeSubScreen = "feed"
            role == Role.STUDENT && activeTab != 0 -> activeTab = 0
        }
    }

    Scaffold(
        topBar = {
            val showGenericTopBar = selectedProjectForDetails == null &&
                                    selectedStudentProfileForDetails == null &&
                                    activeChatConversationId == null
            if (showGenericTopBar) {
                TopAppBar(
                    title = {
                        val title = when (role) {
                            Role.STUDENT -> {
                                when (activeTab) {
                                    0 -> when (homeSubScreen) {
                                        "search" -> "Search Talent"
                                        "leaderboard" -> "Leaderboard"
                                        else -> "DevLink"
                                    }
                                    1 -> "Jobs"
                                    2 -> "Notifications"
                                    3 -> "Messages"
                                    4 -> "Profile"
                                    else -> "DevLink"
                                }
                            }
                            Role.TEACHER -> {
                                when (activeTab) {
                                    0 -> "Dashboard"
                                    1 -> "Verification Center"
                                    2 -> "Students Directory"
                                    3 -> "Department Analytics"
                                    4 -> "Messages"
                                    else -> "DevLink"
                                }
                            }
                            Role.RECRUITER -> {
                                when (activeTab) {
                                    0 -> "Search Talent"
                                    1 -> "Jobs"
                                    2 -> "Notifications"
                                    3 -> "Messages"
                                    4 -> "Profile"
                                    else -> "DevLink"
                                }
                            }
                            Role.ADMIN -> {
                                when (activeTab) {
                                    0 -> "Admin Dashboard"
                                    1 -> "User Management"
                                    2 -> "Audit Logs"
                                    3 -> "Analytics"
                                    4 -> "Settings"
                                    else -> "DevLink"
                                }
                            }
                        }
                        Text(text = title, fontWeight = FontWeight.Bold)
                    },
                    navigationIcon = {
                        if (role == Role.STUDENT && activeTab == 0 && homeSubScreen != "feed") {
                            IconButton(onClick = { homeSubScreen = "feed" }) {
                                Icon(imageVector = Icons.Default.ArrowBack, contentDescription = "Back")
                            }
                        }
                    },
                    actions = {
                        if (role == Role.STUDENT && activeTab == 0 && homeSubScreen == "feed") {
                            IconButton(onClick = { homeSubScreen = "search" }) {
                                Icon(imageVector = Icons.Default.Search, contentDescription = "Search")
                            }
                            IconButton(onClick = { homeSubScreen = "leaderboard" }) {
                                Icon(imageVector = Icons.Default.BarChart, contentDescription = "Leaderboard")
                            }
                        }
                        if ((role == Role.TEACHER || role == Role.ADMIN || role == Role.RECRUITER) && onLogoutClick != null) {
                            IconButton(onClick = onLogoutClick) {
                                Icon(
                                    imageVector = Icons.Default.ExitToApp,
                                    contentDescription = "Logout",
                                    tint = Color(0xFFEF4444)
                                )
                            }
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = Color.White,
                        titleContentColor = DevLinkTextPrimary,
                        navigationIconContentColor = DevLinkTextPrimary,
                        actionIconContentColor = DevLinkTextPrimary
                    )
                )
            }
        },
        bottomBar = {
            val showBottomBar = selectedProjectForDetails == null &&
                                selectedStudentProfileForDetails == null &&
                                activeChatConversationId == null
            if (showBottomBar) {
                val navItemColors = NavigationBarItemDefaults.colors(
                    selectedIconColor = DevLinkBlue,
                    selectedTextColor = DevLinkBlue,
                    unselectedIconColor = DevLinkTextSecondary,
                    unselectedTextColor = DevLinkTextSecondary,
                    indicatorColor = Color(0xFFEFF6FF)
                )
                NavigationBar(
                    containerColor = Color.White,
                    tonalElevation = 8.dp
                ) {
                    when (role) {
                        Role.STUDENT -> {
                            NavigationBarItem(
                                selected = activeTab == 0,
                                onClick = { activeTab = 0 },
                                icon = { Icon(Icons.Default.Home, contentDescription = "Home") },
                                label = { Text("Home", fontSize = 10.sp) },
                                colors = navItemColors
                            )
                            NavigationBarItem(
                                selected = activeTab == 1,
                                onClick = { activeTab = 1 },
                                icon = { Icon(Icons.Default.Work, contentDescription = "Jobs") },
                                label = { Text("Jobs", fontSize = 10.sp) },
                                colors = navItemColors
                            )
                            NavigationBarItem(
                                selected = activeTab == 2,
                                onClick = { activeTab = 2 },
                                icon = { Icon(Icons.Default.Notifications, contentDescription = "Notifications") },
                                label = { Text("Notifications", fontSize = 10.sp) },
                                colors = navItemColors
                            )
                            NavigationBarItem(
                                selected = activeTab == 3,
                                onClick = { activeTab = 3 },
                                icon = { Icon(Icons.Default.MailOutline, contentDescription = "Chat") },
                                label = { Text("Chat", fontSize = 10.sp) },
                                colors = navItemColors
                            )
                            NavigationBarItem(
                                selected = activeTab == 4,
                                onClick = { activeTab = 4 },
                                icon = { Icon(Icons.Default.Person, contentDescription = "Profile") },
                                label = { Text("Profile", fontSize = 10.sp) },
                                colors = navItemColors
                            )
                        }
                        Role.TEACHER -> {
                            NavigationBarItem(
                                selected = activeTab == 0,
                                onClick = { activeTab = 0 },
                                icon = { Icon(Icons.Default.Dashboard, contentDescription = "Dashboard") },
                                label = { Text("Dashboard", fontSize = 10.sp) },
                                colors = navItemColors
                            )
                            NavigationBarItem(
                                selected = activeTab == 1,
                                onClick = { activeTab = 1 },
                                icon = {
                                    val totalPending = skillState.teacherQueue.size + verificationState.projectsQueue.size + verificationState.certificatesQueue.size
                                    if (totalPending > 0) {
                                        BadgedBox(
                                            badge = {
                                                Badge(containerColor = Color.Red, contentColor = Color.White) {
                                                    Text("$totalPending")
                                                }
                                            }
                                        ) {
                                            Icon(Icons.Default.FactCheck, contentDescription = "Verification")
                                        }
                                    } else {
                                        Icon(Icons.Default.FactCheck, contentDescription = "Verification")
                                    }
                                },
                                label = { Text("Verify", fontSize = 10.sp) },
                                colors = navItemColors
                            )
                            NavigationBarItem(
                                selected = activeTab == 2,
                                onClick = { activeTab = 2 },
                                icon = { Icon(Icons.Default.People, contentDescription = "Students") },
                                label = { Text("Students", fontSize = 10.sp) },
                                colors = navItemColors
                            )
                            NavigationBarItem(
                                selected = activeTab == 3,
                                onClick = { activeTab = 3 },
                                icon = { Icon(Icons.Default.BarChart, contentDescription = "Analytics") },
                                label = { Text("Analytics", fontSize = 10.sp) },
                                colors = navItemColors
                            )
                            NavigationBarItem(
                                selected = activeTab == 4,
                                onClick = { activeTab = 4 },
                                icon = { Icon(Icons.Default.MailOutline, contentDescription = "Messages") },
                                label = { Text("Messages", fontSize = 10.sp) },
                                colors = navItemColors
                            )
                        }
                        Role.RECRUITER -> {
                            NavigationBarItem(
                                selected = activeTab == 0,
                                onClick = { activeTab = 0 },
                                icon = { Icon(Icons.Default.Home, contentDescription = "Home") },
                                label = { Text("Home", fontSize = 10.sp) },
                                colors = navItemColors
                            )
                            NavigationBarItem(
                                selected = activeTab == 1,
                                onClick = { activeTab = 1 },
                                icon = { Icon(Icons.Default.Work, contentDescription = "Jobs") },
                                label = { Text("Jobs", fontSize = 10.sp) },
                                colors = navItemColors
                            )
                            NavigationBarItem(
                                selected = activeTab == 2,
                                onClick = { activeTab = 2 },
                                icon = { Icon(Icons.Default.Notifications, contentDescription = "Notifications") },
                                label = { Text("Notifications", fontSize = 10.sp) },
                                colors = navItemColors
                            )
                            NavigationBarItem(
                                selected = activeTab == 3,
                                onClick = { activeTab = 3 },
                                icon = { Icon(Icons.Default.MailOutline, contentDescription = "Messages") },
                                label = { Text("Messages", fontSize = 10.sp) },
                                colors = navItemColors
                            )
                            NavigationBarItem(
                                selected = activeTab == 4,
                                onClick = { activeTab = 4 },
                                icon = { Icon(Icons.Default.Person, contentDescription = "Profile") },
                                label = { Text("Profile", fontSize = 10.sp) },
                                colors = navItemColors
                            )
                        }
                        Role.ADMIN -> {
                            NavigationBarItem(
                                selected = activeTab == 0,
                                onClick = { activeTab = 0 },
                                icon = { Icon(Icons.Default.Dashboard, contentDescription = "Dashboard") },
                                label = { Text("Dashboard", fontSize = 10.sp) },
                                colors = navItemColors
                            )
                            NavigationBarItem(
                                selected = activeTab == 1,
                                onClick = { activeTab = 1 },
                                icon = { Icon(Icons.Default.People, contentDescription = "Users") },
                                label = { Text("Users", fontSize = 10.sp) },
                                colors = navItemColors
                            )
                            NavigationBarItem(
                                selected = activeTab == 2,
                                onClick = { activeTab = 2 },
                                icon = { Icon(Icons.Default.ReceiptLong, contentDescription = "Audit Logs") },
                                label = { Text("Audit Logs", fontSize = 10.sp) },
                                colors = navItemColors
                            )
                            NavigationBarItem(
                                selected = activeTab == 3,
                                onClick = { activeTab = 3 },
                                icon = { Icon(Icons.Default.BarChart, contentDescription = "Analytics") },
                                label = { Text("Analytics", fontSize = 10.sp) },
                                colors = navItemColors
                            )
                            NavigationBarItem(
                                selected = activeTab == 4,
                                onClick = { activeTab = 4 },
                                icon = { Icon(Icons.Default.Settings, contentDescription = "Settings") },
                                label = { Text("Settings", fontSize = 10.sp) },
                                colors = navItemColors
                            )
                        }
                    }
                }
            }
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            Column(modifier = Modifier.fillMaxSize()) {
                if (profileState.error?.contains("403") == true) {
                    Surface(
                        color = MaterialTheme.colorScheme.errorContainer,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp, vertical = 8.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(
                                modifier = Modifier.weight(1f),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Warning,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.onErrorContainer
                                )
                                Text(
                                    text = "Session Expired (HTTP 403). Please log out and sign in again.",
                                    fontSize = 12.sp,
                                    color = MaterialTheme.colorScheme.onErrorContainer
                                )
                            }
                            Button(
                                onClick = onLogoutClick,
                                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error),
                                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp),
                                shape = RoundedCornerShape(6.dp)
                            ) {
                                Text("Log Out", fontSize = 12.sp)
                            }
                        }
                    }
                }

                Box(modifier = Modifier.weight(1f)) {
                    val isChatActive = (role == Role.STUDENT && activeTab == 3) ||
                                       (role == Role.TEACHER && activeTab == 4) ||
                                       (role == Role.RECRUITER && activeTab == 3)


            if (isChatActive) {
                val currentUserId = if (chatCurrentUserId != 0L) chatCurrentUserId else (profileState.profile?.userId ?: 0L)
                when {
                    showTeamInviteScreen != null -> {
                        TeamInviteScreen(
                            searchResults = profileState.searchResults,
                            isSearching = profileState.isSearching,
                            onBackClick = { showTeamInviteScreen = null },
                            onSearch = { q, c, b, y -> profileViewModel.searchStudents(q, c, b, y, null, null, null) },
                            onInviteClick = { username ->
                                teamViewModel.inviteUser(showTeamInviteScreen!!, username) {
                                    // Invited successfully
                                }
                            }
                        )
                    }
                    activeTeamSettingsId != null -> {
                        LaunchedEffect(activeTeamSettingsId) {
                            teamViewModel.loadTeam(activeTeamSettingsId!!)
                        }
                        TeamSettingsScreen(
                            team = activeTeam,
                            currentUserId = currentUserId,
                            onBackClick = { activeTeamSettingsId = null },
                            onInviteClick = { teamId -> showTeamInviteScreen = teamId },
                            onUpdateTeam = { teamId, name, desc, photo ->
                                teamViewModel.updateTeam(teamId, name, desc, photo) {}
                            }
                        )
                    }
                    else -> {
                        LaunchedEffect(Unit) {
                            chatViewModel.loadInbox()
                        }
                        ChatListScreen(
                            conversations = chatState,
                            isLoading = false,
                            onConversationClick = { convId ->
                                chatViewModel.openConversation(convId)
                                activeChatConversationId = convId
                            },
                            onStartDirectChatClick = {
                                showStartDirectChatDialog = true
                            },
                            onCreateTeamClick = {
                                showCreateTeamDialog = true
                            },
                            showCreateTeam = role == Role.STUDENT
                        )
                    }
                }
            } else {
                when (role) {
                    Role.STUDENT -> StudentFlow(
                        activeTab = activeTab,
                        jobViewModel = jobViewModel,
                        homeSubScreen = homeSubScreen,
                        onHomeSubScreenChange = { homeSubScreen = it },
                        profile = currentProfile,
                        profileError = profileState.error,
                        onReloadProfile = { profileViewModel.loadProfile() },
                        approvedSkills = skillState.approvedSkills,

                        skillsCatalog = skillState.skillsCatalog,
                        notifications = skillState.notifications,
                        projects = projectState.studentProjects,
                        certificates = certificateState.certificates,
                        feed = projectState.feed,
                        commentsMap = projectState.commentsMap,
                        searchResults = profileState.searchResults,
                        isSearching = profileState.isSearching,
                        searchError = profileState.searchError,
                        rankings = profileState.rankings,
                        onSearchStudents = { q, c, b, y, s, sk, bg -> profileViewModel.searchStudents(q, c, b, y, s, sk, bg) },
                        onLoadRankings = { c, b, y -> profileViewModel.loadStudentRankings(c, b, y) },
                        onMarkRead = { id -> skillViewModel.markNotificationAsRead(id) },
                        onAcceptInvite = { teamName ->
                            val invite = pendingInvites.find { it.teamName.lowercase() == teamName.lowercase() }
                            if (invite != null) {
                                teamViewModel.respondToInvite(invite.id, true) {
                                    chatViewModel.loadInbox()
                                }
                            }
                        },
                        onRejectInvite = { teamName ->
                            val invite = pendingInvites.find { it.teamName.lowercase() == teamName.lowercase() }
                            if (invite != null) {
                                teamViewModel.respondToInvite(invite.id, false) {}
                            }
                        },
                        onRequestSkill = { skillId -> skillViewModel.requestSkillVerification(skillId) },
                        onUploadProject = { name, desc, tech, gh, demo, imgs ->
                            projectViewModel.uploadProject(name, desc, tech, gh, demo, imgs)
                        },
                        onUploadCertificate = { title, org, purp, furl, ftype ->
                            certificateViewModel.uploadCertificate(title, org, purp, furl, ftype)
                        },
                        savedProjectIds = projectState.savedProjectIds,
                        onSaveProject = { id -> projectViewModel.toggleSave(id) },
                        onLogoutClick = onLogoutClick,
                        onLikeProject = { id -> projectViewModel.toggleLike(id) },
                        onCommentProject = { id, text, parentId -> projectViewModel.addComment(id, text, parentId) },
                        onLoadComments = { id -> projectViewModel.loadComments(id) },
                        onDeleteProject = { id -> projectViewModel.deleteProject(id) },
                        onLoadFeed = { filter -> projectViewModel.loadFeed(filter) },
                        onUserClick = handleUserClick,
                        onAuthorClick = handleUsernameClick,
                        onEditProfile = { isEditingProfile = true },
                        selectedProjectForDetails = selectedProjectForDetails,
                        selectedCertificateForDetails = selectedCertificateForDetails,
                        onProjectClick = { selectedProjectForDetails = it },
                        onCertificateClick = { selectedCertificateForDetails = it }
                    )
                    Role.TEACHER -> TeacherFlow(
                        activeTab = activeTab,
                        queue = skillState.teacherQueue,
                        projectsQueue = verificationState.projectsQueue,
                        certificatesQueue = verificationState.certificatesQueue,
                        analytics = verificationState.analytics,
                        rankings = profileState.rankings,
                        searchResults = profileState.searchResults,
                        isSearching = profileState.isSearching,
                        searchError = profileState.searchError,
                        notifications = skillState.notifications,
                        conversations = chatState,
                        isLoadingInbox = false,
                        inboxError = null,
                        onTabSelect = { activeTab = it },
                        onConversationClick = { convId -> chatViewModel.openConversation(convId) },
                        onRefreshInbox = { chatViewModel.loadInbox() },
                        onStartDirectChat = { username ->
                            chatViewModel.startDirectChat(username) { newConvId ->
                                chatViewModel.openConversation(newConvId)
                                activeChatConversationId = newConvId
                            }
                            activeTab = 4
                        },
                        onMarkRead = { id -> skillViewModel.markNotificationAsRead(id) },
                        onAcceptInvite = { teamName ->
                            val invite = pendingInvites.find { it.teamName.lowercase() == teamName.lowercase() }
                            if (invite != null) {
                                teamViewModel.respondToInvite(invite.id, true) {
                                    chatViewModel.loadInbox()
                                }
                            }
                        },
                        onRejectInvite = { teamName ->
                            val invite = pendingInvites.find { it.teamName.lowercase() == teamName.lowercase() }
                            if (invite != null) {
                                teamViewModel.respondToInvite(invite.id, false) {}
                            }
                        },
                        onSearch = { q, c, b, y, s, sk, bg -> profileViewModel.searchStudents(q, c, b, y, s, sk, bg) },
                        onUserClick = handleUserClick,
                        onLoadRankings = { course, branch, year -> profileViewModel.loadStudentRankings(course, branch, year) },
                        onScheduleMeeting = { id, date, notes -> skillViewModel.scheduleMeeting(id, date, notes) },
                        onApprove = { id, level, points -> skillViewModel.approveRequest(id, level, points) },
                        onReject = { id, reason -> skillViewModel.rejectRequest(id, reason) },
                        onEvaluateRequest = { id, status, remarks, points, scheduleAt ->
                            verificationViewModel.evaluateRequest(id, status, remarks, points, scheduleAt)
                        },
                        onEditProfile = { isEditingProfile = true },
                        onLogoutClick = onLogoutClick,
                        onViewStudentProfile = handleUsernameClick
                    )
                    Role.RECRUITER -> {
                        if (activeTab == 2) {
                            NotificationsListScreen(
                                notifications = skillState.notifications,
                                onMarkRead = { skillViewModel.markNotificationAsRead(it) }
                            )
                        } else {
                            RecruiterFlow(
                                activeTab = activeTab,
                                profile = currentProfile,
                                searchResults = profileState.searchResults,
                                isSearching = profileState.isSearching,
                                searchError = profileState.searchError,
                                onSearch = { q, c, b, y, s, sk, bg -> profileViewModel.searchStudents(q, c, b, y, s, sk, bg) },
                                onUserClick = handleUserClick,
                                onEditProfile = { isEditingProfile = true },
                                jobViewModel = jobViewModel,
                                onStartDirectChat = { username ->
                                    chatViewModel.startDirectChat(username) { newConvId ->
                                        chatViewModel.openConversation(newConvId)
                                        activeChatConversationId = newConvId
                                    }
                                    activeTab = 3
                                },
                                onLogoutClick = onLogoutClick
                            )
                        }
                    }
                    Role.ADMIN -> AdminFlow(
                        activeTab = activeTab,
                        onTabSelect = { activeTab = it },
                        onLogoutClick = onLogoutClick
                    )
                }
            }
            }
        }
    }
}

    activeChatConversationId?.let { convId ->
        val activeConv = chatState.find { it.id == convId }
        val currentUserId = if (chatCurrentUserId != 0L) chatCurrentUserId else (profileState.profile?.userId ?: 0L)
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color(0xFFF6F7FB))
        ) {
            ChatThreadScreen(
                conversationName = activeConv?.name ?: "Chat",
                teamId = activeConv?.teamId,
                photoUrl = activeConv?.photoUrl,
                messages = activeMessages,
                currentUserId = currentUserId,
                onBackClick = {
                    chatViewModel.closeConversation()
                    activeChatConversationId = null
                },
                onSendMessage = { content ->
                    chatViewModel.sendMessage(content)
                },
                errorMessage = chatThreadError,
                onErrorShown = { chatViewModel.clearThreadError() },
                onTeamSettingsClick = { teamId ->
                    activeTeamSettingsId = teamId
                }
            )
        }
    }


    selectedStudentProfileForDetails?.let { student ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
        ) {
            StudentProfileScreen(
                profile = student,
                showContact = role == Role.RECRUITER || role == Role.TEACHER,
                isOwnProfile = false,
                approvedSkills = visitedProfileState.approvedSkills,
                projects = visitedProfileState.projects,
                certificates = visitedProfileState.certificates,
                teacherFeedbacks = visitedProfileState.teacherFeedbacks,
                viewerRole = role,
                onAddFeedback = { content ->
                    visitedProfileViewModel.submitTeacherFeedback(student.userId, content)
                },
                onBackClick = { selectedStudentProfileForDetails = null },
                onMessageClick = { username ->
                    selectedStudentProfileForDetails = null
                    chatViewModel.startDirectChat(username) { newConvId ->
                        chatViewModel.openConversation(newConvId)
                        activeChatConversationId = newConvId
                    }
                    activeTab = if (role == Role.TEACHER) 4 else 3 // Messages is tab 4 for Teacher, tab 3 for Recruiter
                },
                onEditClick = {}
            )
        }
    }

    if (showCreateTeamDialog) {
        var teamName by remember { mutableStateOf("") }
        var teamDesc by remember { mutableStateOf("") }
        var teamLogo by remember { mutableStateOf("") }
        val fileViewModel: com.zyproo.devlink.ui.file.FileViewModel = hiltViewModel()
        val fileState by fileViewModel.uiState.collectAsState()
        val teamLogoPicker = rememberLauncherForActivityResult(
            contract = androidx.activity.result.contract.ActivityResultContracts.GetContent()
        ) { uri: android.net.Uri? ->
            uri?.let {
                fileViewModel.uploadFile(it) { uploadedUrl ->
                    teamLogo = uploadedUrl
                }
            }
        }

        AlertDialog(
            onDismissRequest = { showCreateTeamDialog = false },
            title = { Text("Create New Team") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    OutlinedTextField(value = teamName, onValueChange = { teamName = it }, label = { Text("Team Name") })
                    OutlinedTextField(value = teamDesc, onValueChange = { teamDesc = it }, label = { Text("Description") })
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        OutlinedTextField(
                            value = teamLogo,
                            onValueChange = { teamLogo = it },
                            label = { Text("Logo Image") },
                            placeholder = { Text("URL or pick from device") },
                            modifier = Modifier.weight(1f)
                        )
                        IconButton(
                            onClick = { teamLogoPicker.launch("image/*") },
                            enabled = !fileState.isUploading
                        ) {
                            if (fileState.isUploading) {
                                CircularProgressIndicator(modifier = Modifier.size(24.dp), strokeWidth = 2.dp, color = DevLinkBlue)
                            } else {
                                Icon(Icons.Default.AddPhotoAlternate, contentDescription = "Pick Logo", tint = DevLinkBlue)
                            }
                        }
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (teamName.isNotBlank()) {
                            teamViewModel.createTeam(teamName, teamDesc.ifBlank { null }, teamLogo.ifBlank { null }) {
                                showCreateTeamDialog = false
                                chatViewModel.loadInbox()
                            }
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = DevLinkBlue)
                ) { Text("Create") }
            },
            dismissButton = {
                TextButton(onClick = { showCreateTeamDialog = false }) { Text("Cancel") }
            }
        )
    }

    if (showStartDirectChatDialog) {
        StartDirectChatDialog(
            searchResults = profileState.searchResults,
            isSearching = profileState.isSearching,
            onDismissRequest = { showStartDirectChatDialog = false },
            onStartChat = { targetUsername ->
                if (targetUsername.isNotBlank()) {
                    chatViewModel.startDirectChat(targetUsername) { convId ->
                        showStartDirectChatDialog = false
                        activeChatConversationId = convId
                    }
                }
            },
            onSearch = { q, c, b, y ->
                profileViewModel.searchStudents(q, c, b, y, null, null, null)
            }
        )
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun StudentProfileDetailsDialog(
    profile: ProfileResponse,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("Close", color = DevLinkBlue, fontWeight = FontWeight.Bold)
            }
        },
        title = {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(CircleShape)
                        .background(Color(0xFFE2E8F0)),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = (profile.name ?: profile.username).take(2).uppercase(),
                        fontWeight = FontWeight.Bold,
                        color = DevLinkBlue,
                        fontSize = 14.sp
                    )
                }
                Column {
                    Text(profile.name ?: profile.username, fontSize = 16.sp, fontWeight = FontWeight.Bold)
                    Text("@${profile.username}", fontSize = 12.sp, color = Color.Gray)
                }
            }
        },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text("Course", fontWeight = FontWeight.SemiBold, fontSize = 13.sp)
                    Text("${profile.course ?: "N/A"} · Sem ${profile.semester ?: 1}", fontSize = 13.sp)
                }
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text("Branch", fontWeight = FontWeight.SemiBold, fontSize = 13.sp)
                    Text(profile.branch?.name ?: "N/A", fontSize = 13.sp)
                }
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text("Admission Year", fontWeight = FontWeight.SemiBold, fontSize = 13.sp)
                    Text(profile.admissionYear?.toString() ?: "N/A", fontSize = 13.sp)
                }

                HorizontalDivider(color = Color(0xFFF1F1F1))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text("Total Points", fontWeight = FontWeight.SemiBold, fontSize = 13.sp)
                    Text("${profile.totalPoints ?: 0} points", color = DevLinkBlue, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                }
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text("Leaderboard Rank", fontWeight = FontWeight.SemiBold, fontSize = 13.sp)
                    Text("#${profile.rank ?: "-"}", color = DevLinkBlue, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                }

                if (profile.badges.isNotEmpty()) {
                    HorizontalDivider(color = Color(0xFFF1F1F1))
                    Text("Badges", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                    FlowRow(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        profile.badges.forEach { badge ->
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(6.dp))
                                    .background(Color(0xFFF1F5FE))
                                    .padding(horizontal = 8.dp, vertical = 2.dp)
                            ) {
                                Text(badge, fontSize = 10.sp, color = DevLinkBlue, fontWeight = FontWeight.SemiBold)
                            }
                        }
                    }
                }

                profile.latestFeedback?.let { fb ->
                    HorizontalDivider(color = Color(0xFFF1F1F1))
                    Text("Latest Teacher Feedback", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                    Card(
                        colors = CardDefaults.cardColors(containerColor = Color(0xFFF8FAFC)),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Column(modifier = Modifier.padding(10.dp)) {
                            Text(
                                text = "Verified by @${fb.teacherUsername} (${fb.department})",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = DevLinkBlue
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(fb.content, fontSize = 12.sp, color = Color.DarkGray)
                        }
                    }
                }
            }
        }
    )
}

@Composable
fun TeacherPendingSectionsMenu(
    skillsCount: Int,
    projectsCount: Int,
    certificatesCount: Int,
    onSkillClick: () -> Unit,
    onProjectClick: () -> Unit,
    onCertificateClick: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF6F7FB))
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text(
            text = "Pending Evaluations",
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold,
            color = Color.Black
        )

        PendingCategoryCard(
            title = "Pending Projects",
            count = projectsCount,
            icon = Icons.Default.Folder,
            onClick = onProjectClick
        )

        PendingCategoryCard(
            title = "Skill Verification",
            count = skillsCount,
            icon = Icons.Default.Build,
            onClick = onSkillClick
        )

        PendingCategoryCard(
            title = "Certificate Approvals",
            count = certificatesCount,
            icon = Icons.Default.CardMembership,
            onClick = onCertificateClick
        )
    }
}

@Composable
fun PendingCategoryCard(
    title: String,
    count: Int,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(CircleShape)
                        .background(Color(0xFFF1F5FE)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(imageVector = icon, contentDescription = null, tint = DevLinkBlue)
                }
                Spacer(modifier = Modifier.width(16.dp))
                Text(
                    text = title,
                    fontWeight = FontWeight.Bold,
                    fontSize = 15.sp,
                    color = Color.Black
                )
            }
            Box(
                modifier = Modifier
                    .clip(CircleShape)
                    .background(if (count > 0) Color.Red else Color.LightGray)
                    .padding(horizontal = 8.dp, vertical = 4.dp)
            ) {
                Text(
                    text = count.toString(),
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    fontSize = 11.sp
                )
            }
        }
    }
}

@Composable
fun StudentFlow(
    activeTab: Int,
    jobViewModel: JobViewModel,
    homeSubScreen: String,
    onHomeSubScreenChange: (String) -> Unit,
    profile: ProfileResponse?,
    profileError: String? = null,
    onReloadProfile: () -> Unit = {},
    approvedSkills: List<StudentSkillResponse>,
    skillsCatalog: List<SkillResponse>,
    notifications: List<NotificationResponse>,
    projects: List<ProjectResponse>,
    certificates: List<CertificateResponse>,
    feed: List<ProjectResponse>,
    commentsMap: Map<Long, List<ProjectCommentResponse>>,
    searchResults: List<ProfileResponse>,
    isSearching: Boolean,
    searchError: String?,
    rankings: List<ProfileResponse>,
    onSearchStudents: (String?, Course?, Branch?, Int?, Int?, String?, String?) -> Unit,
    onLoadRankings: (Course?, Branch?, Int?) -> Unit,
    onMarkRead: (Long) -> Unit,
    onAcceptInvite: (String) -> Unit,
    onRejectInvite: (String) -> Unit,
    onRequestSkill: (Long) -> Unit,
    onUploadProject: (String, String, String, String?, String?, List<String>?) -> Unit,
    onUploadCertificate: (String, String, String, String, FileType) -> Unit,
    savedProjectIds: Set<Long> = emptySet(),
    onSaveProject: (Long) -> Unit = {},
    onLogoutClick: () -> Unit = {},
    onLikeProject: (Long) -> Unit,
    onCommentProject: (Long, String, Long?) -> Unit,
    onLoadComments: (Long) -> Unit,
    onDeleteProject: (Long) -> Unit,
    onLoadFeed: (String) -> Unit,
    onUserClick: (ProfileResponse) -> Unit,
    onAuthorClick: ((String) -> Unit)? = null,
    onEditProfile: () -> Unit,
    selectedProjectForDetails: ProjectResponse?,
    selectedCertificateForDetails: CertificateResponse?,
    onProjectClick: (ProjectResponse?) -> Unit,
    onCertificateClick: (CertificateResponse?) -> Unit
) {

    when (activeTab) {
        0 -> {
            when (homeSubScreen) {
                "search" -> {
                    StudentSearchScreen(
                        searchResults = searchResults,
                        isSearching = isSearching,
                        searchError = searchError,
                        onSearch = onSearchStudents,
                        onUserClick = onUserClick
                    )
                }
                "leaderboard" -> {
                    TeacherAnalyticsScreen(
                        analytics = null,
                        rankings = rankings,
                        onFilterChange = onLoadRankings,
                        onUserClick = onUserClick
                    )
                }
                else -> {
                    StudentFeedScreen(
                        feed = feed,
                        commentsMap = commentsMap,
                        savedProjectIds = savedProjectIds,
                        onLikeProject = onLikeProject,
                        onSaveProject = onSaveProject,
                        onCommentProject = onCommentProject,
                        onLoadComments = onLoadComments,
                        onLoadFeed = onLoadFeed,
                        onProjectClick = { onProjectClick(it) },
                        onAuthorClick = onAuthorClick
                    )
                }
            }
        }
        1 -> {
            StudentJobsScreen(
                jobViewModel = jobViewModel,
                approvedSkills = approvedSkills,
                projects = projects
            )
        }
        2 -> {
            NotificationsListScreen(
                notifications = notifications,
                onMarkRead = onMarkRead,
                onAcceptInvite = onAcceptInvite,
                onRejectInvite = onRejectInvite
            )
        }
        4 -> {
            if (profile != null) {
                StudentProfileScreen(
                    profile = profile,
                    showContact = false,
                    approvedSkills = approvedSkills,
                    skillsCatalog = skillsCatalog,
                    projects = projects,
                    certificates = certificates,
                    onRequestSkill = onRequestSkill,
                    onDeleteProject = onDeleteProject,
                    onProjectClick = { onProjectClick(it) },
                    onCertificateClick = { onCertificateClick(it) },
                    onUploadProject = onUploadProject,
                    onUploadCertificate = onUploadCertificate,
                    onLogoutClick = onLogoutClick,
                    onEditClick = onEditProfile
                )
            } else {
                ProfileErrorScreen(
                    errorMessage = profileError ?: "Something went wrong (HTTP 403). Your session may have expired.",
                    onRetryClick = onReloadProfile,
                    onLogoutClick = onLogoutClick
                )
            }
        }
        else -> PlaceholderHomeScreen("Student Tab $activeTab")
    }

    selectedProjectForDetails?.let { proj ->
        val currentProj = feed.find { it.id == proj.id } ?: projects.find { it.id == proj.id } ?: proj
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
        ) {
            ProjectDetailsScreen(
                project = currentProj,
                comments = commentsMap[proj.id] ?: emptyList(),
                isSaved = savedProjectIds.contains(proj.id),
                onBackClick = { onProjectClick(null) },
                onLike = { onLikeProject(proj.id) },
                onSave = { onSaveProject(proj.id) },
                onAddComment = { text, parentId -> onCommentProject(proj.id, text, parentId) },
                onLoadComments = { onLoadComments(proj.id) },
                onAuthorClick = onAuthorClick
            )
        }
    }

    selectedCertificateForDetails?.let { cert ->
        CertificateDetailsDialog(
            certificate = cert,
            onDismiss = { onCertificateClick(null) }
        )
    }
}

@Composable
fun TeacherFlow(
    activeTab: Int,
    queue: List<SkillRequestResponse>,
    projectsQueue: List<VerificationRequestResponse>,
    certificatesQueue: List<VerificationRequestResponse>,
    analytics: TeacherAnalyticsResponse?,
    rankings: List<ProfileResponse>,
    searchResults: List<ProfileResponse>,
    isSearching: Boolean,
    searchError: String?,
    notifications: List<NotificationResponse>,
    conversations: List<ConversationResponse> = emptyList(),
    isLoadingInbox: Boolean = false,
    inboxError: String? = null,
    onTabSelect: (Int) -> Unit,
    onConversationClick: (Long) -> Unit = {},
    onRefreshInbox: () -> Unit = {},
    onStartDirectChat: (String) -> Unit = {},
    onMarkRead: (Long) -> Unit,
    onAcceptInvite: (String) -> Unit,
    onRejectInvite: (String) -> Unit,
    onSearch: (String?, Course?, Branch?, Int?, Int?, String?, String?) -> Unit,
    onUserClick: (ProfileResponse) -> Unit,
    onLoadRankings: (Course?, Branch?, Int?) -> Unit,
    onScheduleMeeting: (Long, String, String) -> Unit,
    onApprove: (Long, SkillLevel, Int) -> Unit,
    onReject: (Long, String) -> Unit,
    onEvaluateRequest: (Long, VerificationStatus, String?, Int?, String?) -> Unit,
    onEditProfile: () -> Unit,
    onLogoutClick: () -> Unit,
    onViewStudentProfile: (String) -> Unit = {}
) {
    var selectedCategory by remember { mutableStateOf<String?>(null) }
    var targetProjectIdForReview by remember { mutableStateOf<Long?>(null) }
    var targetCertificateIdForReview by remember { mutableStateOf<Long?>(null) }
    var lastTab by remember { mutableStateOf(activeTab) }

    LaunchedEffect(activeTab) {
        if (activeTab != 1) {
            selectedCategory = null
            targetProjectIdForReview = null
            targetCertificateIdForReview = null
        }
        lastTab = activeTab
    }

    when (activeTab) {
        0 -> {
            // Dashboard – pending verification requests count, recent student activity, quick stats at a glance
            TeacherDashboardHome(
                skillsCount = queue.size,
                projectsCount = projectsQueue.size,
                certificatesCount = certificatesQueue.size,
                skillsQueue = queue,
                projectsQueue = projectsQueue,
                certificatesQueue = certificatesQueue,
                analytics = analytics,
                onNavigateToVerificationCenter = { category ->
                    selectedCategory = category
                    onTabSelect(1)
                },
                onNavigateToStudents = { onTabSelect(2) },
                onNavigateToAnalytics = { onTabSelect(3) },
                onNavigateToMessages = { onTabSelect(4) },
                onReviewProject = { projectReq ->
                    selectedCategory = "PROJECT"
                    targetProjectIdForReview = projectReq.id
                    onTabSelect(1)
                },
                onReviewCertificate = { certReq ->
                    selectedCategory = "CERTIFICATE"
                    targetCertificateIdForReview = certReq.id
                    onTabSelect(1)
                }
            )
        }
        1 -> {
            // Verification Center – the core teacher task: queue of submitted skills, certificates, and projects awaiting review/approval
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color(0xFFF6F7FB))
            ) {
                if (selectedCategory == null) {
                    TeacherPendingSectionsMenu(
                        skillsCount = queue.size,
                        projectsCount = projectsQueue.size,
                        certificatesCount = certificatesQueue.size,
                        onSkillClick = { selectedCategory = "SKILL" },
                        onProjectClick = { selectedCategory = "PROJECT" },
                        onCertificateClick = { selectedCategory = "CERTIFICATE" }
                    )
                } else {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(Color.White)
                            .padding(horizontal = 16.dp, vertical = 12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        IconButton(onClick = {
                            selectedCategory = null
                            targetProjectIdForReview = null
                            targetCertificateIdForReview = null
                        }) {
                            Icon(
                                imageVector = Icons.Default.ArrowBack,
                                contentDescription = "Back",
                                tint = DevLinkTextPrimary
                            )
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = when (selectedCategory) {
                                "SKILL" -> "Pending Skills Queue"
                                "PROJECT" -> "Pending Projects Queue"
                                else -> "Pending Certificates Queue"
                            },
                            fontWeight = FontWeight.Bold,
                            fontSize = 18.sp,
                            color = DevLinkTextPrimary
                        )
                    }
                    HorizontalDivider(color = DevLinkBorder)

                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .background(Color(0xFFF6F7FB))
                    ) {
                        when (selectedCategory) {
                            "SKILL" -> TeacherEvaluationQueueScreen(
                                queue = queue,
                                onScheduleMeeting = onScheduleMeeting,
                                onApprove = onApprove,
                                onReject = onReject,
                                onMessageStudent = { username ->
                                    onStartDirectChat(username)
                                    onTabSelect(4)
                                },
                                onViewStudentProfile = onViewStudentProfile,
                                rankings = rankings
                            )
                            "PROJECT" -> TeacherProjectReviewList(
                                queue = projectsQueue,
                                initialSelectedRequestId = targetProjectIdForReview,
                                onEvaluate = onEvaluateRequest,
                                onMessageStudent = { username ->
                                    onStartDirectChat(username)
                                    onTabSelect(4)
                                },
                                onViewStudentProfile = onViewStudentProfile,
                                onClearSelectedRequest = { targetProjectIdForReview = null },
                                rankings = rankings
                            )
                            "CERTIFICATE" -> TeacherCertificateReviewList(
                                queue = certificatesQueue,
                                initialSelectedRequestId = targetCertificateIdForReview,
                                onEvaluate = onEvaluateRequest,
                                onMessageStudent = { username ->
                                    onStartDirectChat(username)
                                    onTabSelect(4)
                                },
                                onViewStudentProfile = onViewStudentProfile,
                                onClearSelectedRequest = { targetCertificateIdForReview = null },
                                rankings = rankings
                            )
                        }
                    }
                }
            }
        }
        2 -> {
            // Students – browse/search students by branch, year, skills, ranking to review profiles and track progress
            StudentSearchScreen(
                searchResults = searchResults,
                isSearching = isSearching,
                searchError = searchError,
                onSearch = onSearch,
                onUserClick = onUserClick,
                recruiterMode = false
            )
        }
        3 -> {
            // Analytics – verified vs. unverified stats, top-ranked students, popular skills, project counts
            TeacherAnalyticsScreen(
                analytics = analytics,
                rankings = rankings,
                onFilterChange = onLoadRankings
            )
        }
        4 -> {
            // Messages – chat with students for verification follow-ups, clarifications, or mentorship
            com.zyproo.devlink.ui.chat.ChatListScreen(
                conversations = conversations,
                isLoading = isLoadingInbox,
                onConversationClick = onConversationClick,
                onStartDirectChatClick = { onStartDirectChat("") },
                onCreateTeamClick = {},
                showCreateTeam = false
            )
        }
        else -> PlaceholderHomeScreen("Teacher Tab $activeTab")
    }
}



@Composable
fun NotificationsListScreen(
    notifications: List<NotificationResponse>,
    onMarkRead: (Long) -> Unit,
    onAcceptInvite: (String) -> Unit = {},
    onRejectInvite: (String) -> Unit = {}
) {
    // Helper to group
    fun getNotificationGroup(createdAtStr: String): String {
        return try {
            val instant = java.time.Instant.parse(createdAtStr)
            val zone = java.time.ZoneId.systemDefault()
            val notifDate = instant.atZone(zone).toLocalDate()
            val today = java.time.LocalDate.now(zone)
            val yesterday = today.minusDays(1)
            
            when {
                notifDate.isEqual(today) -> "Today"
                notifDate.isEqual(yesterday) -> "Yesterday"
                notifDate.isAfter(today.minusDays(7)) -> "This Week"
                else -> "Earlier"
            }
        } catch (e: Exception) {
            "Earlier"
        }
    }

    // Helper to format time
    fun formatNotificationTime(createdAtStr: String): String {
        return try {
            val instant = java.time.Instant.parse(createdAtStr)
            val zone = java.time.ZoneId.systemDefault()
            val notifDate = instant.atZone(zone).toLocalDate()
            val today = java.time.LocalDate.now(zone)
            val yesterday = today.minusDays(1)
            
            val timeStr = java.time.format.DateTimeFormatter.ofPattern("hh:mm a")
                .withZone(zone)
                .format(instant)
                
            when {
                notifDate.isEqual(today) -> timeStr
                notifDate.isEqual(yesterday) -> "Yesterday at $timeStr"
                else -> {
                    val dateStr = java.time.format.DateTimeFormatter.ofPattern("MMM dd")
                        .withZone(zone)
                        .format(instant)
                    "$dateStr at $timeStr"
                }
            }
        } catch (e: Exception) {
            ""
        }
    }

    // Helper to get icon
    fun getNotificationIcon(title: String): androidx.compose.ui.graphics.vector.ImageVector {
        val t = title.lowercase()
        return when {
            t.contains("approved") || t.contains("verified") -> Icons.Default.CheckCircle
            t.contains("badge") || t.contains("earned") || t.contains("award") -> Icons.Default.Star
            t.contains("rejected") || t.contains("fail") -> Icons.Default.Cancel
            t.contains("scheduled") || t.contains("talk") || t.contains("meeting") -> Icons.Default.Info
            else -> Icons.Default.Notifications
        }
    }

    // Helper to get icon color
    fun getNotificationIconColor(title: String): Color {
        val t = title.lowercase()
        return when {
            t.contains("approved") || t.contains("verified") -> Color(0xFF10B981)
            t.contains("badge") || t.contains("earned") || t.contains("award") -> Color(0xFFF59E0B)
            t.contains("rejected") || t.contains("fail") -> Color(0xFFEF4444)
            t.contains("scheduled") || t.contains("talk") || t.contains("meeting") -> Color(0xFF3B82F6)
            else -> Color.Gray
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF6F7FB))
    ) {
        if (notifications.isEmpty()) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text("No notifications.", color = Color.Gray)
            }
        } else {
            val grouped = notifications.groupBy { getNotificationGroup(it.createdAt) }
            val groupOrder = listOf("Today", "Yesterday", "This Week", "Earlier")

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                groupOrder.forEach { groupName ->
                    val notifs = grouped[groupName]
                    if (!notifs.isNullOrEmpty()) {
                        Text(
                            text = groupName,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF64748B),
                            modifier = Modifier.padding(vertical = 4.dp)
                        )
                        notifs.forEach { notif ->
                            Card(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable { if (!notif.read) onMarkRead(notif.id) },
                                colors = CardDefaults.cardColors(
                                    containerColor = if (notif.read) Color.White else Color(0xFFF1F5FE)
                                ),
                                shape = RoundedCornerShape(12.dp),
                                border = BorderStroke(1.dp, Color(0xFFE2E8F0))
                            ) {
                                Row(
                                    modifier = Modifier.padding(16.dp),
                                    verticalAlignment = Alignment.Top
                                ) {
                                    Icon(
                                        imageVector = getNotificationIcon(notif.title),
                                        contentDescription = null,
                                        tint = getNotificationIconColor(notif.title),
                                        modifier = Modifier.size(24.dp)
                                    )
                                    Spacer(modifier = Modifier.width(12.dp))
                                    Column(modifier = Modifier.weight(1f)) {
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Text(
                                                text = notif.title,
                                                fontWeight = FontWeight.Bold,
                                                fontSize = 14.sp,
                                                color = Color(0xFF1E293B),
                                                modifier = Modifier.weight(1f)
                                            )
                                            val timeFormatted = formatNotificationTime(notif.createdAt)
                                            if (timeFormatted.isNotEmpty()) {
                                                Text(
                                                    text = timeFormatted,
                                                    fontSize = 10.sp,
                                                    color = Color(0xFF94A3B8),
                                                    modifier = Modifier.padding(start = 8.dp)
                                                )
                                            }
                                        }
                                        Spacer(modifier = Modifier.height(4.dp))
                                        Text(
                                            text = notif.message,
                                            fontSize = 13.sp,
                                            color = Color(0xFF475569)
                                        )

                                        val matchResult = Regex("invited you in the group '(.*)'").find(notif.message)
                                        val teamName = matchResult?.groups?.get(1)?.value
                                        if (teamName != null && !notif.read) {
                                            Row(
                                                modifier = Modifier.fillMaxWidth().padding(top = 10.dp),
                                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                                            ) {
                                                Button(
                                                    onClick = {
                                                        onAcceptInvite(teamName)
                                                        onMarkRead(notif.id)
                                                    },
                                                    colors = ButtonDefaults.buttonColors(containerColor = DevLinkBlue),
                                                    shape = RoundedCornerShape(16.dp),
                                                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp)
                                                ) {
                                                    Text("Accept", fontSize = 11.sp)
                                                }
                                                OutlinedButton(
                                                    onClick = {
                                                        onRejectInvite(teamName)
                                                        onMarkRead(notif.id)
                                                    },
                                                    shape = RoundedCornerShape(16.dp),
                                                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp)
                                                ) {
                                                    Text("Reject", fontSize = 11.sp, color = Color.Red)
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
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TeacherEvaluationQueueScreen(
    queue: List<SkillRequestResponse>,
    onScheduleMeeting: (Long, String, String) -> Unit,
    onApprove: (Long, SkillLevel, Int) -> Unit,
    onReject: (Long, String) -> Unit,
    onMessageStudent: (String) -> Unit = {},
    onViewStudentProfile: (String) -> Unit = {},
    rankings: List<ProfileResponse> = emptyList()
) {
    var selectedRequest by remember { mutableStateOf<SkillRequestResponse?>(null) }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF6F7FB))
    ) {
        if (queue.isEmpty()) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text("No pending evaluation requests.", color = Color.Gray)
            }
        } else {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text("Pending Requests", fontSize = 18.sp, fontWeight = FontWeight.Bold)
                queue.forEach { req ->
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
                                Column {
                                    Text(
                                        text = req.studentName.ifBlank { req.studentUsername },
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 14.sp,
                                        color = DevLinkTextPrimary
                                    )
                                    Text(
                                        text = "@${req.studentUsername}",
                                        fontSize = 11.sp,
                                        color = DevLinkTextSecondary
                                    )
                                }
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(
                                            if (req.status == SkillStatus.MEET) Color(0xFFFFF3CD)
                                            else DevLinkBlueContainer
                                        )
                                        .padding(horizontal = 8.dp, vertical = 4.dp)
                                ) {
                                    Text(
                                        text = req.status.name,
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.SemiBold,
                                        color = if (req.status == SkillStatus.MEET) Color(0xFF856404) else DevLinkBlue
                                    )
                                }
                            }
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = "Skill Claim: \"${req.skillName}\"",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold,
                                color = DevLinkTextPrimary
                            )
                            if (req.status == SkillStatus.MEET && !req.meetingNotes.isNullOrBlank()) {
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = "Scheduled: ${req.meetingDate?.take(10) ?: ""} - ${req.meetingNotes}",
                                    fontSize = 12.sp,
                                    color = Color.Gray
                                )
                            }
                            Spacer(modifier = Modifier.height(8.dp))
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                val dateFormatted = req.createdAt.take(16).replace("T", " ")
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
                                        text = "Submitted: $dateFormatted",
                                        fontSize = 11.sp,
                                        color = DevLinkTextSecondary
                                    )
                                }
                                Text(
                                    text = "Tap to Evaluate →",
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

        // Decision Overlay Dialog
        selectedRequest?.let { req ->
            var action by remember { mutableStateOf("Approved") }
            var pointsVal by remember { mutableIntStateOf(10) }
            var pointsStr by remember { mutableStateOf("10") }
            var reason by remember { mutableStateOf("") }
            var meetingDateStr by remember { mutableStateOf("2026-08-10T10:00:00Z") }
            var meetingNotes by remember { mutableStateOf("") }

            AlertDialog(
                onDismissRequest = { selectedRequest = null },
                confirmButton = {
                    Button(
                        onClick = {
                            onMessageStudent(req.studentUsername)
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
                                when (action) {
                                    "Approved" -> {
                                        val points = pointsVal
                                        val level = when {
                                            points <= 10 -> SkillLevel.LOW
                                            points <= 20 -> SkillLevel.MID
                                            else -> SkillLevel.HIGH
                                        }
                                        onApprove(req.id, level, points)
                                    }
                                    "Rejected" -> {
                                        onReject(req.id, reason.ifBlank { "Does not meet requirements." })
                                    }
                                    else -> { // Request More Info
                                        onScheduleMeeting(req.id, meetingDateStr, meetingNotes.ifBlank { "Discussion" })
                                    }
                                }
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
                        Text("Evaluate Request", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = DevLinkTextPrimary)
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = "Student: @${req.studentUsername} (View Profile)",
                            fontSize = 12.sp,
                            color = DevLinkBlue,
                            fontWeight = FontWeight.SemiBold,
                            modifier = Modifier.clickable {
                                onViewStudentProfile(req.studentUsername)
                                selectedRequest = null
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
                        val studentProfile = rankings.find { it.username == req.studentUsername }
                        val photoUrl = studentProfile?.photoUrl?.toFullMediaUrl()
                        val studentName = req.studentName
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

                        Text(
                            text = "Decide status for @${req.studentUsername}'s request on \"${req.skillName}\"",
                            fontSize = 13.sp,
                            color = DevLinkTextSecondary
                        )

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

                        when (action) {
                            "Approved" -> {
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
                                Text(
                                    text = "Easy (1-10), Medium (11-20), Hard (21-30)",
                                    fontSize = 11.sp,
                                    color = Color.Gray,
                                    fontWeight = FontWeight.Medium
                                )
                            }
                            "Rejected" -> {
                                OutlinedTextField(
                                    value = reason,
                                    onValueChange = { reason = it },
                                    label = { Text("Rejection Reason") },
                                    modifier = Modifier.fillMaxWidth(),
                                    maxLines = 3
                                )
                            }
                            "Request More Info" -> {
                                com.zyproo.devlink.ui.common.MeetingSchedulePicker(
                                    initialIsoString = meetingDateStr,
                                    onIsoStringChange = { meetingDateStr = it }
                                )
                                OutlinedTextField(
                                    value = meetingNotes,
                                    onValueChange = { meetingNotes = it },
                                    label = { Text("Discussion Topic / Questions") },
                                    modifier = Modifier.fillMaxWidth(),
                                    maxLines = 3
                                )
                            }
                        }
                    }
                }
            )
        }
    }
}

@Composable
fun PointsStepper(
    value: Int,
    onValueChange: (Int) -> Unit,
    minValue: Int = 1,
    maxValue: Int = 100
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        OutlinedIconButton(
            onClick = { if (value > minValue) onValueChange(value - 1) },
            modifier = Modifier.size(36.dp),
            shape = RoundedCornerShape(8.dp)
        ) {
            Text("-", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = DevLinkBlue)
        }
        
        Text(
            text = value.toString(),
            fontSize = 16.sp,
            fontWeight = FontWeight.Bold,
            color = Color.Black,
            modifier = Modifier.widthIn(min = 32.dp),
            textAlign = TextAlign.Center
        )

        OutlinedIconButton(
            onClick = { if (value < maxValue) onValueChange(value + 1) },
            modifier = Modifier.size(36.dp),
            shape = RoundedCornerShape(8.dp)
        ) {
            Text("+", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = DevLinkBlue)
        }
    }
}

@Composable
fun ProfileErrorScreen(
    errorMessage: String?,
    onRetryClick: () -> Unit,
    onLogoutClick: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(DevLinkBackground)
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Surface(
            shape = CircleShape,
            color = MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.2f),
            modifier = Modifier.size(80.dp)
        ) {
            Box(contentAlignment = Alignment.Center) {
                Icon(
                    imageVector = Icons.Default.Lock,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.error,
                    modifier = Modifier.size(40.dp)
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = "Profile Unavailable",
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
            color = DevLinkTextPrimary
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = errorMessage ?: "Something went wrong (HTTP 403). Your session may have expired.",
            style = MaterialTheme.typography.bodyMedium,
            color = DevLinkTextSecondary,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(24.dp))

        Row(
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            OutlinedButton(
                onClick = onRetryClick,
                shape = RoundedCornerShape(8.dp)
            ) {
                Icon(Icons.Default.Refresh, contentDescription = null, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Text("Retry")
            }

            Button(
                onClick = onLogoutClick,
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error),
                shape = RoundedCornerShape(8.dp)
            ) {
                Icon(Icons.Default.ExitToApp, contentDescription = null, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Text("Log Out")
            }
        }
    }
}


