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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil3.compose.AsyncImage
import com.zyproo.devlink.common.toFullMediaUrl
import com.zyproo.devlink.data.remote.dto.*
import com.zyproo.devlink.ui.theme.*

import android.content.Intent
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.BookmarkBorder
import androidx.compose.material.icons.filled.Share
import androidx.compose.ui.platform.LocalContext
import com.zyproo.devlink.common.formatRelativeTime
import kotlinx.coroutines.launch

@OptIn(ExperimentalLayoutApi::class, ExperimentalMaterial3Api::class)
@Composable
fun ProjectDetailsScreen(
    project: ProjectResponse,
    comments: List<ProjectCommentResponse>,
    isSaved: Boolean = false,
    onBackClick: () -> Unit,
    onLike: () -> Unit,
    onSave: () -> Unit = {},
    onAddComment: (String, Long?) -> Unit,
    onLoadComments: () -> Unit,
    onAuthorClick: ((String) -> Unit)? = null
) {
    val context = LocalContext.current
    val uriHandler = androidx.compose.ui.platform.LocalUriHandler.current
    var newCommentText by remember { mutableStateOf("") }
    var replyingToComment by remember { mutableStateOf<ProjectCommentResponse?>(null) }
    val timeAgo = formatRelativeTime(project.createdAt)

    LaunchedEffect(project.id) {
        onLoadComments()
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .fillMaxWidth()
                            .then(
                                if (onAuthorClick != null) Modifier.clickable { onAuthorClick(project.studentUsername) }
                                else Modifier
                            )
                    ) {
                        val authorPhoto = project.studentPhotoUrl.toFullMediaUrl()
                        Box(
                            modifier = Modifier
                                .size(34.dp)
                                .clip(CircleShape)
                                .background(DevLinkBlueContainer),
                            contentAlignment = Alignment.Center
                        ) {
                            if (!authorPhoto.isNullOrBlank()) {
                                AsyncImage(
                                    model = authorPhoto,
                                    contentDescription = "Avatar",
                                    contentScale = ContentScale.Crop,
                                    modifier = Modifier.fillMaxSize()
                                )
                            } else {
                                Text(
                                    text = project.studentName.take(2).uppercase(),
                                    fontWeight = FontWeight.Bold,
                                    color = DevLinkBlue,
                                    fontSize = 12.sp
                                )
                            }
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text(
                                text = project.studentName,
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.sp,
                                color = DevLinkTextPrimary
                            )
                            Text(
                                text = "@${project.studentUsername}",
                                fontSize = 11.sp,
                                color = DevLinkTextSecondary
                            )
                        }
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(
                            imageVector = Icons.Default.ArrowBack,
                            contentDescription = "Back",
                            tint = DevLinkTextPrimary
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color.White,
                    titleContentColor = DevLinkTextPrimary,
                    navigationIconContentColor = DevLinkTextPrimary,
                    actionIconContentColor = DevLinkTextPrimary
                )
            )
        },
        bottomBar = {
            // Fixed Bottom Comment Input Bar
            Surface(
                color = Color.White,
                border = BorderStroke(1.dp, DevLinkBorder),
                modifier = Modifier.imePadding()
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 10.dp)
                ) {
                    if (replyingToComment != null) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(8.dp))
                                .background(DevLinkBlueContainer)
                                .padding(horizontal = 10.dp, vertical = 6.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Replying to @${replyingToComment!!.username}",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = DevLinkBlue
                            )
                            IconButton(
                                onClick = { replyingToComment = null },
                                modifier = Modifier.size(20.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Close,
                                    contentDescription = "Cancel reply",
                                    tint = DevLinkBlue,
                                    modifier = Modifier.size(14.dp)
                                )
                            }
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        OutlinedTextField(
                            value = newCommentText,
                            onValueChange = { newCommentText = it },
                            placeholder = { Text("Add a comment...") },
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(20.dp),
                            maxLines = 3,
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = DevLinkBlue,
                                unfocusedBorderColor = DevLinkBorder
                            )
                        )
                        IconButton(
                            onClick = {
                                if (newCommentText.isNotBlank()) {
                                    onAddComment(newCommentText, replyingToComment?.id)
                                    newCommentText = ""
                                    replyingToComment = null
                                }
                            },
                            enabled = newCommentText.isNotBlank(),
                            modifier = Modifier
                                .size(44.dp)
                                .clip(CircleShape)
                                .background(if (newCommentText.isNotBlank()) DevLinkBlue else DevLinkBorder)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Send,
                                contentDescription = "Post Comment",
                                tint = Color.White,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }
                }
            }
        }
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .background(DevLinkBackground),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Main Post Card
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    border = BorderStroke(1.dp, DevLinkBorder),
                    elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        // Project Title in Big Bold Letters
                        Text(
                            text = project.name,
                            fontWeight = FontWeight.Bold,
                            fontSize = 22.sp,
                            color = DevLinkTextPrimary,
                            lineHeight = 28.sp
                        )

                        // Description
                        Text(
                            text = project.description,
                            fontSize = 14.sp,
                            color = DevLinkTextSecondary,
                            lineHeight = 20.sp
                        )

                        // Photos Gallery (Horizontal Swipe / Pager)
                        if (project.imageUrls.isNotEmpty()) {
                            if (project.imageUrls.size == 1) {
                                val imageUrl = project.imageUrls.first().toFullMediaUrl()
                                if (!imageUrl.isNullOrBlank()) {
                                    Box(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .heightIn(min = 200.dp, max = 340.dp)
                                            .clip(RoundedCornerShape(12.dp))
                                            .background(Color(0xFF0F172A)),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        AsyncImage(
                                            model = imageUrl,
                                            contentDescription = "Project Photo",
                                            contentScale = ContentScale.Fit,
                                            modifier = Modifier.fillMaxSize()
                                        )
                                    }
                                }
                            } else {
                                val pagerState = rememberPagerState(pageCount = { project.imageUrls.size })
                                val scope = rememberCoroutineScope()

                                Column(
                                    modifier = Modifier.fillMaxWidth(),
                                    verticalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .heightIn(min = 200.dp, max = 340.dp)
                                    ) {
                                        HorizontalPager(
                                            state = pagerState,
                                            modifier = Modifier.fillMaxSize(),
                                            pageSpacing = 8.dp
                                        ) { page ->
                                            val imageUrl = project.imageUrls[page].toFullMediaUrl()
                                            Box(
                                                modifier = Modifier
                                                    .fillMaxSize()
                                                    .clip(RoundedCornerShape(12.dp))
                                                    .background(Color(0xFF0F172A)),
                                                contentAlignment = Alignment.Center
                                            ) {
                                                if (!imageUrl.isNullOrBlank()) {
                                                    AsyncImage(
                                                        model = imageUrl,
                                                        contentDescription = "Project Photo ${page + 1}",
                                                        contentScale = ContentScale.Fit,
                                                        modifier = Modifier.fillMaxSize()
                                                    )
                                                }
                                                // Photo counter badge
                                                Surface(
                                                    color = Color.Black.copy(alpha = 0.65f),
                                                    shape = RoundedCornerShape(6.dp),
                                                    modifier = Modifier
                                                        .align(Alignment.TopStart)
                                                        .padding(8.dp)
                                                ) {
                                                    Text(
                                                        text = "Photo ${page + 1} of ${project.imageUrls.size}",
                                                        color = Color.White,
                                                        fontSize = 11.sp,
                                                        fontWeight = FontWeight.Bold,
                                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                                                    )
                                                }
                                            }
                                        }

                                        // Left arrow button (if page > 0)
                                        if (pagerState.currentPage > 0) {
                                            IconButton(
                                                onClick = {
                                                    scope.launch {
                                                        pagerState.animateScrollToPage(pagerState.currentPage - 1)
                                                    }
                                                },
                                                modifier = Modifier
                                                    .align(Alignment.CenterStart)
                                                    .padding(start = 6.dp)
                                                    .size(32.dp)
                                                    .background(Color.Black.copy(alpha = 0.5f), CircleShape)
                                            ) {
                                                Icon(
                                                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                                    contentDescription = "Previous Photo",
                                                    tint = Color.White,
                                                    modifier = Modifier.size(18.dp)
                                                )
                                            }
                                        }

                                        // Right arrow button (if page < lastIndex)
                                        if (pagerState.currentPage < project.imageUrls.lastIndex) {
                                            IconButton(
                                                onClick = {
                                                    scope.launch {
                                                        pagerState.animateScrollToPage(pagerState.currentPage + 1)
                                                    }
                                                },
                                                modifier = Modifier
                                                    .align(Alignment.CenterEnd)
                                                    .padding(end = 6.dp)
                                                    .size(32.dp)
                                                    .background(Color.Black.copy(alpha = 0.5f), CircleShape)
                                            ) {
                                                Icon(
                                                    imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                                                    contentDescription = "Next Photo",
                                                    tint = Color.White,
                                                    modifier = Modifier.size(18.dp)
                                                )
                                            }
                                        }
                                    }

                                    // Dot indicators
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.Center,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        repeat(project.imageUrls.size) { index ->
                                            val isSelected = index == pagerState.currentPage
                                            Box(
                                                modifier = Modifier
                                                    .padding(horizontal = 3.dp)
                                                    .width(if (isSelected) 18.dp else 6.dp)
                                                    .height(6.dp)
                                                    .clip(RoundedCornerShape(3.dp))
                                                    .background(if (isSelected) DevLinkBlue else Color(0xFFCBD5E1))
                                                    .clickable {
                                                        scope.launch {
                                                            pagerState.animateScrollToPage(index)
                                                        }
                                                    }
                                            )
                                        }
                                    }
                                }
                            }
                        }

                        // Tech Stack Chips
                        if (project.techStack.isNotBlank()) {
                            FlowRow(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(6.dp),
                                verticalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                project.techStack.split(",").forEach { tag ->
                                    Surface(
                                        color = DevLinkBlueContainer,
                                        shape = RoundedCornerShape(8.dp)
                                    ) {
                                        Text(
                                            text = tag.trim(),
                                            fontSize = 12.sp,
                                            fontWeight = FontWeight.Medium,
                                            color = DevLinkBlue,
                                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                                        )
                                    }
                                }
                            }
                        }

                        // Action Buttons: GitHub Repo & Live Demo
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(10.dp),
                            modifier = Modifier.padding(top = 4.dp)
                        ) {
                            if (!project.githubUrl.isNullOrBlank()) {
                                Surface(
                                    color = Color(0xFFF1F5F9),
                                    shape = RoundedCornerShape(10.dp),
                                    border = BorderStroke(1.dp, DevLinkBorder),
                                    modifier = Modifier.clickable {
                                        val formatted = if (project.githubUrl.startsWith("http://") || project.githubUrl.startsWith("https://")) {
                                            project.githubUrl
                                        } else {
                                            "https://${project.githubUrl}"
                                        }
                                        try {
                                            uriHandler.openUri(formatted)
                                        } catch (e: Exception) {}
                                    }
                                ) {
                                    Row(
                                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Code,
                                            contentDescription = "GitHub",
                                            tint = DevLinkTextPrimary,
                                            modifier = Modifier.size(16.dp)
                                        )
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text(
                                            text = "GitHub Repo",
                                            fontSize = 12.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = DevLinkTextPrimary
                                        )
                                    }
                                }
                            }

                            if (!project.demoUrl.isNullOrBlank()) {
                                Surface(
                                    color = DevLinkBlueContainer,
                                    shape = RoundedCornerShape(10.dp),
                                    border = BorderStroke(1.dp, Color(0xFFBFDBFE)),
                                    modifier = Modifier.clickable {
                                        val formatted = if (project.demoUrl.startsWith("http://") || project.demoUrl.startsWith("https://")) {
                                            project.demoUrl
                                        } else {
                                            "https://${project.demoUrl}"
                                        }
                                        try {
                                            uriHandler.openUri(formatted)
                                        } catch (e: Exception) {}
                                    }
                                ) {
                                    Row(
                                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Link,
                                            contentDescription = "Live Demo",
                                            tint = DevLinkBlue,
                                            modifier = Modifier.size(16.dp)
                                        )
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text(
                                            text = "Live Demo",
                                            fontSize = 12.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = DevLinkBlue
                                        )
                                    }
                                }
                            }
                        }

                        HorizontalDivider(color = DevLinkBorder, modifier = Modifier.padding(vertical = 4.dp))

                        // Like, Comment, Save, Share & Date Bar
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
                                    modifier = Modifier.clickable { onLike() }
                                ) {
                                    Icon(
                                        imageVector = if (project.likedByCurrentUser) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                                        contentDescription = "Like",
                                        tint = if (project.likedByCurrentUser) Color(0xFFEF4444) else DevLinkTextSecondary,
                                        modifier = Modifier.size(22.dp)
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = "${project.likeCount}",
                                        fontSize = 13.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = DevLinkTextPrimary
                                    )
                                }

                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        imageVector = Icons.Default.Comment,
                                        contentDescription = "Comments",
                                        tint = DevLinkBlue,
                                        modifier = Modifier.size(22.dp)
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = "${comments.size}",
                                        fontSize = 13.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = DevLinkTextPrimary
                                    )
                                }
                            }

                            Row(
                                horizontalArrangement = Arrangement.spacedBy(16.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                IconButton(
                                    onClick = onSave,
                                    modifier = Modifier.size(24.dp)
                                ) {
                                    Icon(
                                        imageVector = if (isSaved) Icons.Default.Bookmark else Icons.Default.BookmarkBorder,
                                        contentDescription = "Save",
                                        tint = if (isSaved) DevLinkBlue else DevLinkTextSecondary,
                                        modifier = Modifier.size(20.dp)
                                    )
                                }

                                IconButton(
                                    onClick = {
                                        val shareIntent = Intent(Intent.ACTION_SEND).apply {
                                            type = "text/plain"
                                            putExtra(Intent.EXTRA_SUBJECT, project.name)
                                            putExtra(Intent.EXTRA_TEXT, "Check out this project on DevLink by @${project.studentUsername}:\n\n📌 ${project.name}\n${project.description}\n\n${project.githubUrl.orEmpty()}")
                                        }
                                        context.startActivity(Intent.createChooser(shareIntent, "Share Project"))
                                    },
                                    modifier = Modifier.size(24.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Share,
                                        contentDescription = "Share",
                                        tint = DevLinkTextSecondary,
                                        modifier = Modifier.size(20.dp)
                                    )
                                }

                                if (timeAgo.isNotBlank()) {
                                    Text(
                                        text = timeAgo,
                                        fontSize = 12.sp,
                                        color = DevLinkTextSecondary
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // Comments Section Header
            item {
                Text(
                    text = "Comments (${comments.size})",
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp,
                    color = DevLinkTextPrimary,
                    modifier = Modifier.padding(horizontal = 4.dp)
                )
            }

            if (comments.isEmpty()) {
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = Color.White),
                        border = BorderStroke(1.dp, DevLinkBorder),
                        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(24.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "No comments yet. Be the first to share feedback!",
                                fontSize = 13.sp,
                                color = DevLinkTextSecondary
                            )
                        }
                    }
                }
            } else {
                items(comments) { c ->
                    CommentCard(
                        comment = c,
                        onReplyClick = { replyingToComment = c },
                        onAuthorClick = onAuthorClick
                    )
                }
            }
        }
    }
}

