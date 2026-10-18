package com.zyproo.devlink.ui.home

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Comment
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
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
import android.content.Intent
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.BookmarkBorder
import androidx.compose.material.icons.filled.Share
import androidx.compose.ui.platform.LocalContext
import com.zyproo.devlink.common.formatRelativeTime
import com.zyproo.devlink.common.toFullMediaUrl
import com.zyproo.devlink.data.remote.dto.*
import com.zyproo.devlink.ui.theme.*

@OptIn(ExperimentalLayoutApi::class, ExperimentalMaterial3Api::class)
@Composable
fun StudentFeedScreen(
    feed: List<ProjectResponse>,
    commentsMap: Map<Long, List<ProjectCommentResponse>>,
    savedProjectIds: Set<Long> = emptySet(),
    onLikeProject: (Long) -> Unit,
    onSaveProject: (Long) -> Unit = {},
    onCommentProject: (Long, String, Long?) -> Unit,
    onLoadComments: (Long) -> Unit,
    onLoadFeed: (String) -> Unit,
    onProjectClick: (ProjectResponse) -> Unit,
    onAuthorClick: ((String) -> Unit)? = null
) {
    val context = LocalContext.current
    var selectedFilter by remember { mutableStateOf("trending") }

    LaunchedEffect(selectedFilter) {
        onLoadFeed(if (selectedFilter == "trending") "trending" else "all")
    }

    val displayedFeed = remember(feed, savedProjectIds, selectedFilter) {
        when (selectedFilter) {
            "saved" -> feed.filter { savedProjectIds.contains(it.id) }
            else -> feed
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(DevLinkBackground)
    ) {
        // Top Filter Bar Segment
        Surface(
            color = Color.White,
            border = BorderStroke(1.dp, DevLinkBorder)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                listOf(
                    "trending" to "🔥 Trending",
                    "projects" to "🚀 All Projects",
                    "saved" to "🔖 Saved (${savedProjectIds.size})"
                ).forEach { (key, label) ->
                    val isSelected = selectedFilter == key
                    Surface(
                        modifier = Modifier.clickable { selectedFilter = key },
                        shape = RoundedCornerShape(20.dp),
                        color = if (isSelected) DevLinkBlue else Color(0xFFF1F5F9)
                    ) {
                        Text(
                            text = label,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (isSelected) Color.White else DevLinkTextPrimary,
                            modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp)
                        )
                    }
                }
            }
        }

        if (displayedFeed.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(24.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = if (selectedFilter == "saved")
                        "No saved projects yet. Click the bookmark icon on any project to save it here!"
                    else
                        "No projects verified in the feed.",
                    color = DevLinkTextSecondary,
                    textAlign = TextAlign.Center,
                    style = MaterialTheme.typography.bodyMedium
                )
            }
        } else {
            LazyColumn(
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                items(displayedFeed) { proj ->

                    val isSaved = savedProjectIds.contains(proj.id)
                    val timeAgo = formatRelativeTime(proj.createdAt)

                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onProjectClick(proj) },
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = Color.White),
                        border = BorderStroke(1.dp, DevLinkBorder),
                        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(
                                    modifier = Modifier
                                        .weight(1f)
                                        .then(
                                            if (onAuthorClick != null) Modifier.clickable { onAuthorClick(proj.studentUsername) }
                                            else Modifier
                                        ),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    val authorPhotoUrl = proj.studentPhotoUrl.toFullMediaUrl()
                                    com.zyproo.devlink.ui.common.UserAvatar(
                                        model = authorPhotoUrl,
                                        name = proj.studentName,
                                        modifier = Modifier.size(40.dp),
                                        backgroundColor = DevLinkBlueContainer,
                                        textColor = DevLinkBlue,
                                        fontSize = 14.sp
                                    )
                                    Spacer(modifier = Modifier.width(10.dp))
                                    Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = proj.studentName,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 14.sp,
                                        color = DevLinkTextPrimary
                                    )
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Text(
                                            text = "@${proj.studentUsername}",
                                            fontSize = 12.sp,
                                            color = DevLinkTextSecondary
                                        )
                                        if (timeAgo.isNotBlank()) {
                                            Text(
                                                text = " • $timeAgo",
                                                fontSize = 12.sp,
                                                color = DevLinkTextSecondary
                                            )
                                        }
                                    }
                                }
                                }
                                Surface(
                                    color = DevLinkBlueContainer,
                                    shape = RoundedCornerShape(6.dp)
                                ) {
                                    Text(
                                        text = "VERIFIED",
                                        fontSize = 9.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = DevLinkBlue,
                                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(12.dp))
                            Text(
                                text = proj.name,
                                fontWeight = FontWeight.Bold,
                                fontSize = 16.sp,
                                color = DevLinkTextPrimary
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = proj.description,
                                fontSize = 13.sp,
                                color = DevLinkTextSecondary,
                                maxLines = 3,
                                lineHeight = 18.sp
                            )
                            Spacer(modifier = Modifier.height(10.dp))

                            if (proj.techStack.isNotBlank()) {
                                FlowRow(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                                    verticalArrangement = Arrangement.spacedBy(4.dp)
                                ) {
                                    proj.techStack.split(",").forEach { tag ->
                                        Surface(
                                            color = DevLinkBlueContainer,
                                            shape = RoundedCornerShape(6.dp)
                                        ) {
                                            Text(
                                                text = tag.trim(),
                                                fontSize = 11.sp,
                                                fontWeight = FontWeight.Medium,
                                                color = DevLinkBlue,
                                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                                            )
                                        }
                                    }
                                }
                            }

                            if (proj.imageUrls.isNotEmpty()) {
                                Spacer(modifier = Modifier.height(10.dp))
                                if (proj.imageUrls.size == 1) {
                                    val imageUrl = proj.imageUrls.first().toFullMediaUrl()
                                    if (!imageUrl.isNullOrBlank()) {
                                        Box(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .heightIn(min = 180.dp, max = 260.dp)
                                                .clip(RoundedCornerShape(12.dp))
                                                .background(Color(0xFFF1F5F9)),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            AsyncImage(
                                                model = imageUrl,
                                                contentDescription = "Project Image Preview",
                                                contentScale = ContentScale.Fit,
                                                modifier = Modifier.fillMaxSize()
                                            )
                                        }
                                    }
                                } else {
                                    LazyRow(
                                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                                        contentPadding = PaddingValues(vertical = 2.dp)
                                    ) {
                                        itemsIndexed(proj.imageUrls) { idx, rawUrl ->
                                            val fullUrl = rawUrl.toFullMediaUrl()
                                            if (!fullUrl.isNullOrBlank()) {
                                                Box(
                                                    modifier = Modifier
                                                        .width(220.dp)
                                                        .height(200.dp)
                                                        .clip(RoundedCornerShape(12.dp))
                                                        .background(Color(0xFFF1F5F9)),
                                                    contentAlignment = Alignment.Center
                                                ) {
                                                    AsyncImage(
                                                        model = fullUrl,
                                                        contentDescription = "Screenshot ${idx + 1}",
                                                        contentScale = ContentScale.Fit,
                                                        modifier = Modifier.fillMaxSize()
                                                    )
                                                    Surface(
                                                        color = Color.White.copy(alpha = 0.9f),
                                                        shape = RoundedCornerShape(6.dp),
                                                        border = BorderStroke(1.dp, DevLinkBorder),
                                                        modifier = Modifier
                                                            .align(Alignment.TopStart)
                                                            .padding(6.dp)
                                                    ) {
                                                        Text(
                                                            text = "${idx + 1}/${proj.imageUrls.size}",
                                                            color = DevLinkTextPrimary,
                                                            fontSize = 10.sp,
                                                            fontWeight = FontWeight.Bold,
                                                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                                        )
                                                    }
                                                }
                                            }
                                        }
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.height(12.dp))
                            HorizontalDivider(color = DevLinkBorder)
                            Spacer(modifier = Modifier.height(8.dp))

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(
                                    horizontalArrangement = Arrangement.spacedBy(20.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        modifier = Modifier.clickable { onLikeProject(proj.id) }
                                    ) {
                                        Icon(
                                            imageVector = if (proj.likedByCurrentUser) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                                            contentDescription = "Like",
                                            tint = if (proj.likedByCurrentUser) Color(0xFFEF4444) else DevLinkTextSecondary,
                                            modifier = Modifier.size(20.dp)
                                        )
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text(
                                            text = "${proj.likeCount}",
                                            fontSize = 13.sp,
                                            fontWeight = FontWeight.SemiBold,
                                            color = DevLinkTextPrimary
                                        )
                                    }

                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        modifier = Modifier.clickable { onProjectClick(proj) }
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Comment,
                                            contentDescription = "Comment",
                                            tint = DevLinkBlue,
                                            modifier = Modifier.size(20.dp)
                                        )
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text(
                                            text = "${proj.commentCount}",
                                            fontSize = 13.sp,
                                            fontWeight = FontWeight.SemiBold,
                                            color = DevLinkTextPrimary
                                        )
                                    }
                                }

                                Row(
                                    horizontalArrangement = Arrangement.spacedBy(16.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    IconButton(
                                        onClick = { onSaveProject(proj.id) },
                                        modifier = Modifier.size(24.dp)
                                    ) {
                                        Icon(
                                            imageVector = if (isSaved) Icons.Default.Bookmark else Icons.Default.BookmarkBorder,
                                            contentDescription = "Save Project",
                                            tint = if (isSaved) DevLinkBlue else DevLinkTextSecondary,
                                            modifier = Modifier.size(20.dp)
                                        )
                                    }

                                    IconButton(
                                        onClick = {
                                            val shareIntent = Intent(Intent.ACTION_SEND).apply {
                                                type = "text/plain"
                                                putExtra(Intent.EXTRA_SUBJECT, proj.name)
                                                putExtra(Intent.EXTRA_TEXT, "Check out this project on DevLink by @${proj.studentUsername}:\n\n📌 ${proj.name}\n${proj.description}\n\n${proj.githubUrl.orEmpty()}")
                                            }
                                            context.startActivity(Intent.createChooser(shareIntent, "Share Project"))
                                        },
                                        modifier = Modifier.size(24.dp)
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Share,
                                            contentDescription = "Share Project",
                                            tint = DevLinkTextSecondary,
                                            modifier = Modifier.size(20.dp)
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
