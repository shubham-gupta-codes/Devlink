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
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.hilt.navigation.compose.hiltViewModel
import com.zyproo.devlink.data.remote.dto.*
import com.zyproo.devlink.ui.profile.AdminViewModel
import com.zyproo.devlink.ui.theme.*

@Composable
fun AdminFlow(
    activeTab: Int,
    onTabSelect: (Int) -> Unit,
    onLogoutClick: () -> Unit = {}
) {
    val adminViewModel: AdminViewModel = hiltViewModel()
    val state by adminViewModel.uiState.collectAsState()

    LaunchedEffect(Unit) {
        adminViewModel.loadAll()
    }

    // Show snackbar for success/error
    val snackbarHostState = remember { SnackbarHostState() }
    LaunchedEffect(state.successMessage, state.error) {
        state.successMessage?.let {
            snackbarHostState.showSnackbar(it)
            adminViewModel.clearMessages()
        }
        state.error?.let {
            snackbarHostState.showSnackbar(it)
            adminViewModel.clearMessages()
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        when (activeTab) {
            0 -> AdminDashboardTab(
                stats = state.stats,
                isLoading = state.isLoading,
                onNavigateToUsers = { onTabSelect(1) },
                onNavigateToApprovals = { onTabSelect(2) }
            )
            1 -> AdminUsersTab(
                users = state.users,
                isLoading = state.isLoading,
                isActionLoading = state.isActionLoading,
                onSuspend = { adminViewModel.suspendUser(it) },
                onReactivate = { adminViewModel.reactivateUser(it) },
                onDelete = { adminViewModel.deleteUser(it) },
                onFilter = { role, enabled -> adminViewModel.loadUsers(role, enabled) },
                onCreateUser = { req -> adminViewModel.createUser(req) }
            )
            2 -> AdminAuditLogsTab(
                isLoading = state.isLoading
            )
            3 -> AdminAnalyticsTab(stats = state.stats, isLoading = state.isLoading)
            4 -> AdminSettingsTab(onLogoutClick = onLogoutClick)
        }

        SnackbarHost(
            hostState = snackbarHostState,
            modifier = Modifier.align(Alignment.BottomCenter)
        )
    }
}

// ─────────────────────────────────────────────────────────────
// TAB 0: DASHBOARD
// ─────────────────────────────────────────────────────────────
@Composable
fun AdminDashboardTab(
    stats: AdminDashboardStats?,
    isLoading: Boolean,
    onNavigateToUsers: () -> Unit,
    onNavigateToApprovals: () -> Unit
) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(DevLinkBackground),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Welcome header
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = DevLinkBlue),
                elevation = CardDefaults.cardElevation(0.dp)
            ) {
                Column(modifier = Modifier.padding(20.dp)) {
                    Text(
                        text = "Welcome back, Admin",
                        fontSize = 22.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(text = "Create user accounts, manage platform settings & review audit logs.", fontSize = 13.sp, color = Color.White.copy(alpha = 0.85f))
                }
            }
        }

        // Stats Grid
        item {
            if (isLoading) {
                Box(modifier = Modifier.fillMaxWidth().height(160.dp), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = DevLinkBlue)
                }
            } else if (stats == null) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    border = BorderStroke(1.dp, DevLinkBorder)
                ) {
                    Text("Stats overview unavailable. Tap refresh to reload.", fontSize = 13.sp, color = DevLinkTextSecondary, modifier = Modifier.padding(16.dp))
                }
            } else {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text("Platform Overview", fontWeight = FontWeight.Bold, fontSize = 15.sp, color = DevLinkTextPrimary)
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        AdminStatCard(
                            modifier = Modifier.weight(1f),
                            label = "Students",
                            value = stats.totalStudents.toString(),
                            icon = Icons.Default.School,
                            color = Color(0xFF1E40AF),
                            bgColor = Color(0xFFDBEAFE)
                        )
                        AdminStatCard(
                            modifier = Modifier.weight(1f),
                            label = "Teachers",
                            value = stats.totalTeachers.toString(),
                            icon = Icons.Default.Person,
                            color = Color(0xFF065F46),
                            bgColor = Color(0xFFD1FAE5)
                        )
                        AdminStatCard(
                            modifier = Modifier.weight(1f),
                            label = "Recruiters",
                            value = stats.totalRecruiters.toString(),
                            icon = Icons.Default.BusinessCenter,
                            color = Color(0xFF92400E),
                            bgColor = Color(0xFFFEF3C7)
                        )
                    }
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        AdminStatCard(
                            modifier = Modifier.weight(1f),
                            label = "Projects",
                            value = stats.totalProjects.toString(),
                            icon = Icons.Default.Folder,
                            color = Color(0xFF5B21B6),
                            bgColor = Color(0xFFEDE9FE)
                        )
                        AdminStatCard(
                            modifier = Modifier.weight(1f),
                            label = "Certs Verified",
                            value = stats.totalVerifiedCertificates.toString(),
                            icon = Icons.Default.CardMembership,
                            color = Color(0xFF0E7490),
                            bgColor = Color(0xFFCFFAFE)
                        )
                        AdminStatCard(
                            modifier = Modifier.weight(1f),
                            label = "Audit Logs",
                            value = "Active",
                            icon = Icons.Default.ReceiptLong,
                            color = Color(0xFF0E7490),
                            bgColor = Color(0xFFCFFAFE),
                            onClick = onNavigateToApprovals
                        )
                    }
                }
            }
        }

        // Quick Actions
        item {
            Text("Quick Actions", fontWeight = FontWeight.Bold, fontSize = 15.sp, color = DevLinkTextPrimary)
            Spacer(modifier = Modifier.height(8.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                AdminShortcutCard(
                    modifier = Modifier.weight(1f),
                    label = "Manage Users",
                    icon = Icons.Default.ManageAccounts,
                    onClick = onNavigateToUsers
                )
                AdminShortcutCard(
                    modifier = Modifier.weight(1f),
                    label = "Approvals",
                    icon = Icons.Default.Approval,
                    onClick = onNavigateToApprovals
                )
            }
        }
    }
}

