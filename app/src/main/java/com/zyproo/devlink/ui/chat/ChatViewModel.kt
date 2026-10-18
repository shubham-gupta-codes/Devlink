package com.zyproo.devlink.ui.chat

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.zyproo.devlink.data.local.SessionManager
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
    private val webSocketManager: WebSocketManager,
    private val sessionManager: SessionManager
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

    /** Id of the logged-in user, read from the session so it works for every role. */
    private val _currentUserId = MutableStateFlow(0L)
    val currentUserId: StateFlow<Long> = _currentUserId.asStateFlow()

    var activeConversationId: Long? = null

    init {
        viewModelScope.launch {
            sessionManager.userIdFlow.collect { id -> _currentUserId.value = id ?: 0L }
        }
        viewModelScope.launch {
            webSocketManager.incomingMessages.collect { msg ->
                if (msg.conversationId == activeConversationId) {
                    appendIfNew(msg)
                }
                loadInbox()
            }
        }
    }

    /** Adds a message only if one with the same id is not already shown (REST reply and socket push can both arrive). */
    private fun appendIfNew(msg: MessageResponse) {
        if (_activeMessages.value.none { it.id == msg.id }) {
            _activeMessages.value = _activeMessages.value + msg
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

    fun clearThreadError() {
        _threadError.value = null
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

    /**
     * Sends through REST only. The server saves the message once and pushes it to every participant's live
     * socket, so there is a single saved copy and the other person receives it in real time.
     */
    fun sendMessage(content: String) {
        val convId = activeConversationId ?: return
        val text = content.trim()
        if (text.isEmpty()) return
        viewModelScope.launch {
            chatRepository.sendMessage(convId, text)
                .onSuccess { sentMsg ->
                    if (activeConversationId == convId) appendIfNew(sentMsg)
                    loadInbox()
                }
                .onFailure {
                    _threadError.value = it.message ?: "Message could not be sent. Check your connection and try again."
                }
        }
    }

    /**
     * Opens (or creates) the direct chat and marks it as the active conversation inside the ViewModel,
     * so messages typed afterwards are sent to the right conversation.
     */
    fun startDirectChat(targetUsername: String, onComplete: (Long) -> Unit) {
        viewModelScope.launch {
            chatRepository.startDirectChat(targetUsername)
                .onSuccess {
                    loadInbox()
                    openConversation(it.id)
                    onComplete(it.id)
                }
                .onFailure {
                    _inboxError.value = it.message ?: "Failed to start direct chat"
                }
        }
    }
}
