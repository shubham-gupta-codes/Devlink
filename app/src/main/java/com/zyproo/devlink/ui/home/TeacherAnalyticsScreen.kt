package com.zyproo.devlink.ui.home

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil3.compose.AsyncImage
import com.zyproo.devlink.common.toFullMediaUrl
import com.zyproo.devlink.data.remote.dto.*
import com.zyproo.devlink.ui.theme.*

@Composable
fun TeacherAnalyticsScreen(
    analytics: TeacherAnalyticsResponse?,
    rankings: List<ProfileResponse>,
    onFilterChange: (Course?, Branch?, Int?) -> Unit = { _, _, _ -> },
    onUserClick: (ProfileResponse) -> Unit = {}
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(DevLinkBackground)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // ==========================================
            // RANKINGS LEADERBOARD
            // ==========================================
            
            // Cohort filters (Course, Branch, Year)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                var selectedCourse by remember { mutableStateOf<Course?>(null) }
                var selectedBranch by remember { mutableStateOf<Branch?>(null) }
                var selectedYear by remember { mutableStateOf<Int?>(null) }

                Box(modifier = Modifier.weight(1f)) {
                    FilterDropdown(
                        label = "Course",
                        selectedValue = selectedCourse,
                        options = Course.values().toList(),
                        onValueChange = {
                            selectedCourse = it
                            onFilterChange(it, selectedBranch, selectedYear)
                        },
                        itemLabel = { it.name }
                    )
                }
                Box(modifier = Modifier.weight(1f)) {
                    FilterDropdown(
                        label = "Branch",
                        selectedValue = selectedBranch,
                        options = Branch.values().toList(),
                        onValueChange = {
                            selectedBranch = it
                            onFilterChange(selectedCourse, it, selectedYear)
                        },
                        itemLabel = { it.name }
                    )
                }
                Box(modifier = Modifier.weight(1f)) {
                    FilterDropdown(
                        label = "Year",
                        selectedValue = selectedYear,
                        options = (2022..2026).toList(),
                        onValueChange = {
                            selectedYear = it
                            onFilterChange(selectedCourse, selectedBranch, it)
                        },
                        itemLabel = { it.toString() }
                    )
                }
            }

            if (rankings.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(150.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text("No student rankings available.", color = DevLinkTextSecondary)
                }
            } else {
                // Podium Cards Row
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 12.dp),
                    horizontalArrangement = Arrangement.SpaceEvenly,
                    verticalAlignment = Alignment.Bottom
                ) {
                    if (rankings.size >= 2) {
                        PodiumCard(
                            rank = 2,
                            name = rankings[1].name ?: rankings[1].username,
                            points = rankings[1].totalPoints ?: 0,
                            photoUrl = rankings[1].photoUrl,
                            modifier = Modifier
                                .weight(1f)
                                .clickable { onUserClick(rankings[1]) }
                        )
                    }
                    if (rankings.isNotEmpty()) {
                        PodiumCard(
                            rank = 1,
                            name = rankings[0].name ?: rankings[0].username,
                            points = rankings[0].totalPoints ?: 0,
                            photoUrl = rankings[0].photoUrl,
                            modifier = Modifier
                                .weight(1.2f)
                                .clickable { onUserClick(rankings[0]) }
                        )
                    }
                    if (rankings.size >= 3) {
                        PodiumCard(
                            rank = 3,
                            name = rankings[2].name ?: rankings[2].username,
                            points = rankings[2].totalPoints ?: 0,
                            photoUrl = rankings[2].photoUrl,
                            modifier = Modifier
                                .weight(1f)
                                .clickable { onUserClick(rankings[2]) }
                        )
                    }
                }

                // Rankings List
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    border = BorderStroke(1.dp, DevLinkBorder)
                ) {
                    Column {
                        rankings.forEachIndexed { idx, student ->
                            val studentPhoto = student.photoUrl.toFullMediaUrl()
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable { onUserClick(student) }
                                    .padding(horizontal = 16.dp, vertical = 12.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier.weight(1f)
                                ) {
                                    Text(
                                        text = "#${idx + 1}",
                                        fontWeight = FontWeight.Bold,
                                        color = DevLinkBlue,
                                        fontSize = 14.sp,
                                        modifier = Modifier.width(32.dp)
                                    )
                                    Box(
                                        modifier = Modifier
                                            .size(36.dp)
                                            .clip(CircleShape)
                                            .background(DevLinkBlueContainer),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        if (!studentPhoto.isNullOrBlank()) {
                                            AsyncImage(
                                                model = studentPhoto,
                                                contentDescription = student.name ?: student.username,
                                                contentScale = ContentScale.Crop,
                                                modifier = Modifier.fillMaxSize()
                                            )
                                        } else {
                                            Text(
                                                text = (student.name ?: student.username).take(2).uppercase(),
                                                color = DevLinkBlue,
                                                fontWeight = FontWeight.Bold,
                                                fontSize = 12.sp
                                            )
                                        }
                                    }
                                    Spacer(modifier = Modifier.width(10.dp))
                                    Column {
                                        Text(
                                            text = student.name ?: student.username,
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 14.sp,
                                            color = DevLinkTextPrimary
                                        )
                                        Text(
                                            text = "${student.course ?: "N/A"} - ${student.branch ?: "N/A"}",
                                            fontSize = 11.sp,
                                            color = DevLinkTextSecondary
                                        )
                                    }
                                }
                                Text(
                                    text = "${student.totalPoints ?: 0} pts",
                                    fontWeight = FontWeight.Bold,
                                    color = DevLinkBlue,
                                    fontSize = 14.sp
                                )
                            }
                            if (idx < rankings.lastIndex) {
                                HorizontalDivider(color = DevLinkBorder)
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun PodiumCard(
    rank: Int,
    name: String,
    points: Int,
    photoUrl: String? = null,
    modifier: Modifier = Modifier
) {
    val fullPhotoUrl = photoUrl.toFullMediaUrl()
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(4.dp),
        modifier = modifier
    ) {
        Box(contentAlignment = Alignment.BottomCenter) {
            val avatarSize = when (rank) {
                1 -> 76.dp
                2 -> 64.dp
                else -> 60.dp
            }
            val borderCol = when (rank) {
                1 -> Color(0xFFFFD700) // Gold
                2 -> Color(0xFFC0C0C0) // Silver
                else -> Color(0xFFCD7F32) // Bronze
            }
            Box(
                modifier = Modifier
                    .size(avatarSize)
                    .clip(CircleShape)
                    .background(DevLinkBlueContainer)
                    .border(3.dp, borderCol, CircleShape),
                contentAlignment = Alignment.Center
            ) {
                if (!fullPhotoUrl.isNullOrBlank()) {
                    AsyncImage(
                        model = fullPhotoUrl,
                        contentDescription = name,
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxSize()
                    )
                } else {
                    Text(
                        text = name.take(2).uppercase(),
                        color = DevLinkBlue,
                        fontWeight = FontWeight.Bold,
                        fontSize = if (rank == 1) 20.sp else 16.sp
                    )
                }
            }
            Box(
                modifier = Modifier
                    .clip(CircleShape)
                    .background(borderCol)
                    .padding(horizontal = 6.dp, vertical = 2.dp)
                    .offset(y = 6.dp)
            ) {
                Text(
                    text = "#$rank",
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    fontSize = 10.sp
                )
            }
        }
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = name,
            fontWeight = FontWeight.Bold,
            fontSize = 13.sp,
            maxLines = 1,
            color = DevLinkTextPrimary
        )
        Text(
            text = "$points pts",
            fontSize = 11.sp,
            color = DevLinkBlue,
            fontWeight = FontWeight.Bold
        )
    }
}

@Composable
fun <T> FilterDropdown(
    label: String,
    selectedValue: T?,
    options: List<T>,
    onValueChange: (T?) -> Unit,
    itemLabel: (T) -> String
) {
    var expanded by remember { mutableStateOf(false) }
    Box(modifier = Modifier.fillMaxWidth()) {
        OutlinedCard(
            onClick = { expanded = true },
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(8.dp)
        ) {
            Column(modifier = Modifier.padding(horizontal = 8.dp, vertical = 6.dp)) {
                Text(label, fontSize = 10.sp, color = Color.Gray)
                Text(
                    text = selectedValue?.let { itemLabel(it) } ?: "All",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1
                )
            }
        }
        DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
            DropdownMenuItem(
                text = { Text("All", fontSize = 13.sp) },
                onClick = {
                    onValueChange(null)
                    expanded = false
                }
            )
            options.forEach { option ->
                DropdownMenuItem(
                    text = { Text(itemLabel(option), fontSize = 13.sp) },
                    onClick = {
                        onValueChange(option)
                        expanded = false
                    }
                )
            }
        }
    }
}
