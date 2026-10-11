package com.zyproo.devlink.ui.home

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Code
import androidx.compose.material.icons.filled.Link
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.zyproo.devlink.data.remote.dto.*
import com.zyproo.devlink.ui.theme.DevLinkBlue

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProjectDetailsDialog(
    project: ProjectResponse,

    comments: List<ProjectCommentResponse>,


    onDismiss: () -> Unit,
    onLike: () -> Unit,
    onAddComment: (String, Long?) -> Unit,
    onLoadComments: () -> Unit
) {
    val uriHandler = androidx.compose.ui.platform.LocalUriHandler.current
    var newCommentText by remember { mutableStateOf("") }
    var replyingToComment by remember { mutableStateOf<ProjectCommentResponse?>(null) }

    LaunchedEffect(project.id) {
        onLoadComments()
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        confirmButton = {},
        title = {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(project.name, fontWeight = FontWeight.Bold, fontSize = 18.sp)
                IconButton(onClick = onDismiss) {
                    Icon(Icons.Default.Close, contentDescription = "Close")
                }
            }
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(max = 500.dp)
            ) {
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .verticalScroll(rememberScrollState())
                ) {
                    Text("By @${project.studentUsername} (${project.studentName})", fontSize = 12.sp, color = Color.Gray)
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(project.description, fontSize = 14.sp, color = Color.DarkGray)
                    
                    if (project.githubUrl != null) {
                        Spacer(modifier = Modifier.height(6.dp))
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
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
                            Icon(Icons.Default.Code, contentDescription = null, tint = DevLinkBlue, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(project.githubUrl, fontSize = 12.sp, color = DevLinkBlue, fontWeight = FontWeight.Medium)
                        }
                    }
                    if (project.demoUrl != null) {
                        Spacer(modifier = Modifier.height(4.dp))
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
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
                            Icon(Icons.Default.Link, contentDescription = null, tint = DevLinkBlue, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(project.demoUrl, fontSize = 12.sp, color = DevLinkBlue, fontWeight = FontWeight.Medium)
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))
                    HorizontalDivider(color = Color(0xFFF1F1F1))
                    Spacer(modifier = Modifier.height(8.dp))
                    Text("Comments (${comments.size})", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                    Spacer(modifier = Modifier.height(8.dp))

                    if (comments.isEmpty()) {
                        Text("No comments yet. Start the conversation!", fontSize = 12.sp, color = Color.Gray)
                    } else {
                        comments.forEach { c ->
                            CommentRow(
                                comment = c,
                                onReplyClick = { replyingToComment = c }
                            )
                        }
                    }
                }

                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 8.dp)
                ) {
                    if (replyingToComment != null) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(Color(0xFFF1F5FE))
                                .padding(horizontal = 8.dp, vertical = 4.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("Replying to @${replyingToComment!!.username}", fontSize = 11.sp, color = DevLinkBlue)
                            IconButton(onClick = { replyingToComment = null }, modifier = Modifier.size(18.dp)) {
                                Icon(Icons.Default.Close, contentDescription = null, modifier = Modifier.size(12.dp))
                            }
                        }
                    }
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        OutlinedTextField(
                            value = newCommentText,
                            onValueChange = { newCommentText = it },
                            placeholder = { Text("Write a comment...") },
                            modifier = Modifier.weight(1f),
                            maxLines = 2
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        TextButton(
                            onClick = {
                                if (newCommentText.isNotBlank()) {
                                    onAddComment(newCommentText, replyingToComment?.id)
                                    newCommentText = ""
                                    replyingToComment = null
                                }
                            },
                            enabled = newCommentText.isNotBlank()
                        ) {
                            Text("Post", fontWeight = FontWeight.Bold, color = DevLinkBlue)
                        }
                    }
                }
            }
        }
    )
}

@Composable
fun CommentRow(
    comment: ProjectCommentResponse,
    onReplyClick: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("@${comment.username}", fontWeight = FontWeight.Bold, fontSize = 13.sp)
            Text(
                text = "Reply",
                fontSize = 11.sp,
                color = DevLinkBlue,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.clickable { onReplyClick() }
            )
        }
        Text(comment.content, fontSize = 13.sp, color = Color.DarkGray)
        
        if (comment.replies.isNotEmpty()) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = 16.dp, top = 4.dp)
                    .background(Color(0xFFFAFAFA))
                    .padding(8.dp),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                comment.replies.forEach { r ->
                    Column {
                        Text("@${r.username}", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                        Text(r.content, fontSize = 12.sp, color = Color.DarkGray)
                    }
                }
            }
        }
        Spacer(modifier = Modifier.height(4.dp))
        HorizontalDivider(color = Color(0xFFF9F9F9))
    }
}
