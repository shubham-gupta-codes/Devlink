package com.zyproo.devlink.ui.chat

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
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
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil3.compose.AsyncImage
import com.zyproo.devlink.common.toFullMediaUrl
import com.zyproo.devlink.data.remote.dto.Branch
import com.zyproo.devlink.data.remote.dto.Course
import com.zyproo.devlink.data.remote.dto.ProfileResponse
import com.zyproo.devlink.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TeamInviteScreen(
    searchResults: List<ProfileResponse>,
    isSearching: Boolean,
    onBackClick: () -> Unit,
    onSearch: (query: String?, course: Course?, branch: Branch?, year: Int?) -> Unit,
    onInviteClick: (String) -> Unit
) {
    var query by remember { mutableStateOf("") }
    var selectedCourse by remember { mutableStateOf<Course?>(null) }
    var selectedBranch by remember { mutableStateOf<Branch?>(null) }
    var selectedYear by remember { mutableStateOf<Int?>(null) }
    var activeFilterDialog by remember { mutableStateOf<String?>(null) }
    val invitedUsernames = remember { mutableStateListOf<String>() }

    // Trigger search on query or filter changes
    LaunchedEffect(query, selectedCourse, selectedBranch, selectedYear) {
        onSearch(
            query.ifBlank { null },
            selectedCourse,
            selectedBranch,
            selectedYear
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Invite Members", fontWeight = FontWeight.Bold, fontSize = 18.sp) },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.White)
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .background(Color(0xFFF6F7FB))
                .padding(16.dp)
        ) {
            // Search Input
            OutlinedTextField(
                value = query,
                onValueChange = { query = it },
                placeholder = { Text("Search by name or username...") },
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                trailingIcon = {
                    if (query.isNotEmpty()) {
                        IconButton(onClick = { query = "" }) {
                            Icon(Icons.Default.Close, contentDescription = "Clear")
                        }
                    }
                },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(24.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = DevLinkBlue,
                    unfocusedBorderColor = Color.LightGray
                )
            )

            Spacer(modifier = Modifier.height(10.dp))

            // Search Filter Chips
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                item {
                    Icon(
                        imageVector = Icons.Default.FilterList,
                        contentDescription = "Filters",
                        tint = DevLinkBlue,
                        modifier = Modifier.size(18.dp)
                    )
                }

                item {
                    FilterChip(
                        selected = selectedCourse != null,
                        onClick = { activeFilterDialog = "course" },
                        label = { Text(selectedCourse?.name ?: "Course", fontSize = 12.sp) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = DevLinkBlue,
                            selectedLabelColor = Color.White
                        ),
                        trailingIcon = {
                            if (selectedCourse != null) {
                                Icon(
                                    imageVector = Icons.Default.Close,
                                    contentDescription = "Clear Course",
                                    modifier = Modifier
                                        .size(14.dp)
                                        .clickable { selectedCourse = null }
                                )
                            }
                        }
                    )
                }

                item {
                    FilterChip(
                        selected = selectedBranch != null,
                        onClick = { activeFilterDialog = "branch" },
                        label = { Text(selectedBranch?.name ?: "Branch", fontSize = 12.sp) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = DevLinkBlue,
                            selectedLabelColor = Color.White
                        ),
                        trailingIcon = {
                            if (selectedBranch != null) {
                                Icon(
                                    imageVector = Icons.Default.Close,
                                    contentDescription = "Clear Branch",
                                    modifier = Modifier
                                        .size(14.dp)
                                        .clickable { selectedBranch = null }
                                )
                            }
                        }
                    )
                }

                item {
                    FilterChip(
                        selected = selectedYear != null,
                        onClick = { activeFilterDialog = "year" },
                        label = { Text(selectedYear?.let { "Year $it" } ?: "Year", fontSize = 12.sp) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = DevLinkBlue,
                            selectedLabelColor = Color.White
                        ),
                        trailingIcon = {
                            if (selectedYear != null) {
                                Icon(
                                    imageVector = Icons.Default.Close,
                                    contentDescription = "Clear Year",
                                    modifier = Modifier
                                        .size(14.dp)
                                        .clickable { selectedYear = null }
                                )
                            }
                        }
                    )
                }

                if (selectedCourse != null || selectedBranch != null || selectedYear != null) {
                    item {
                        TextButton(
                            onClick = {
                                selectedCourse = null
                                selectedBranch = null
                                selectedYear = null
                            },
                            contentPadding = PaddingValues(horizontal = 6.dp)
                        ) {
                            Text("Clear", color = Color(0xFFEF4444), fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            Text(
                text = "Search Results",
                fontWeight = FontWeight.Bold,
                fontSize = 14.sp,
                color = Color(0xFF64748B)
            )

            Spacer(modifier = Modifier.height(10.dp))

            if (isSearching) {
                Box(modifier = Modifier.fillMaxWidth().weight(1f), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = DevLinkBlue)
                }
            } else if (searchResults.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                        .padding(32.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "No students found matching your filters.",
                        color = Color.Gray,
                        fontSize = 14.sp
                    )
                }
            } else {
                LazyColumn(
                    modifier = Modifier.weight(1f).fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(searchResults) { student ->
                        val isInvited = invitedUsernames.contains(student.username)
                        InviteUserRow(
                            student = student,
                            isInvited = isInvited,
                            onInviteClick = {
                                onInviteClick(student.username)
                                invitedUsernames.add(student.username)
                            }
                        )
                    }
                }
            }
        }
    }

    // Filter Dialog Overlays
    activeFilterDialog?.let { filterType ->
        AlertDialog(
            onDismissRequest = { activeFilterDialog = null },
            containerColor = Color.White,
            shape = RoundedCornerShape(20.dp),
            confirmButton = {
                TextButton(onClick = { activeFilterDialog = null }) {
                    Text("Close", color = DevLinkBlue, fontWeight = FontWeight.Bold)
                }
            },
            title = {
                Text(
                    text = "Filter by ${filterType.replaceFirstChar { it.uppercase() }}",
                    fontSize = 17.sp,
                    fontWeight = FontWeight.Bold,
                    color = DevLinkTextPrimary
                )
            },
            text = {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    when (filterType) {
                        "course" -> {
                            Course.values().forEach { courseVal ->
                                val isSelected = selectedCourse == courseVal
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clip(RoundedCornerShape(10.dp))
                                        .background(if (isSelected) Color(0xFFEFF6FF) else Color.Transparent)
                                        .clickable {
                                            selectedCourse = courseVal
                                            activeFilterDialog = null
                                        }
                                        .padding(horizontal = 8.dp, vertical = 8.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    RadioButton(
                                        selected = isSelected,
                                        colors = RadioButtonDefaults.colors(selectedColor = DevLinkBlue),
                                        onClick = {
                                            selectedCourse = courseVal
                                            activeFilterDialog = null
                                        }
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = courseVal.name,
                                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                        color = if (isSelected) DevLinkBlue else DevLinkTextPrimary
                                    )
                                }
                            }
                        }
                        "branch" -> {
                            Branch.values().forEach { branchVal ->
                                val isSelected = selectedBranch == branchVal
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clip(RoundedCornerShape(10.dp))
                                        .background(if (isSelected) Color(0xFFEFF6FF) else Color.Transparent)
                                        .clickable {
                                            selectedBranch = branchVal
                                            activeFilterDialog = null
                                        }
                                        .padding(horizontal = 8.dp, vertical = 8.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    RadioButton(
                                        selected = isSelected,
                                        colors = RadioButtonDefaults.colors(selectedColor = DevLinkBlue),
                                        onClick = {
                                            selectedBranch = branchVal
                                            activeFilterDialog = null
                                        }
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = branchVal.name,
                                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                        color = if (isSelected) DevLinkBlue else DevLinkTextPrimary
                                    )
                                }
                            }
                        }
                        "year" -> {
                            listOf(1, 2, 3, 4).forEach { yearVal ->
                                val isSelected = selectedYear == yearVal
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clip(RoundedCornerShape(10.dp))
                                        .background(if (isSelected) Color(0xFFEFF6FF) else Color.Transparent)
                                        .clickable {
                                            selectedYear = yearVal
                                            activeFilterDialog = null
                                        }
                                        .padding(horizontal = 8.dp, vertical = 8.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    RadioButton(
                                        selected = isSelected,
                                        colors = RadioButtonDefaults.colors(selectedColor = DevLinkBlue),
                                        onClick = {
                                            selectedYear = yearVal
                                            activeFilterDialog = null
                                        }
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = "Year $yearVal",
                                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                        color = if (isSelected) DevLinkBlue else DevLinkTextPrimary
                                    )
                                }
                            }
                        }
                    }
                }
            }
        )
    }
}

@Composable
fun InviteUserRow(
    student: ProfileResponse,
    isInvited: Boolean,
    onInviteClick: () -> Unit
) {
    val photoUrl = student.photoUrl.toFullMediaUrl()
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(42.dp)
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
                        text = (student.name ?: student.username).take(2).uppercase(),
                        fontWeight = FontWeight.Bold,
                        color = DevLinkBlue,
                        fontSize = 14.sp
                    )
                }
            }

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = student.name ?: student.username,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 14.sp,
                    color = Color(0xFF1E293B)
                )
                Text(
                    text = "@${student.username}",
                    fontSize = 12.sp,
                    color = Color.Gray
                )
                if (student.branch != null || student.admissionYear != null) {
                    Text(
                        text = "${student.branch?.name ?: ""} ${student.admissionYear?.let { "• Yr $it" } ?: ""}".trim(),
                        fontSize = 11.sp,
                        color = DevLinkBlue
                    )
                }
            }

            Button(
                onClick = onInviteClick,
                enabled = !isInvited,
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (isInvited) Color.LightGray else DevLinkBlue,
                    contentColor = Color.White
                ),
                shape = RoundedCornerShape(20.dp),
                contentPadding = PaddingValues(horizontal = 14.dp, vertical = 6.dp)
            ) {
                Text(
                    text = if (isInvited) "Invited" else "Invite",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.SemiBold
                )
            }
        }
    }
}
