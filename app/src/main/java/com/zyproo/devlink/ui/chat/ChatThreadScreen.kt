package com.zyproo.devlink.ui.chat

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.Group
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil3.compose.AsyncImage
import com.zyproo.devlink.common.toFullMediaUrl
import com.zyproo.devlink.data.remote.dto.MessageResponse
import com.zyproo.devlink.ui.theme.*

// Chat palette: every shade here is chosen so text stays clearly readable on its background.
private val ChatBackground = Color(0xFFE9EFFA)
private val ReceivedBubble = Color.White
private val ReceivedBubbleBorder = Color(0xFFD5DEEE)
private val ReceivedText = Color(0xFF0F172A)
private val ReceivedTime = Color(0xFF64748B)
private val DateChipBackground = Color(0xFFCBD7EE)
private val DateChipText = Color(0xFF1E293B)
private val InputFieldBackground = Color(0xFFF1F5F9)
private val InputFieldBorder = Color(0xFFB6C3DA)

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
    onTeamSettingsClick: (Long) -> Unit,
    errorMessage: String? = null,
    onErrorShown: () -> Unit = {}
) {
    var textInput by remember { mutableStateOf("") }
    val listState = rememberLazyListState()
    val snackbarHostState = remember { SnackbarHostState() }

    // Auto-scroll to the latest message whenever messages list updates
    LaunchedEffect(messages.size) {
        if (messages.isNotEmpty()) {
            listState.animateScrollToItem(listState.layoutInfo.totalItemsCount.coerceAtLeast(1) - 1)
        }
    }

    // Surface send/load failures instead of failing silently
    LaunchedEffect(errorMessage) {
        if (!errorMessage.isNullOrBlank()) {
            snackbarHostState.showSnackbar(errorMessage)
            onErrorShown()
        }
    }

    val fullPhotoUrl = photoUrl.toFullMediaUrl()
    val canSend = textInput.isNotBlank()
    val sendNow = {
        if (textInput.isNotBlank()) {
            onSendMessage(textInput)
            textInput = ""
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            Surface(color = Color.White, shadowElevation = 3.dp) {
                TopAppBar(
                    title = {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(38.dp)
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
                            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
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
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .imePadding()
                .background(ChatBackground)
        ) {
            if (messages.isEmpty()) {
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth()
                        .padding(32.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = "No messages yet",
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp,
                            color = DateChipText
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Send the first message to start the conversation.",
                            fontSize = 13.sp,
                            color = ReceivedTime,
                            textAlign = TextAlign.Center
                        )
                    }
                }
            } else {
                LazyColumn(
                    state = listState,
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth(),
                    contentPadding = PaddingValues(horizontal = 14.dp, vertical = 12.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
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
                                        .padding(vertical = 10.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Surface(
                                        color = DateChipBackground,
                                        shape = RoundedCornerShape(12.dp)
                                    ) {
                                        Text(
                                            text = dateStr,
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = DateChipText,
                                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp)
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
            }

            // Input Row
            Surface(
                modifier = Modifier.fillMaxWidth(),
                color = Color.White,
                shadowElevation = 8.dp
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .navigationBarsPadding()
                        .padding(horizontal = 12.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    OutlinedTextField(
                        value = textInput,
                        onValueChange = { textInput = it },
                        placeholder = { Text("Type a message", color = ReceivedTime) },
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(24.dp),
                        maxLines = 4,
                        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Send),
                        keyboardActions = KeyboardActions(onSend = { sendNow() }),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedContainerColor = InputFieldBackground,
                            unfocusedContainerColor = InputFieldBackground,
                            focusedBorderColor = DevLinkBlue,
                            unfocusedBorderColor = InputFieldBorder,
                            focusedTextColor = ReceivedText,
                            unfocusedTextColor = ReceivedText
                        )
                    )

                    Spacer(modifier = Modifier.width(10.dp))

                    IconButton(
                        onClick = { sendNow() },
                        enabled = canSend,
                        colors = IconButtonDefaults.iconButtonColors(
                            containerColor = DevLinkBlue,
                            contentColor = Color.White,
                            disabledContainerColor = Color(0xFFB6C3DA),
                            disabledContentColor = Color.White
                        ),
                        modifier = Modifier.size(48.dp)
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.Send,
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
                fontWeight = FontWeight.SemiBold,
                color = ReceivedTime,
                modifier = Modifier.padding(start = 8.dp, bottom = 2.dp)
            )
        }

        Surface(
            shape = RoundedCornerShape(
                topStart = 16.dp,
                topEnd = 16.dp,
                bottomStart = if (isOwnMessage) 16.dp else 4.dp,
                bottomEnd = if (isOwnMessage) 4.dp else 16.dp
            ),
            color = if (isOwnMessage) DevLinkBlue else ReceivedBubble,
            border = if (isOwnMessage) null else BorderStroke(1.dp, ReceivedBubbleBorder),
            shadowElevation = 1.dp
        ) {
            Column(modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp)) {
                Text(
                    text = message.content,
                    color = if (isOwnMessage) Color.White else ReceivedText,
                    fontSize = 15.sp
                )
                Spacer(modifier = Modifier.height(2.dp))
                val timeStr = formatMessageTime(message.createdAt)
                if (timeStr.isNotEmpty()) {
                    Text(
                        text = timeStr,
                        color = if (isOwnMessage) Color.White.copy(alpha = 0.85f) else ReceivedTime,
                        fontSize = 10.sp,
                        modifier = Modifier.align(Alignment.End)
                    )
                }
            }
        }
    }
}
