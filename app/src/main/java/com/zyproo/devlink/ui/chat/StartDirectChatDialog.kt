package com.zyproo.devlink.ui.chat

import androidx.compose.foundation.BorderStroke
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
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import coil3.compose.AsyncImage
import com.zyproo.devlink.common.toFullMediaUrl
import com.zyproo.devlink.data.remote.dto.Branch
import com.zyproo.devlink.data.remote.dto.Course
import com.zyproo.devlink.data.remote.dto.ProfileResponse
import com.zyproo.devlink.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StartDirectChatDialog(
    searchResults: List<ProfileResponse>,
    isSearching: Boolean,
    onDismissRequest: () -> Unit,
    onStartChat: (String) -> Unit,
    onSearch: (query: String?, course: Course?, branch: Branch?, year: Int?) -> Unit
) {
    var query by remember { mutableStateOf("") }
    var selectedCourse by remember { mutableStateOf<Course?>(null) }
    var selectedBranch by remember { mutableStateOf<Branch?>(null) }
    var selectedYear by remember { mutableStateOf<Int?>(null) }
    var activeFilterDialog by remember { mutableStateOf<String?>(null) }
    var manualUsername by remember { mutableStateOf("") }

    // Trigger search when query or filters change
    LaunchedEffect(query, selectedCourse, selectedBranch, selectedYear) {
        onSearch(
            query.ifBlank { null },
            selectedCourse,
            selectedBranch,
            selectedYear
        )
    }

    Dialog(onDismissRequest = onDismissRequest) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight(0.85f),
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            elevation = CardDefaults.cardElevation(defaultElevation = 6.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(18.dp)
            ) {
                // Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Find User to Message",
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp,
                        color = DevLinkTextPrimary
                    )
                    IconButton(onClick = onDismissRequest, modifier = Modifier.size(28.dp)) {
                        Icon(Icons.Default.Close, contentDescription = "Close", tint = DevLinkTextSecondary)
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Search Bar
                OutlinedTextField(
                    value = query,
                    onValueChange = { query = it },
                    placeholder = { Text("Search by name or username...", fontSize = 13.sp) },
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Default.Search,
                            contentDescription = "Search",
                            tint = DevLinkTextSecondary,
                            modifier = Modifier.size(20.dp)
                        )
                    },
                    trailingIcon = {
                        if (query.isNotEmpty()) {
                            IconButton(onClick = { query = "" }) {
                                Icon(
                                    imageVector = Icons.Default.Clear,
                                    contentDescription = "Clear",
                                    tint = DevLinkTextSecondary,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        }
                    },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(14.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = DevLinkBlue,
                        unfocusedBorderColor = DevLinkBorder
                    ),
                    singleLine = true
                )

                Spacer(modifier = Modifier.height(10.dp))

                // Filter Chips Row
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    item {
                        Icon(
                            imageVector = Icons.Default.FilterList,
                            contentDescription = "Filter",
                            tint = DevLinkBlue,
                            modifier = Modifier.size(18.dp)
                        )
                    }

                    // Course Filter Chip
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

                    // Branch Filter Chip
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

                    // Year Filter Chip
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

                    // Clear Filters
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

                Spacer(modifier = Modifier.height(10.dp))
                HorizontalDivider(color = DevLinkBorder)
                Spacer(modifier = Modifier.height(10.dp))

                // Search Results List
                Box(modifier = Modifier.weight(1f).fillMaxWidth()) {
                    if (isSearching) {
                        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                            CircularProgressIndicator(color = DevLinkBlue, modifier = Modifier.size(32.dp))
                        }
                    } else if (searchResults.isEmpty()) {
                        Column(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(16.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.SearchOff,
                                contentDescription = null,
                                tint = Color.Gray,
                                modifier = Modifier.size(40.dp)
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = "No users found matching your filters.",
                                color = DevLinkTextSecondary,
                                fontSize = 13.sp
                            )
                            if (query.isNotBlank()) {
                                Spacer(modifier = Modifier.height(12.dp))
                                Button(
                                    onClick = { onStartChat(query.trim()) },
                                    colors = ButtonDefaults.buttonColors(containerColor = DevLinkBlue),
                                    shape = RoundedCornerShape(10.dp)
                                ) {
                                    Text("Message \"${query.trim()}\" directly", fontSize = 12.sp)
                                }
                            }
                        }
                    } else {
                        LazyColumn(
                            modifier = Modifier.fillMaxSize(),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            items(searchResults) { user ->
                                UserSearchRow(
                                    user = user,
                                    onChatClick = { onStartChat(user.username) }
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))
                HorizontalDivider(color = DevLinkBorder)
                Spacer(modifier = Modifier.height(8.dp))

                // Manual username input fallback
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    OutlinedTextField(
                        value = manualUsername,
                        onValueChange = { manualUsername = it },
                        placeholder = { Text("Exact username...", fontSize = 12.sp) },
                        modifier = Modifier.weight(1f),
                        singleLine = true,
                        shape = RoundedCornerShape(10.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = DevLinkBlue,
                            unfocusedBorderColor = DevLinkBorder
                        )
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Button(
                        onClick = {
                            if (manualUsername.isNotBlank()) {
                                onStartChat(manualUsername.trim())
                            }
                        },
                        enabled = manualUsername.isNotBlank(),
                        shape = RoundedCornerShape(10.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = DevLinkBlue)
                    ) {
                        Text("Chat", fontSize = 13.sp, fontWeight = FontWeight.Bold)
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
fun UserSearchRow(
    user: ProfileResponse,
    onChatClick: () -> Unit
) {
    val photoUrl = user.photoUrl.toFullMediaUrl()
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onChatClick() },
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFFF8FAFC)),
        border = BorderStroke(1.dp, DevLinkBorder)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            com.zyproo.devlink.ui.common.UserAvatar(
                model = user.photoUrl,
                name = user.name ?: user.username,
                modifier = Modifier.size(42.dp),
                backgroundColor = DevLinkBlueContainer,
                textColor = DevLinkBlue,
                fontSize = 14.sp
            )

            Spacer(modifier = Modifier.width(10.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = user.name ?: user.username,
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp,
                    color = DevLinkTextPrimary,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = "@${user.username}",
                    fontSize = 12.sp,
                    color = DevLinkTextSecondary
                )
                if (user.branch != null || user.admissionYear != null) {
                    Text(
                        text = "${user.branch?.name ?: ""} ${user.admissionYear?.let { "• Yr $it" } ?: ""}".trim(),
                        fontSize = 11.sp,
                        color = DevLinkBlue,
                        fontWeight = FontWeight.Medium
                    )
                }
            }

            Button(
                onClick = onChatClick,
                shape = RoundedCornerShape(20.dp),
                colors = ButtonDefaults.buttonColors(containerColor = DevLinkBlue),
                contentPadding = PaddingValues(horizontal = 14.dp, vertical = 6.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Chat,
                        contentDescription = null,
                        modifier = Modifier.size(14.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Chat", fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                }
            }
        }
    }
}
