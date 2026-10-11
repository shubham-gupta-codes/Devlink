package com.zyproo.devlink.data.repository

import com.zyproo.devlink.data.remote.ChatApi
import com.zyproo.devlink.data.remote.dto.*
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ChatRepositoryImpl @Inject constructor(
    private val chatApi: ChatApi
) : ChatRepository {

    override suspend fun getInbox(): Result<List<ConversationResponse>> = runCatching {
        val res = chatApi.getInbox()
        if (res.isSuccessful) res.body() ?: emptyList()
        else throw Exception("Failed to fetch inbox: ${res.message()}")
    }

    override suspend fun getMessages(conversationId: Long): Result<List<MessageResponse>> = runCatching {
        val res = chatApi.getMessages(conversationId)
        if (res.isSuccessful) res.body() ?: emptyList()
        else throw Exception("Failed to fetch messages: ${res.message()}")
    }

    override suspend fun startDirectChat(targetUsername: String): Result<ConversationResponse> = runCatching {
        val res = chatApi.startDirectChat(targetUsername)
        if (res.isSuccessful) res.body() ?: throw Exception("Body is null")
        else throw Exception("Failed to start direct chat: ${res.message()}")
    }

    override suspend fun sendMessage(conversationId: Long, content: String): Result<MessageResponse> = runCatching {
        val res = chatApi.sendMessage(conversationId, SendMessageRequest(content))
        if (res.isSuccessful) res.body() ?: throw Exception("Body is null")
        else throw Exception("Failed to send message: ${res.message()}")
    }
}