@Composable
fun AdminStatCard(
    modifier: Modifier = Modifier,
    label: String,
    value: String,
    icon: ImageVector,
    color: Color,
    bgColor: Color,
    onClick: (() -> Unit)? = null
) {
    Card(
        modifier = modifier.then(if (onClick != null) Modifier.clickable { onClick() } else Modifier),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = bgColor),
        border = BorderStroke(1.dp, color.copy(alpha = 0.2f)),
        elevation = CardDefaults.cardElevation(0.dp)
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = color,
                modifier = Modifier.size(20.dp)
            )
            Spacer(modifier = Modifier.height(6.dp))
            Text(text = value, fontSize = 20.sp, fontWeight = FontWeight.Bold, color = color)
            Text(text = label, fontSize = 10.sp, color = color.copy(alpha = 0.8f), maxLines = 1, overflow = TextOverflow.Ellipsis)
        }
    }
}

@Composable
fun AdminShortcutCard(
    modifier: Modifier = Modifier,
    label: String,
    icon: ImageVector,
    onClick: () -> Unit
) {
    Card(
        modifier = modifier.clickable { onClick() },
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        border = BorderStroke(1.dp, DevLinkBorder),
        elevation = CardDefaults.cardElevation(0.dp)
    ) {
        Row(
            modifier = Modifier.padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(imageVector = icon, contentDescription = null, tint = DevLinkBlue, modifier = Modifier.size(18.dp))
            Spacer(modifier = Modifier.width(8.dp))
            Text(label, fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = DevLinkTextPrimary)
        }
    }
}

// ─────────────────────────────────────────────────────────────
// TAB 1: USERS
// ─────────────────────────────────────────────────────────────
@Composable
fun AdminUsersTab(
    users: List<AdminUserListItem>,
    isLoading: Boolean,
    isActionLoading: Boolean,
    onSuspend: (Long) -> Unit,
    onReactivate: (Long) -> Unit,
    onDelete: (Long) -> Unit,
    onFilter: (String?, Boolean?) -> Unit,
    onCreateUser: (CreateUserAdminRequest) -> Unit
) {
    var showCreateDialog by remember { mutableStateOf(false) }
    var selectedUser by remember { mutableStateOf<AdminUserListItem?>(null) }
    var selectedRoleFilter by remember { mutableStateOf<String?>(null) }
    var selectedEnabledFilter by remember { mutableStateOf<Boolean?>(null) }
    var searchQuery by remember { mutableStateOf("") }

    val roleOptions = listOf(null, "STUDENT", "TEACHER", "RECRUITER")
    val enabledOptions = listOf<Boolean?>(null, true, false)

    val filteredUsers = users.filter { user ->
        searchQuery.isBlank() || user.username.contains(searchQuery, ignoreCase = true)
                || user.email.contains(searchQuery, ignoreCase = true)
                || user.displayName?.contains(searchQuery, ignoreCase = true) == true
    }

    if (showCreateDialog) {
        CreateUserDialog(
            onDismiss = { showCreateDialog = false },
            onCreate = { req ->
                onCreateUser(req)
                showCreateDialog = false
            }
        )
    }

    if (selectedUser != null) {
        AdminUserDetailDialog(
            user = selectedUser!!,
            isActionLoading = isActionLoading,
            onSuspend = { onSuspend(selectedUser!!.userId); selectedUser = null },
            onReactivate = { onReactivate(selectedUser!!.userId); selectedUser = null },
            onDelete = { onDelete(selectedUser!!.userId); selectedUser = null },
            onDismiss = { selectedUser = null }
        )
    }

    Column(modifier = Modifier.fillMaxSize().background(DevLinkBackground)) {
        // Header toolbar
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(Color.White)
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text("All Users", fontWeight = FontWeight.Bold, fontSize = 16.sp, color = DevLinkTextPrimary)
            Button(
                onClick = { showCreateDialog = true },
                colors = ButtonDefaults.buttonColors(containerColor = DevLinkBlue),
                shape = RoundedCornerShape(8.dp),
                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp)
            ) {
                Icon(Icons.Default.PersonAdd, contentDescription = null, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Text("Create Account", fontSize = 12.sp)
            }
        }

        // Search bar
        OutlinedTextField(
            value = searchQuery,
            onValueChange = { searchQuery = it },
            modifier = Modifier.fillMaxWidth().padding(horizontal = 12.dp, vertical = 8.dp),
            placeholder = { Text("Search by name, username, or email...") },
            leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
            singleLine = true,
            shape = RoundedCornerShape(12.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = DevLinkBlue,
                unfocusedBorderColor = DevLinkBorder,
                focusedContainerColor = Color.White,
                unfocusedContainerColor = Color.White
            )
        )

        // Role filter chips
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp)
                .padding(bottom = 8.dp),
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            val labels = listOf("All", "Students", "Teachers", "Recruiters")
            roleOptions.forEachIndexed { i, role ->
                FilterChip(
                    selected = selectedRoleFilter == role,
                    onClick = {
                        selectedRoleFilter = role
                        onFilter(role, selectedEnabledFilter)
                    },
                    label = { Text(labels[i], fontSize = 11.sp) },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = DevLinkBlue,
                        selectedLabelColor = Color.White
                    )
                )
            }
            FilterChip(
                selected = selectedEnabledFilter == false,
                onClick = {
                    val next = if (selectedEnabledFilter == false) null else false
                    selectedEnabledFilter = next
                    onFilter(selectedRoleFilter, next)
                },
                label = { Text("Suspended", fontSize = 11.sp) },
                colors = FilterChipDefaults.filterChipColors(
                    selectedContainerColor = DevLinkError,
                    selectedLabelColor = Color.White
                )
            )
        }

        if (isLoading) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = DevLinkBlue)
            }
        } else {
            LazyColumn(
                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(filteredUsers) { user ->
                    AdminUserCard(user = user, onClick = { selectedUser = user })
                }
                if (filteredUsers.isEmpty()) {
                    item {
                        Box(modifier = Modifier.fillMaxWidth().padding(40.dp), contentAlignment = Alignment.Center) {
                            Text("No users found", color = DevLinkTextSecondary, fontSize = 14.sp)
                        }
                    }
                }
                item { Spacer(modifier = Modifier.height(80.dp)) }
            }
        }
    }
}

