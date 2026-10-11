package com.zyproo.devlink.ui.chat

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.zyproo.devlink.data.remote.WebSocketManager
import com.zyproo.devlink.data.remote.dto.ConversationResponse
import com.zyproo.devlink.data.remote.dto.MessageResponse
import com.zyproo.devlink.data.repository.ChatRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ChatViewModel @Inject constructor(
    private val chatRepository: ChatRepository,
    private val webSocketManager: WebSocketManager
) : ViewModel() {

    private val _conversations = MutableStateFlow<List<ConversationResponse>>(emptyList())
    val conversations: StateFlow<List<ConversationResponse>> = _conversations.asStateFlow()

    private val _activeMessages = MutableStateFlow<List<MessageResponse>>(emptyList())
    val activeMessages: StateFlow<List<MessageResponse>> = _activeMessages.asStateFlow()

    private val _isLoadingInbox = MutableStateFlow(false)
    val isLoadingInbox: StateFlow<Boolean> = _isLoadingInbox.asStateFlow()

    private val _isLoadingThread = MutableStateFlow(false)
    val isLoadingThread: StateFlow<Boolean> = _isLoadingThread.asStateFlow()

    private val _inboxError = MutableStateFlow<String?>(null)
    val inboxError: StateFlow<String?> = _inboxError.asStateFlow()

    private val _threadError = MutableStateFlow<String?>(null)
    val threadError: StateFlow<String?> = _threadError.asStateFlow()

    var activeConversationId: Long? = null

    init {
        viewModelScope.launch {
            webSocketManager.incomingMessages.collect { msg ->
                if (msg.conversationId == activeConversationId) {
                    _activeMessages.value = _activeMessages.value + msg
                }
                loadInbox()
            }
        }
    }

    fun loadInbox() {
        viewModelScope.launch {
            _isLoadingInbox.value = true
            _inboxError.value = null
            chatRepository.getInbox()
                .onSuccess {
                    _conversations.value = it
                    _isLoadingInbox.value = false
                }
                .onFailure {
                    _inboxError.value = it.message ?: "Failed to load conversations"
                    _isLoadingInbox.value = false
                }
        }
    }

    fun openConversation(conversationId: Long) {
        activeConversationId = conversationId
        loadMessages(conversationId)
    }

    fun closeConversation() {
        activeConversationId = null
        _activeMessages.value = emptyList()
    }

    fun loadMessages(conversationId: Long) {
        viewModelScope.launch {
            _isLoadingThread.value = true
            _threadError.value = null
            chatRepository.getMessages(conversationId)
                .onSuccess {
                    _activeMessages.value = it
                    _isLoadingThread.value = false
                }
                .onFailure {
                    _threadError.value = it.message ?: "Failed to load messages"
                    _isLoadingThread.value = false
                }
        }
    }

    fun sendMessage(content: String) {
        val convId = activeConversationId ?: return
        if (content.isBlank()) return
        viewModelScope.launch {
            webSocketManager.sendMessage(convId, content)
            chatRepository.sendMessage(convId, content)
                .onSuccess { sentMsg ->
                    if (!_activeMessages.value.any { it.id == sentMsg.id }) {
                        _activeMessages.value = _activeMessages.value + sentMsg
                    }
                    loadInbox()
                }
                .onFailure {
                    _threadError.value = it.message ?: "Failed to send message"
                }
        }
    }

    fun startDirectChat(targetUsername: String, onComplete: (Long) -> Unit) {
        viewModelScope.launch {
            chatRepository.startDirectChat(targetUsername)
                .onSuccess {
                    loadInbox()
                    onComplete(it.id)
                }
                .onFailure {
                    _inboxError.value = it.message ?: "Failed to start direct chat"
                }
        }
    }
}
