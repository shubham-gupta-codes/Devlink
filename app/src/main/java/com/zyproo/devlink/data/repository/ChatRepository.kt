package com.zyproo.devlink.data.repository

import com.zyproo.devlink.data.remote.dto.*

interface ChatRepository {
    suspend fun getInbox(): Result<List<ConversationResponse>>
    suspend fun getMessages(conversationId: Long): Result<List<MessageResponse>>
    suspend fun startDirectChat(targetUsername: String): Result<ConversationResponse>
    suspend fun sendMessage(conversationId: Long, content: String): Result<MessageResponse>
}
