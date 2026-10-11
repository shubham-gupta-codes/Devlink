package com.zyproo.devlink.ui.chat

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Send
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.zyproo.devlink.data.remote.dto.MessageResponse
import com.zyproo.devlink.ui.theme.*

import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.filled.Group
import androidx.compose.material.icons.filled.Person
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import coil3.compose.AsyncImage
import com.zyproo.devlink.common.toFullMediaUrl

// Helper functions for date separator formatting
private fun getMessageDateSeparator(createdAtStr: String): String {
    return try {
        val instant = java.time.Instant.parse(createdAtStr)
        val zone = java.time.ZoneId.systemDefault()
        val msgDate = instant.atZone(zone).toLocalDate()
        val today = java.time.LocalDate.now(zone)
        val yesterday = today.minusDays(1)
        
        when {
            msgDate.isEqual(today) -> "Today"
            msgDate.isEqual(yesterday) -> "Yesterday"
            else -> java.time.format.DateTimeFormatter.ofPattern("MMMM dd, yyyy")
                .withZone(zone)
                .format(instant)
        }
    } catch (e: Exception) {
        ""
    }
}

private fun formatMessageTime(createdAtStr: String): String {
    return try {
        val instant = java.time.Instant.parse(createdAtStr)
        val zone = java.time.ZoneId.systemDefault()
        java.time.format.DateTimeFormatter.ofPattern("hh:mm a")
            .withZone(zone)
            .format(instant)
    } catch (e: Exception) {
        ""
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChatThreadScreen(
    conversationName: String,
    teamId: Long?,
    photoUrl: String? = null,
    messages: List<MessageResponse>,
    currentUserId: Long,
    onBackClick: () -> Unit,
    onSendMessage: (String) -> Unit,
    onTeamSettingsClick: (Long) -> Unit
) {
    var textInput by remember { mutableStateOf("") }
    val listState = rememberLazyListState()

    // Auto-scroll to the latest message whenever messages list updates
    LaunchedEffect(messages.size) {
        if (messages.isNotEmpty()) {
            listState.animateScrollToItem(messages.size - 1)
        }
    }

    val fullPhotoUrl = photoUrl.toFullMediaUrl()

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .clip(CircleShape)
                                .background(DevLinkBlueContainer),
                            contentAlignment = Alignment.Center
                        ) {
                            if (!fullPhotoUrl.isNullOrBlank()) {
                                AsyncImage(
                                    model = fullPhotoUrl,
                                    contentDescription = conversationName,
                                    contentScale = ContentScale.Crop,
                                    modifier = Modifier.fillMaxSize()
                                )
                            } else {
                                val icon = if (teamId != null) Icons.Default.Group else Icons.Default.Person
                                Icon(
                                    imageVector = icon,
                                    contentDescription = null,
                                    tint = DevLinkBlue,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                        }
                        Text(
                            text = conversationName,
                            fontWeight = FontWeight.Bold,
                            fontSize = 17.sp,
                            maxLines = 1
                        )
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    if (teamId != null) {
                        IconButton(onClick = { onTeamSettingsClick(teamId) }) {
                            Icon(Icons.Default.Settings, contentDescription = "Team Settings")
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
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .imePadding()
                .background(Color(0xFFF6F7FB))
        ) {
            LazyColumn(
                state = listState,
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth(),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
                reverseLayout = false
            ) {
                var lastDateStr = ""
                messages.forEach { msg ->
                    val dateStr = getMessageDateSeparator(msg.createdAt)
                    if (dateStr.isNotEmpty() && dateStr != lastDateStr) {
                        lastDateStr = dateStr
                        item(key = "date_sep_${msg.id}") {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 12.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Surface(
                                    color = Color(0xFFE2E8F0),
                                    shape = RoundedCornerShape(12.dp)
                                ) {
                                    Text(
                                        text = dateStr,
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color(0xFF64748B),
                                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                                    )
                                }
                            }
                        }
                    }

                    item(key = "msg_${msg.id}") {
                        val isOwnMessage = msg.senderId == currentUserId
                        MessageBubble(
                            message = msg,
                            isOwnMessage = isOwnMessage,
                            showSenderName = !isOwnMessage && teamId != null
                        )
                    }
                }
            }

            // Input Row
            Surface(
                modifier = Modifier.fillMaxWidth(),
                color = Color.White,
                tonalElevation = 2.dp
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    OutlinedTextField(
                        value = textInput,
                        onValueChange = { textInput = it },
                        placeholder = { Text("Type message...") },
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(24.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = DevLinkBlue,
                            unfocusedBorderColor = Color.LightGray
                        )
                    )

                    Spacer(modifier = Modifier.width(12.dp))

                    IconButton(
                        onClick = {
                            if (textInput.isNotBlank()) {
                                onSendMessage(textInput)
                                textInput = ""
                            }
                        },
                        colors = IconButtonDefaults.iconButtonColors(
                            containerColor = DevLinkBlue,
                            contentColor = Color.White
                        ),
                        modifier = Modifier.size(48.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Send,
                            contentDescription = "Send",
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun MessageBubble(
    message: MessageResponse,
    isOwnMessage: Boolean,
    showSenderName: Boolean
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(
                start = if (isOwnMessage) 56.dp else 0.dp,
                end = if (isOwnMessage) 0.dp else 56.dp
            ),
        horizontalAlignment = if (isOwnMessage) Alignment.End else Alignment.Start
    ) {
        if (showSenderName) {
            Text(
                text = "@${message.senderUsername}",
                fontSize = 11.sp,
                color = Color.Gray,
                modifier = Modifier.padding(start = 8.dp, bottom = 2.dp)
            )
        }

        Card(
            shape = RoundedCornerShape(
                topStart = 16.dp,
                topEnd = 16.dp,
                bottomStart = if (isOwnMessage) 16.dp else 4.dp,
                bottomEnd = if (isOwnMessage) 4.dp else 16.dp
            ),
            colors = CardDefaults.cardColors(
                containerColor = if (isOwnMessage) DevLinkBlue else Color.White
            ),
            elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
        ) {
            Column(modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp)) {
                Text(
                    text = message.content,
                    color = if (isOwnMessage) Color.White else Color(0xFF1E293B),
                    fontSize = 14.sp
                )
                Spacer(modifier = Modifier.height(2.dp))
                val timeStr = formatMessageTime(message.createdAt)
                if (timeStr.isNotEmpty()) {
                    Text(
                        text = timeStr,
                        color = if (isOwnMessage) Color.White.copy(alpha = 0.7f) else Color.Gray,
                        fontSize = 9.sp,
                        modifier = Modifier.align(Alignment.End)
                    )
                }
            }
        }
    }
}