@Composable
fun AdminUserCard(user: AdminUserListItem, onClick: () -> Unit) {
    val roleColor = when (user.role) {
        "STUDENT" -> Color(0xFF1E40AF)
        "TEACHER" -> Color(0xFF065F46)
        "RECRUITER" -> Color(0xFF92400E)
        else -> DevLinkTextSecondary
    }
    val roleBg = when (user.role) {
        "STUDENT" -> Color(0xFFDBEAFE)
        "TEACHER" -> Color(0xFFD1FAE5)
        "RECRUITER" -> Color(0xFFFEF3C7)
        else -> DevLinkBorder
    }

    Card(
        modifier = Modifier.fillMaxWidth().clickable { onClick() },
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        border = BorderStroke(1.dp, if (!user.enabled) DevLinkError.copy(alpha = 0.3f) else DevLinkBorder),
        elevation = CardDefaults.cardElevation(0.dp)
    ) {
        Row(
            modifier = Modifier.padding(12.dp).fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Avatar
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(if (user.enabled) DevLinkBlueContainer else Color(0xFFFEE2E2)),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = user.username.take(2).uppercase(),
                    fontWeight = FontWeight.Bold,
                    color = if (user.enabled) DevLinkBlue else DevLinkError,
                    fontSize = 14.sp
                )
            }
            Spacer(modifier = Modifier.width(10.dp))
            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = user.displayName ?: user.username,
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 13.sp,
                        color = DevLinkTextPrimary,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.weight(1f)
                    )
                    Surface(color = roleBg, shape = RoundedCornerShape(6.dp)) {
                        Text(
                            text = user.role,
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Bold,
                            color = roleColor,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }
                }
                Text(text = "@${user.username}", fontSize = 11.sp, color = DevLinkTextSecondary)
                Text(text = user.email, fontSize = 11.sp, color = DevLinkTextSecondary, maxLines = 1, overflow = TextOverflow.Ellipsis)
            }
            Spacer(modifier = Modifier.width(8.dp))
            if (!user.enabled) {
                Surface(color = Color(0xFFFEE2E2), shape = CircleShape) {
                    Icon(
                        imageVector = Icons.Default.Block,
                        contentDescription = "Suspended",
                        tint = DevLinkError,
                        modifier = Modifier.size(18.dp).padding(2.dp)
                    )
                }
            } else {
                Icon(
                    imageVector = Icons.Default.ChevronRight,
                    contentDescription = null,
                    tint = DevLinkTextSecondary,
                    modifier = Modifier.size(18.dp)
                )
            }
        }
    }
}

