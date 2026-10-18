package com.zyproo.devlink.ui.home

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
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.BookmarkBorder
import androidx.compose.material.icons.filled.Chat
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
import com.zyproo.devlink.common.toFullMediaUrl
import com.zyproo.devlink.data.remote.dto.Branch
import com.zyproo.devlink.data.remote.dto.Course
import com.zyproo.devlink.data.remote.dto.ProfileResponse
import com.zyproo.devlink.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StudentSearchScreen(
    searchResults: List<ProfileResponse>,
    isSearching: Boolean,
    searchError: String?,
    onSearch: (String?, Course?, Branch?, Int?, Int?, String?, String?) -> Unit,
    onUserClick: (ProfileResponse) -> Unit,
    recruiterMode: Boolean = false,
    savedStudentIds: Set<Long> = emptySet(),
    onShortlistClick: ((ProfileResponse) -> Unit)? = null,
    onMessageClick: ((ProfileResponse) -> Unit)? = null
) {
    var query by remember { mutableStateOf("") }
    
    // Filters State
    var selectedCourse by remember { mutableStateOf<Course?>(null) }
    var selectedBranch by remember { mutableStateOf<Branch?>(null) }
    var selectedYear by remember { mutableStateOf<Int?>(null) }
    var selectedSemester by remember { mutableStateOf<Int?>(null) }
    var selectedSkill by remember { mutableStateOf("") }
    var selectedBadge by remember { mutableStateOf("") }
    var sortBy by remember { mutableStateOf("Top Rated") }

    // Dialog trigger states
    var activeFilterDialog by remember { mutableStateOf<String?>(null) }

    // Trigger search on filter changes or query edits
    LaunchedEffect(query, selectedCourse, selectedBranch, selectedYear, selectedSemester, selectedSkill, selectedBadge) {
        onSearch(
            query.ifBlank { null },
            selectedCourse,
            selectedBranch,
            selectedYear,
            selectedSemester,
            selectedSkill.ifBlank { null },
            selectedBadge.ifBlank { null }
        )
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(DevLinkBackground)
    ) {
        // Search Header Card
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(bottomStart = 20.dp, bottomEnd = 20.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            border = BorderStroke(1.dp, DevLinkBorder),
            elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                if (recruiterMode) {
                    Text(
                        text = "Find Talent",
                        fontWeight = FontWeight.Bold,
                        fontSize = 20.sp,
                        color = DevLinkTextPrimary
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                }

                // Search TextField
                OutlinedTextField(
                    value = query,
                    onValueChange = { query = it },
                    placeholder = { Text("Search by name or username...") },
                    leadingIcon = { Icon(imageVector = Icons.Default.Search, contentDescription = "Search", tint = DevLinkTextSecondary) },
                    trailingIcon = {
                        if (query.isNotEmpty()) {
                            IconButton(onClick = { query = "" }) {
                                Icon(imageVector = Icons.Default.Close, contentDescription = "Clear", tint = DevLinkTextSecondary)
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

                Spacer(modifier = Modifier.height(12.dp))

                // Scrollable Filter Chips row
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    item {
                        Icon(
                            imageVector = Icons.Default.FilterList,
                            contentDescription = "Filters",
                            tint = DevLinkTextSecondary,
                            modifier = Modifier.size(20.dp)
                        )
                    }

                    item {
                        FilterSelectChip(
                            label = selectedCourse?.name ?: "Course",
                            isActive = selectedCourse != null,
                            onClick = { activeFilterDialog = "course" },
                            onClear = { selectedCourse = null }
                        )
                    }

                    item {
                        FilterSelectChip(
                            label = selectedBranch?.name ?: "Branch",
                            isActive = selectedBranch != null,
                            onClick = { activeFilterDialog = "branch" },
                            onClear = { selectedBranch = null }
                        )
                    }

                    item {
                        FilterSelectChip(
                            label = if (selectedYear != null) "Year $selectedYear" else "Year",
                            isActive = selectedYear != null,
                            onClick = { activeFilterDialog = "year" },
                            onClear = { selectedYear = null }
                        )
                    }

                    item {
                        FilterSelectChip(
                            label = if (selectedSemester != null) "Sem $selectedSemester" else "Semester",
                            isActive = selectedSemester != null,
                            onClick = { activeFilterDialog = "semester" },
                            onClear = { selectedSemester = null }
                        )
                    }

                    item {
                        FilterSelectChip(
                            label = selectedSkill.ifBlank { "Skill" },
                            isActive = selectedSkill.isNotBlank(),
                            onClick = { activeFilterDialog = "skill" },
                            onClear = { selectedSkill = "" }
                        )
                    }

                    item {
                        FilterSelectChip(
                            label = selectedBadge.ifBlank { "Badge" },
                            isActive = selectedBadge.isNotBlank(),
                            onClick = { activeFilterDialog = "badge" },
                            onClear = { selectedBadge = "" }
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        if (recruiterMode && query.isEmpty() && selectedCourse == null && selectedBranch == null && selectedYear == null && selectedSemester == null && selectedSkill.isBlank() && selectedBadge.isBlank()) {
            Text(
                text = "Suggested Candidates",
                fontWeight = FontWeight.Bold,
                fontSize = 15.sp,
                color = DevLinkTextPrimary,
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
            )
        }

        // Results Section
        Box(modifier = Modifier.weight(1f)) {
            if (isSearching) {
                CircularProgressIndicator(
                    modifier = Modifier.align(Alignment.Center),
                    color = DevLinkBlue
                )
            } else if (!searchError.isNullOrBlank()) {
                Text(
                    text = searchError,
                    color = DevLinkError,
                    fontSize = 14.sp,
                    modifier = Modifier
                        .align(Alignment.Center)
                        .padding(24.dp),
                    textAlign = TextAlign.Center
                )
            } else if (searchResults.isEmpty()) {
                Text(
                    text = "No matching profiles found.",
                    color = DevLinkTextSecondary,
                    fontSize = 14.sp,
                    modifier = Modifier
                        .align(Alignment.Center)
                        .padding(24.dp),
                    textAlign = TextAlign.Center
                )
            } else {
                val sortedList = remember(searchResults, sortBy) {
                    when (sortBy) {
                        "Top Rated" -> searchResults.sortedByDescending { it.totalPoints ?: 0 }
                        "Name" -> searchResults.sortedBy { it.name ?: it.username }
                        else -> searchResults
                    }
                }

                LazyColumn(
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    if (recruiterMode) {
                        item {
                            Row(
                                modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "${sortedList.size} Candidates Found",
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = DevLinkTextSecondary
                                )
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text("Sort by: ", fontSize = 12.sp, color = DevLinkTextSecondary)
                                    var expanded by remember { mutableStateOf(false) }
                                    Box {
                                        Text(
                                            text = sortBy,
                                            fontSize = 12.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = DevLinkBlue,
                                            modifier = Modifier.clickable { expanded = true }
                                        )
                                        DropdownMenu(
                                            expanded = expanded,
                                            onDismissRequest = { expanded = false }
                                        ) {
                                            listOf("Top Rated", "Name").forEach { option ->
                                                DropdownMenuItem(
                                                    text = { Text(option) },
                                                    onClick = {
                                                        sortBy = option
                                                        expanded = false
                                                    }
                                                )
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }

                    items(sortedList) { student ->
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { onUserClick(student) },
                            shape = RoundedCornerShape(16.dp),
                            colors = CardDefaults.cardColors(containerColor = Color.White),
                            border = BorderStroke(1.dp, DevLinkBorder),
                            elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
                        ) {
                            Row(
                                modifier = Modifier
                                    .padding(16.dp)
                                    .fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                com.zyproo.devlink.ui.common.UserAvatar(
                                    model = student.photoUrl,
                                    name = student.name ?: student.username,
                                    modifier = Modifier.size(52.dp),
                                    backgroundColor = DevLinkBlueContainer,
                                    textColor = DevLinkBlue,
                                    fontSize = 16.sp
                                )

                                Spacer(modifier = Modifier.width(12.dp))

                                // Information details
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = student.name ?: student.username,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 15.sp,
                                        color = DevLinkTextPrimary
                                    )
                                    Text(
                                        text = "@${student.username}",
                                        fontSize = 12.sp,
                                        color = DevLinkTextSecondary
                                    )
                                    Spacer(modifier = Modifier.height(4.dp))

                                    val tagText = when {
                                        student.badges.isNotEmpty() -> student.badges.first()
                                        else -> "${student.course ?: ""} · Sem ${student.semester ?: 1}"
                                    }
                                    Surface(
                                        color = DevLinkBlueContainer,
                                        shape = RoundedCornerShape(6.dp)
                                    ) {
                                        Text(
                                            text = tagText,
                                            fontSize = 10.sp,
                                            color = DevLinkBlue,
                                            fontWeight = FontWeight.SemiBold,
                                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                                        )
                                    }
                                }

                                Spacer(modifier = Modifier.width(8.dp))

                                // Points Chip & Quick Actions
                                Column(horizontalAlignment = Alignment.End) {
                                    Text(
                                        text = "${student.totalPoints ?: 0} pts",
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 14.sp,
                                        color = DevLinkBlue
                                    )
                                    Text(
                                        text = "Rank #${student.rank ?: "-"}",
                                        fontSize = 11.sp,
                                        color = DevLinkTextSecondary
                                    )

                                    if (recruiterMode) {
                                        Spacer(modifier = Modifier.height(8.dp))
                                        Row(
                                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            val isSaved = savedStudentIds.contains(student.userId)
                                            Surface(
                                                shape = RoundedCornerShape(8.dp),
                                                color = if (isSaved) DevLinkBlue else Color.White,
                                                border = BorderStroke(1.5.dp, DevLinkBlue),
                                                modifier = Modifier
                                                    .size(32.dp)
                                                    .clip(RoundedCornerShape(8.dp))
                                                    .clickable { onShortlistClick?.invoke(student) }
                                            ) {
                                                Box(contentAlignment = Alignment.Center) {
                                                    Icon(
                                                        imageVector = if (isSaved) Icons.Default.Bookmark else Icons.Default.BookmarkBorder,
                                                        contentDescription = if (isSaved) "Shortlisted" else "Shortlist candidate",
                                                        tint = if (isSaved) Color.White else DevLinkBlue,
                                                        modifier = Modifier.size(18.dp)
                                                    )
                                                }
                                            }

                                            Surface(
                                                shape = RoundedCornerShape(8.dp),
                                                color = DevLinkBlueContainer,
                                                border = BorderStroke(1.dp, Color(0xFFBFDBFE)),
                                                modifier = Modifier
                                                    .size(32.dp)
                                                    .clip(RoundedCornerShape(8.dp))
                                                    .clickable { onMessageClick?.invoke(student) }
                                            ) {
                                                Box(contentAlignment = Alignment.Center) {
                                                    Icon(
                                                        imageVector = Icons.Default.Chat,
                                                        contentDescription = "Message",
                                                        tint = DevLinkBlue,
                                                        modifier = Modifier.size(17.dp)
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
                                        .padding(horizontal = 8.dp, vertical = 10.dp),
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
                                        .padding(horizontal = 8.dp, vertical = 10.dp),
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
                                        .padding(horizontal = 8.dp, vertical = 10.dp),
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
                        "semester" -> {
                            listOf(1, 2, 3, 4, 5, 6, 7, 8).forEach { semVal ->
                                val isSelected = selectedSemester == semVal
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clip(RoundedCornerShape(10.dp))
                                        .background(if (isSelected) Color(0xFFEFF6FF) else Color.Transparent)
                                        .clickable {
                                            selectedSemester = semVal
                                            activeFilterDialog = null
                                        }
                                        .padding(horizontal = 8.dp, vertical = 10.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    RadioButton(
                                        selected = isSelected,
                                        colors = RadioButtonDefaults.colors(selectedColor = DevLinkBlue),
                                        onClick = {
                                            selectedSemester = semVal
                                            activeFilterDialog = null
                                        }
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = "Semester $semVal",
                                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                        color = if (isSelected) DevLinkBlue else DevLinkTextPrimary
                                    )
                                }
                            }
                        }
                        "skill" -> {
                            var skillInput by remember { mutableStateOf(selectedSkill) }
                            OutlinedTextField(
                                value = skillInput,
                                onValueChange = { skillInput = it },
                                placeholder = { Text("e.g. Kotlin") },
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(10.dp),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = DevLinkBlue,
                                    unfocusedBorderColor = DevLinkBorder
                                )
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Button(
                                onClick = {
                                    selectedSkill = skillInput
                                    activeFilterDialog = null
                                },
                                modifier = Modifier.align(Alignment.End),
                                colors = ButtonDefaults.buttonColors(containerColor = DevLinkBlue)
                            ) {
                                Text("Apply")
                            }
                        }
                        "badge" -> {
                            var badgeInput by remember { mutableStateOf(selectedBadge) }
                            OutlinedTextField(
                                value = badgeInput,
                                onValueChange = { badgeInput = it },
                                placeholder = { Text("e.g. Android Developer") },
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(10.dp),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = DevLinkBlue,
                                    unfocusedBorderColor = DevLinkBorder
                                )
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Button(
                                onClick = {
                                    selectedBadge = badgeInput
                                    activeFilterDialog = null
                                },
                                modifier = Modifier.align(Alignment.End),
                                colors = ButtonDefaults.buttonColors(containerColor = DevLinkBlue)
                            ) {
                                Text("Apply")
                            }
                        }
                    }
                }
            }
        )
    }
}

@Composable
fun FilterSelectChip(
    label: String,
    isActive: Boolean,
    onClick: () -> Unit,
    onClear: () -> Unit
) {
    Surface(
        modifier = Modifier.clickable(onClick = onClick),
        shape = RoundedCornerShape(20.dp),
        color = if (isActive) DevLinkBlue else Color(0xFFF1F5F9),
        border = BorderStroke(1.dp, if (isActive) DevLinkBlue else DevLinkBorder),
        shadowElevation = if (isActive) 1.dp else 0.dp
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Text(
                text = label,
                fontSize = 12.sp,
                fontWeight = if (isActive) FontWeight.Bold else FontWeight.Medium,
                color = if (isActive) Color.White else DevLinkTextPrimary
            )
            if (isActive) {
                Icon(
                    imageVector = Icons.Default.Close,
                    contentDescription = "Clear filter",
                    tint = Color.White,
                    modifier = Modifier
                        .size(14.dp)
                        .clickable(onClick = onClear)
                )
            }
        }
    }
}