@Composable
fun CommentCard(
    comment: ProjectCommentResponse,
    onReplyClick: () -> Unit,
    onAuthorClick: ((String) -> Unit)? = null
) {
    val commentTimeAgo = formatRelativeTime(comment.createdAt)
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        border = BorderStroke(1.dp, DevLinkBorder),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.then(
                        if (onAuthorClick != null) Modifier.clickable { onAuthorClick(comment.username) }
                        else Modifier
                    )
                ) {
                    val commenterPhoto = comment.photoUrl.toFullMediaUrl()
                    Box(
                        modifier = Modifier
                            .size(32.dp)
                            .clip(CircleShape)
                            .background(DevLinkBlueContainer),
                        contentAlignment = Alignment.Center
                    ) {
                        if (!commenterPhoto.isNullOrBlank()) {
                            AsyncImage(
                                model = commenterPhoto,
                                contentDescription = "Avatar",
                                contentScale = ContentScale.Crop,
                                modifier = Modifier.fillMaxSize()
                            )
                        } else {
                            Text(
                                text = comment.username.take(2).uppercase(),
                                fontWeight = FontWeight.Bold,
                                fontSize = 12.sp,
                                color = DevLinkBlue
                            )
                        }
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Column {
                        Text(
                            text = "@${comment.username}",
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp,
                            color = DevLinkTextPrimary
                        )
                        if (commentTimeAgo.isNotBlank()) {
                            Text(
                                text = commentTimeAgo,
                                fontSize = 11.sp,
                                color = DevLinkTextSecondary
                            )
                        }
                    }
                }
                Surface(
                    color = DevLinkBlueContainer,
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.clickable { onReplyClick() }
                ) {
                    Text(
                        text = "Reply",
                        fontSize = 11.sp,
                        color = DevLinkBlue,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = comment.content,
                fontSize = 13.sp,
                color = DevLinkTextPrimary,
                lineHeight = 18.sp
            )

            // Thread Replies Indented
            if (comment.replies.isNotEmpty()) {
                Spacer(modifier = Modifier.height(8.dp))
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(start = 12.dp)
                        .clip(RoundedCornerShape(10.dp))
                        .background(Color(0xFFF8FAFC))
                        .padding(10.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    comment.replies.forEach { reply ->
                        Column {
                            Text(
                                text = "@${reply.username}",
                                fontWeight = FontWeight.Bold,
                                fontSize = 12.sp,
                                color = DevLinkBlue
                            )
                            Text(
                                text = reply.content,
                                fontSize = 12.sp,
                                color = DevLinkTextPrimary
                            )
                        }
                    }
                }
            }
        }
    }
}