@Composable
fun AdminUserDetailDialog(
    user: AdminUserListItem,
    isActionLoading: Boolean,
    onSuspend: () -> Unit,
    onReactivate: () -> Unit,
    onDelete: () -> Unit,
    onDismiss: () -> Unit
) {
    var showDeleteConfirm by remember { mutableStateOf(false) }

    Dialog(onDismissRequest = onDismiss) {
        Card(
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White)
        ) {
            Column(modifier = Modifier.padding(20.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier.size(48.dp).clip(CircleShape).background(DevLinkBlueContainer),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(user.username.take(2).uppercase(), fontWeight = FontWeight.Bold, color = DevLinkBlue, fontSize = 16.sp)
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text(user.displayName ?: user.username, fontWeight = FontWeight.Bold, fontSize = 15.sp, color = DevLinkTextPrimary)
                        Text("@${user.username}", fontSize = 12.sp, color = DevLinkTextSecondary)
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))
                HorizontalDivider(color = DevLinkBorder)
                Spacer(modifier = Modifier.height(12.dp))

                AdminDetailRow("Email", user.email)
                AdminDetailRow("Role", user.role)
                AdminDetailRow("Status", if (user.enabled) "Active" else "Suspended")
                user.department?.let { AdminDetailRow("Department", it) }
                user.companyName?.let { AdminDetailRow("Company", it) }

                Spacer(modifier = Modifier.height(16.dp))

                if (isActionLoading) {
                    Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator(color = DevLinkBlue)
                    }
                } else if (showDeleteConfirm) {
                    Text("Are you sure? This cannot be undone.", color = DevLinkError, fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedButton(onClick = { showDeleteConfirm = false }, modifier = Modifier.weight(1f)) { Text("Cancel") }
                        Button(
                            onClick = onDelete,
                            modifier = Modifier.weight(1f),
                            colors = ButtonDefaults.buttonColors(containerColor = DevLinkError)
                        ) { Text("Delete") }
                    }
                } else {
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        if (user.enabled) {
                            OutlinedButton(
                                onClick = onSuspend,
                                modifier = Modifier.weight(1f),
                                border = BorderStroke(1.dp, DevLinkError),
                                colors = ButtonDefaults.outlinedButtonColors(contentColor = DevLinkError)
                            ) {
                                Icon(Icons.Default.Block, null, modifier = Modifier.size(14.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Suspend", fontSize = 12.sp)
                            }
                        } else {
                            OutlinedButton(
                                onClick = onReactivate,
                                modifier = Modifier.weight(1f),
                                border = BorderStroke(1.dp, DevLinkSuccess),
                                colors = ButtonDefaults.outlinedButtonColors(contentColor = DevLinkSuccess)
                            ) {
                                Icon(Icons.Default.CheckCircle, null, modifier = Modifier.size(14.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Reactivate", fontSize = 12.sp)
                            }
                        }
                        OutlinedButton(
                            onClick = { showDeleteConfirm = true },
                            modifier = Modifier.weight(1f),
                            border = BorderStroke(1.dp, DevLinkError),
                            colors = ButtonDefaults.outlinedButtonColors(contentColor = DevLinkError)
                        ) {
                            Icon(Icons.Default.Delete, null, modifier = Modifier.size(14.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Delete", fontSize = 12.sp)
                        }
                    }
                }
                Spacer(modifier = Modifier.height(8.dp))
                TextButton(onClick = onDismiss, modifier = Modifier.fillMaxWidth()) { Text("Close") }
            }
        }
    }
}

@Composable
fun AdminDetailRow(label: String, value: String) {
    Row(modifier = Modifier.fillMaxWidth().padding(vertical = 3.dp)) {
        Text(label, fontSize = 12.sp, color = DevLinkTextSecondary, modifier = Modifier.width(80.dp))
        Text(value, fontSize = 12.sp, color = DevLinkTextPrimary, fontWeight = FontWeight.Medium, modifier = Modifier.weight(1f))
    }
}

// ─────────────────────────────────────────────────────────────
// CREATE USER DIALOG
// ─────────────────────────────────────────────────────────────
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreateUserDialog(
    onDismiss: () -> Unit,
    onCreate: (CreateUserAdminRequest) -> Unit
) {
    var role by remember { mutableStateOf("STUDENT") }
    var username by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    // Student fields
    var name by remember { mutableStateOf("") }
    var course by remember { mutableStateOf("") }
    var branch by remember { mutableStateOf("") }
    var admissionYear by remember { mutableStateOf("") }
    var semester by remember { mutableStateOf("") }
    // Teacher fields
    var employeeId by remember { mutableStateOf("") }
    var department by remember { mutableStateOf("") }
    var designation by remember { mutableStateOf("") }
    // Recruiter fields
    var companyName by remember { mutableStateOf("") }

    var roleDropdownExpanded by remember { mutableStateOf(false) }
    val roles = listOf("STUDENT", "TEACHER", "RECRUITER")

    var localError by remember { mutableStateOf<String?>(null) }

    Dialog(onDismissRequest = onDismiss) {
        Card(
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier
                    .verticalScroll(rememberScrollState())
                    .padding(20.dp)
            ) {
                Text("Create Account", fontWeight = FontWeight.Bold, fontSize = 18.sp, color = DevLinkTextPrimary)
                Text(
                    "Credentials will be emailed to the user automatically.",
                    fontSize = 12.sp,
                    color = DevLinkTextSecondary,
                    modifier = Modifier.padding(top = 4.dp, bottom = 12.dp)
                )

                if (localError != null) {
                    Surface(
                        color = Color(0xFFFEE2E2),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp)
                    ) {
                        Row(
                            modifier = Modifier.padding(10.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(Icons.Default.Error, contentDescription = null, tint = DevLinkError, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(localError!!, fontSize = 12.sp, color = DevLinkError, fontWeight = FontWeight.Medium)
                        }
                    }
                }

                // Role picker
                ExposedDropdownMenuBox(
                    expanded = roleDropdownExpanded,
                    onExpandedChange = { roleDropdownExpanded = it }
                ) {
                    OutlinedTextField(
                        value = role,
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Role") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(roleDropdownExpanded) },
                        modifier = Modifier.menuAnchor().fillMaxWidth(),
                        shape = RoundedCornerShape(10.dp),
                        colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = DevLinkBlue)
                    )
                    ExposedDropdownMenu(expanded = roleDropdownExpanded, onDismissRequest = { roleDropdownExpanded = false }) {
                        roles.forEach { r ->
                            DropdownMenuItem(
                                text = { Text(r) },
                                onClick = { role = r; localError = null; roleDropdownExpanded = false }
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                AdminTextField(value = username, onValueChange = { username = it; localError = null }, label = "Username")
                Spacer(modifier = Modifier.height(8.dp))
                AdminTextField(value = email, onValueChange = { email = it; localError = null }, label = "Email")
                Spacer(modifier = Modifier.height(8.dp))
                AdminTextField(value = password, onValueChange = { password = it; localError = null }, label = "Password (min 8 chars)")

                Spacer(modifier = Modifier.height(12.dp))
                HorizontalDivider(color = DevLinkBorder)
                Spacer(modifier = Modifier.height(12.dp))

                when (role) {
                    "STUDENT" -> {
                        Text("Student Details", fontWeight = FontWeight.SemiBold, fontSize = 13.sp, color = DevLinkTextSecondary)
                        Spacer(modifier = Modifier.height(8.dp))
                        AdminTextField(value = name, onValueChange = { name = it; localError = null }, label = "Full Name")
                        Spacer(modifier = Modifier.height(8.dp))
                        
                        // Course Dropdown
                        var courseExpanded by remember { mutableStateOf(false) }
                        val courses = listOf("BTECH", "MTECH", "BCA", "MCA", "BSC", "BSC_IT", "MSC", "OTHER")
                        ExposedDropdownMenuBox(expanded = courseExpanded, onExpandedChange = { courseExpanded = it }) {
                            OutlinedTextField(
                                value = if (course.isBlank()) "BTECH" else course,
                                onValueChange = {},
                                readOnly = true,
                                label = { Text("Course", fontSize = 12.sp) },
                                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(courseExpanded) },
                                modifier = Modifier.menuAnchor().fillMaxWidth(),
                                shape = RoundedCornerShape(10.dp),
                                textStyle = LocalTextStyle.current.copy(fontSize = 13.sp),
                                colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = DevLinkBlue)
                            )
                            ExposedDropdownMenu(expanded = courseExpanded, onDismissRequest = { courseExpanded = false }) {
                                courses.forEach { c ->
                                    DropdownMenuItem(text = { Text(c) }, onClick = { course = c; courseExpanded = false })
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        // Branch Dropdown
                        var branchExpanded by remember { mutableStateOf(false) }
                        val branches = listOf("CS", "IT", "ECE", "EE", "ME", "CE", "CYBER_SECURITY", "AI", "DATA_SCIENCE", "OTHER")
                        ExposedDropdownMenuBox(expanded = branchExpanded, onExpandedChange = { branchExpanded = it }) {
                            OutlinedTextField(
                                value = if (branch.isBlank()) "CS" else branch,
                                onValueChange = {},
                                readOnly = true,
                                label = { Text("Branch", fontSize = 12.sp) },
                                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(branchExpanded) },
                                modifier = Modifier.menuAnchor().fillMaxWidth(),
                                shape = RoundedCornerShape(10.dp),
                                textStyle = LocalTextStyle.current.copy(fontSize = 13.sp),
                                colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = DevLinkBlue)
                            )
                            ExposedDropdownMenu(expanded = branchExpanded, onDismissRequest = { branchExpanded = false }) {
                                branches.forEach { b ->
                                    DropdownMenuItem(text = { Text(b) }, onClick = { branch = b; branchExpanded = false })
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(8.dp))
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            AdminTextField(value = admissionYear, onValueChange = { admissionYear = it }, label = "Admission Year (e.g. 2024)", modifier = Modifier.weight(1f))
                            AdminTextField(value = semester, onValueChange = { semester = it }, label = "Semester (e.g. 1)", modifier = Modifier.weight(1f))
                        }
                    }
                    "TEACHER" -> {
                        Text("Teacher Details", fontWeight = FontWeight.SemiBold, fontSize = 13.sp, color = DevLinkTextSecondary)
                        Spacer(modifier = Modifier.height(8.dp))
                        AdminTextField(value = employeeId, onValueChange = { employeeId = it; localError = null }, label = "Employee ID")
                        Spacer(modifier = Modifier.height(8.dp))
                        AdminTextField(value = department, onValueChange = { department = it; localError = null }, label = "Department")
                        Spacer(modifier = Modifier.height(8.dp))
                        AdminTextField(value = designation, onValueChange = { designation = it }, label = "Designation")
                    }
                    "RECRUITER" -> {
                        Text("Recruiter Details", fontWeight = FontWeight.SemiBold, fontSize = 13.sp, color = DevLinkTextSecondary)
                        Spacer(modifier = Modifier.height(8.dp))
                        AdminTextField(value = companyName, onValueChange = { companyName = it; localError = null }, label = "Company Name")
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))

                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    OutlinedButton(onClick = onDismiss, modifier = Modifier.weight(1f)) { Text("Cancel") }
                    Button(
                        onClick = {
                            val cleanUsername = username.trim()
                            val cleanEmail = email.trim()
                            val cleanPass = password.trim()

                            when {
                                cleanUsername.isBlank() -> localError = "Username is required"
                                cleanUsername.length < 3 -> localError = "Username must be at least 3 characters"
                                cleanEmail.isBlank() || !cleanEmail.contains("@") -> localError = "Valid email is required"
                                cleanPass.length < 8 -> localError = "Password must be at least 8 characters"
                                role == "STUDENT" && name.isBlank() -> localError = "Full Name is required for Student"
                                role == "TEACHER" && (employeeId.isBlank() || department.isBlank()) -> localError = "Employee ID & Department are required for Teacher"
                                role == "RECRUITER" && companyName.isBlank() -> localError = "Company Name is required for Recruiter"
                                else -> {
                                    localError = null
                                    val request = CreateUserAdminRequest(
                                        username = cleanUsername,
                                        email = cleanEmail,
                                        password = cleanPass,
                                        role = role,
                                        name = name.takeIf { it.isNotBlank() },
                                        course = if (role == "STUDENT") (course.ifBlank { "BTECH" }) else null,
                                        branch = if (role == "STUDENT") (branch.ifBlank { "CS" }) else null,
                                        admissionYear = if (role == "STUDENT") (admissionYear.toIntOrNull() ?: 2024) else null,
                                        semester = if (role == "STUDENT") (semester.toIntOrNull() ?: 1) else null,
                                        employeeId = employeeId.toLongOrNull(),
                                        department = department.takeIf { it.isNotBlank() },
                                        designation = designation.takeIf { it.isNotBlank() },
                                        companyName = companyName.takeIf { it.isNotBlank() }
                                    )
                                    onCreate(request)
                                }
                            }
                        },
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.buttonColors(containerColor = DevLinkBlue),
                        shape = RoundedCornerShape(10.dp)
                    ) { Text("Create & Send Email") }
                }
            }
        }
    }
}

@Composable
fun AdminTextField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    modifier: Modifier = Modifier
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        label = { Text(label, fontSize = 12.sp) },
        modifier = modifier.fillMaxWidth(),
        singleLine = true,
        shape = RoundedCornerShape(10.dp),
        textStyle = LocalTextStyle.current.copy(fontSize = 13.sp),
        colors = OutlinedTextFieldDefaults.colors(
            focusedBorderColor = DevLinkBlue,
            unfocusedBorderColor = DevLinkBorder,
            focusedContainerColor = Color.White,
            unfocusedContainerColor = Color.White
        )
    )
}

// ─────────────────────────────────────────────────────────────
// TAB 2: AUDIT & CONTENT LOGS
// ─────────────────────────────────────────────────────────────
@Composable
fun AdminAuditLogsTab(
    isLoading: Boolean
) {
    var selectedFilter by remember { mutableStateOf("ALL") }
    val filterOptions = listOf("ALL", "CERTIFICATES", "PROJECTS", "ACCOUNTS")

    Column(modifier = Modifier.fillMaxSize().background(DevLinkBackground)) {
        // Section header
        Column(
            modifier = Modifier.fillMaxWidth().background(Color.White).padding(16.dp)
        ) {
            Text("Audit & Content Logs", fontWeight = FontWeight.Bold, fontSize = 16.sp, color = DevLinkTextPrimary)
            Text("Platform verification history, project showcases & account activity", fontSize = 12.sp, color = DevLinkTextSecondary)
            
            Spacer(modifier = Modifier.height(12.dp))

            // Filter chips
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                filterOptions.forEach { filter ->
                    val isSelected = selectedFilter == filter
                    FilterChip(
                        selected = isSelected,
                        onClick = { selectedFilter = filter },
                        label = { Text(filter, fontSize = 11.sp, fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = DevLinkBlue,
                            selectedLabelColor = Color.White
                        )
                    )
                }
            }
        }

        if (isLoading) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = DevLinkBlue)
            }
        } else {
            // Audit log list
            LazyColumn(
                contentPadding = PaddingValues(12.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                if (selectedFilter == "ALL" || selectedFilter == "ACCOUNTS") {
                    item {
                        AuditLogCard(
                            title = "Account Created & Credentials Emailed",
                            subtitle = "Admin created user account @shubham_student (Student)",
                            timestamp = "Just now",
                            tag = "ACCOUNT",
                            tagColor = Color(0xFF1E40AF),
                            tagBg = Color(0xFFDBEAFE),
                            icon = Icons.Default.PersonAdd
                        )
                    }
                }
                if (selectedFilter == "ALL" || selectedFilter == "CERTIFICATES") {
                    item {
                        AuditLogCard(
                            title = "Certificate Verified: AWS Certified Developer",
                            subtitle = "Requested by @rahul_s | Reviewed by Prof. Sharma (CS Dept)",
                            timestamp = "2 hours ago",
                            tag = "APPROVED",
                            tagColor = Color(0xFF065F46),
                            tagBg = Color(0xFFD1FAE5),
                            icon = Icons.Default.VerifiedUser
                        )
                    }
                    item {
                        AuditLogCard(
                            title = "Certificate Verification Pending: React Native Certification",
                            subtitle = "Requested by @anita_m | Assigned to Prof. Verma",
                            timestamp = "5 hours ago",
                            tag = "PENDING",
                            tagColor = Color(0xFF92400E),
                            tagBg = Color(0xFFFEF3C7),
                            icon = Icons.Default.HourglassTop
                        )
                    }
                }
                if (selectedFilter == "ALL" || selectedFilter == "PROJECTS") {
                    item {
                        AuditLogCard(
                            title = "Project Showcase Published: DevLink Platform",
                            subtitle = "Submitted by @team_devlink | Kotlin, Spring Boot, PostgreSQL",
                            timestamp = "1 day ago",
                            tag = "PROJECT",
                            tagColor = Color(0xFF5B21B6),
                            tagBg = Color(0xFFEDE9FE),
                            icon = Icons.Default.Folder
                        )
                    }
                }
                item { Spacer(modifier = Modifier.height(80.dp)) }
            }
        }
    }
}

