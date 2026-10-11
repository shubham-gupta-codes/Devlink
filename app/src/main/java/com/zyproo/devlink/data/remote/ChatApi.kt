package com.zyproo.devlink.data.remote

import com.zyproo.devlink.data.remote.dto.*
import retrofit2.Response
import retrofit2.http.*

interface ChatApi {
    @GET("api/chat/inbox")
    suspend fun getInbox(): Response<List<ConversationResponse>>

    @GET("api/chat/conversations/{conversationId}/messages")
    suspend fun getMessages(@Path("conversationId") conversationId: Long): Response<List<MessageResponse>>

    @POST("api/chat/conversations/direct")
    suspend fun startDirectChat(@Query("targetUsername") targetUsername: String): Response<ConversationResponse>

    @POST("api/chat/conversations/{conversationId}/messages")
    suspend fun sendMessage(
        @Path("conversationId") conversationId: Long,
        @Body request: SendMessageRequest
    ): Response<MessageResponse>
}