@Composable
fun AuditLogCard(
    title: String,
    subtitle: String,
    timestamp: String,
    tag: String,
    tagColor: Color,
    tagBg: Color,
    icon: ImageVector
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        border = BorderStroke(1.dp, DevLinkBorder),
        elevation = CardDefaults.cardElevation(0.dp)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier.size(42.dp).clip(RoundedCornerShape(10.dp)).background(tagBg),
                contentAlignment = Alignment.Center
            ) {
                Icon(imageVector = icon, contentDescription = null, tint = tagColor, modifier = Modifier.size(20.dp))
            }
            Spacer(modifier = Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(title, fontWeight = FontWeight.Bold, fontSize = 13.sp, color = DevLinkTextPrimary, modifier = Modifier.weight(1f))
                    Spacer(modifier = Modifier.width(6.dp))
                    Surface(color = tagBg, shape = RoundedCornerShape(6.dp)) {
                        Text(tag, fontSize = 9.sp, fontWeight = FontWeight.Bold, color = tagColor,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp))
                    }
                }
                Spacer(modifier = Modifier.height(4.dp))
                Text(subtitle, fontSize = 11.sp, color = DevLinkTextSecondary)
                Spacer(modifier = Modifier.height(2.dp))
                Text(timestamp, fontSize = 10.sp, color = Color.Gray)
            }
        }
    }
}

// ─────────────────────────────────────────────────────────────
// TAB 3: ANALYTICS
// ─────────────────────────────────────────────────────────────
@Composable
fun AdminAnalyticsTab(stats: AdminDashboardStats?, isLoading: Boolean) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(DevLinkBackground)
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text("Platform Analytics", fontWeight = FontWeight.Bold, fontSize = 18.sp, color = DevLinkTextPrimary)

        if (isLoading) {
            Box(modifier = Modifier.fillMaxWidth().height(200.dp), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = DevLinkBlue)
            }
        } else if (stats == null) {
            Box(modifier = Modifier.fillMaxWidth().height(100.dp), contentAlignment = Alignment.Center) {
                Text("Analytics data unavailable.", fontSize = 13.sp, color = DevLinkTextSecondary)
            }
        } else {
            // User breakdown
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                border = BorderStroke(1.dp, DevLinkBorder)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("User Breakdown", fontWeight = FontWeight.Bold, fontSize = 14.sp, color = DevLinkTextPrimary)
                    Spacer(modifier = Modifier.height(12.dp))
                    val totalUsers = stats.totalStudents + stats.totalTeachers + stats.totalRecruiters
                    AnalyticsBar("Students", stats.totalStudents, totalUsers, Color(0xFF1E40AF))
                    Spacer(modifier = Modifier.height(8.dp))
                    AnalyticsBar("Teachers", stats.totalTeachers, totalUsers, Color(0xFF065F46))
                    Spacer(modifier = Modifier.height(8.dp))
                    AnalyticsBar("Recruiters", stats.totalRecruiters, totalUsers, Color(0xFF92400E))
                }
            }

            // Content stats
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                border = BorderStroke(1.dp, DevLinkBorder)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("Content Stats", fontWeight = FontWeight.Bold, fontSize = 14.sp, color = DevLinkTextPrimary)
                    Spacer(modifier = Modifier.height(12.dp))
                    AnalyticsStatRow("Total Projects", stats.totalProjects.toString(), Icons.Default.Folder, Color(0xFF5B21B6))
                    Spacer(modifier = Modifier.height(8.dp))
                    AnalyticsStatRow("Verified Certificates", stats.totalVerifiedCertificates.toString(), Icons.Default.CardMembership, Color(0xFF0E7490))
                    Spacer(modifier = Modifier.height(8.dp))
                    AnalyticsStatRow("Pending Recruiter Approvals", stats.pendingRecruiterApprovals.toString(), Icons.Default.HourglassTop, DevLinkError)
                }
            }
        }
        Spacer(modifier = Modifier.height(80.dp))
    }
}

@Composable
fun AnalyticsBar(label: String, count: Long, total: Long, color: Color) {
    val fraction = if (total > 0) (count.toFloat() / total.toFloat()) else 0f
    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
        Text(label, fontSize = 12.sp, color = DevLinkTextSecondary, modifier = Modifier.width(72.dp))
        Box(
            modifier = Modifier
                .weight(1f)
                .height(12.dp)
                .clip(RoundedCornerShape(6.dp))
                .background(DevLinkBorder)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxHeight()
                    .fillMaxWidth(fraction)
                    .background(color)
                    .clip(RoundedCornerShape(6.dp))
            )
        }
        Spacer(modifier = Modifier.width(8.dp))
        Text("$count", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = color)
    }
}

@Composable
fun AnalyticsStatRow(label: String, value: String, icon: ImageVector, color: Color) {
    Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
        Icon(imageVector = icon, contentDescription = null, tint = color, modifier = Modifier.size(18.dp))
        Spacer(modifier = Modifier.width(10.dp))
        Text(label, fontSize = 13.sp, color = DevLinkTextPrimary, modifier = Modifier.weight(1f))
        Text(value, fontSize = 14.sp, fontWeight = FontWeight.Bold, color = color)
    }
}

// ─────────────────────────────────────────────────────────────
// TAB 4: SETTINGS
// ─────────────────────────────────────────────────────────────
@Composable
fun AdminSettingsTab(onLogoutClick: () -> Unit = {}) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(DevLinkBackground)
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text("Platform Settings", fontWeight = FontWeight.Bold, fontSize = 18.sp, color = DevLinkTextPrimary)

        AdminSettingsSection(
            title = "Account Management",
            items = listOf(
                Triple(Icons.Default.PersonAdd, "Create User Account", "Add student, teacher, or recruiter"),
                Triple(Icons.Default.ManageAccounts, "Manage All Users", "View, suspend, or delete accounts"),
                Triple(Icons.Default.Approval, "Review Approvals", "Approve pending recruiter accounts")
            )
        )

        AdminSettingsSection(
            title = "Platform Configuration",
            items = listOf(
                Triple(Icons.Default.School, "Skill Taxonomy", "Manage the skills catalog"),
                Triple(Icons.Default.EmojiEvents, "Badges & Points", "Configure reward rules"),
                Triple(Icons.Default.Campaign, "Announcements", "Broadcast platform-wide messages")
            )
        )

        AdminSettingsSection(
            title = "System",
            items = listOf(
                Triple(Icons.Default.Security, "Audit Log", "View admin action history"),
                Triple(Icons.Default.ToggleOn, "Feature Flags", "Toggle platform features on/off"),
                Triple(Icons.Default.Info, "About DevLink", "Version and build information")
            )
        )

        Spacer(modifier = Modifier.height(8.dp))

        Card(
            modifier = Modifier
                .fillMaxWidth()
                .clickable { onLogoutClick() },
            shape = RoundedCornerShape(14.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            border = BorderStroke(1.dp, Color(0xFFFCA5A5))
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .clip(RoundedCornerShape(10.dp))
                        .background(Color(0xFFFEE2E2)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.ExitToApp,
                        contentDescription = "Logout",
                        tint = Color(0xFFEF4444),
                        modifier = Modifier.size(18.dp)
                    )
                }
                Spacer(modifier = Modifier.width(12.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Sign Out",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFFEF4444)
                    )
                    Text(
                        text = "Log out of the administrator account",
                        fontSize = 11.sp,
                        color = DevLinkTextSecondary
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(80.dp))
    }
}

@Composable
fun AdminSettingsSection(title: String, items: List<Triple<ImageVector, String, String>>) {
    Column {
        Text(title, fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = DevLinkTextSecondary,
            modifier = Modifier.padding(bottom = 8.dp))
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(14.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            border = BorderStroke(1.dp, DevLinkBorder)
        ) {
            items.forEachIndexed { index, (icon, label, subtitle) ->
                Row(
                    modifier = Modifier.fillMaxWidth().clickable { }.padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier.size(36.dp).clip(RoundedCornerShape(10.dp)).background(DevLinkBlueContainer),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(imageVector = icon, contentDescription = null, tint = DevLinkBlue, modifier = Modifier.size(18.dp))
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(label, fontSize = 13.sp, fontWeight = FontWeight.Medium, color = DevLinkTextPrimary)
                        Text(subtitle, fontSize = 11.sp, color = DevLinkTextSecondary)
                    }
                    Icon(Icons.Default.ChevronRight, null, tint = DevLinkTextSecondary, modifier = Modifier.size(18.dp))
                }
                if (index < items.size - 1) HorizontalDivider(color = DevLinkBorder, modifier = Modifier.padding(horizontal = 16.dp))
            }
        }
    }
}
