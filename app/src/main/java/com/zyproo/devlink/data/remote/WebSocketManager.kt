package com.zyproo.devlink.data.remote

import android.util.Log
import com.google.gson.Gson
import com.zyproo.devlink.BuildConfig
import com.zyproo.devlink.data.local.SessionManager
import com.zyproo.devlink.data.remote.dto.MessageResponse
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*
import okhttp3.*
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class WebSocketManager @Inject constructor(
    private val sessionManager: SessionManager,
    private val gson: Gson
) {
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    private var webSocket: WebSocket? = null
    private val okHttpClient = OkHttpClient()

    private val _incomingMessages = MutableSharedFlow<MessageResponse>(extraBufferCapacity = 64)
    val incomingMessages: SharedFlow<MessageResponse> = _incomingMessages.asSharedFlow()

    private val _connectionState = MutableStateFlow(false)
    val connectionState: StateFlow<Boolean> = _connectionState.asStateFlow()

    private var currentToken: String? = null

    init {
        scope.launch {
            sessionManager.accessTokenFlow.collect { token ->
                currentToken = token
                if (token != null) {
                    connect(token)
                } else {
                    disconnect()
                }
            }
        }
    }

    @Synchronized
    private fun connect(token: String) {
        disconnect()

        val customHost = runBlocking { sessionManager.serverHostFlow.first() }
        val rawBase = if (!customHost.isNullOrBlank()) {
            if (!customHost.startsWith("http://") && !customHost.startsWith("https://")) "http://$customHost" else customHost
        } else {
            BuildConfig.API_BASE_URL
        }

        val wsBase = rawBase
            .replace("http://", "ws://")
            .replace("https://", "wss://")
        
        val baseUrl = if (wsBase.endsWith("/api")) wsBase.removeSuffix("/api") else wsBase
        val wsUrl = "$baseUrl/ws/chat?token=$token"

        Log.d("WebSocketManager", "Connecting to WebSocket: $wsUrl")

        val request = Request.Builder().url(wsUrl).build()
        webSocket = okHttpClient.newWebSocket(request, object : WebSocketListener() {
            override fun onOpen(webSocket: WebSocket, response: Response) {
                Log.d("WebSocketManager", "WebSocket Connected successfully")
                _connectionState.value = true
            }

            override fun onMessage(webSocket: WebSocket, text: String) {
                Log.d("WebSocketManager", "Received WebSocket message: $text")
                try {
                    val msg = gson.fromJson(text, MessageResponse::class.java)
                    scope.launch {
                        _incomingMessages.emit(msg)
                    }
                } catch (e: Exception) {
                    Log.e("WebSocketManager", "Error parsing incoming msg", e)
                }
            }

            override fun onClosing(webSocket: WebSocket, code: Int, reason: String) {
                Log.d("WebSocketManager", "WebSocket Closing: $code / $reason")
                _connectionState.value = false
            }

            override fun onClosed(webSocket: WebSocket, code: Int, reason: String) {
                Log.d("WebSocketManager", "WebSocket Closed: $code / $reason")
                _connectionState.value = false
            }

            override fun onFailure(webSocket: WebSocket, t: Throwable, response: Response?) {
                Log.e("WebSocketManager", "WebSocket Failure: ${t.message}", t)
                _connectionState.value = false
                
                scope.launch {
                    delay(5000)
                    val activeToken = currentToken
                    if (activeToken != null && !_connectionState.value) {
                        connect(activeToken)
                    }
                }
            }
        })
    }

    @Synchronized
    fun disconnect() {
        webSocket?.close(1000, "Disconnect requested")
        webSocket = null
        _connectionState.value = false
    }

    fun sendMessage(conversationId: Long, content: String) {
        val ws = webSocket
        if (ws != null && _connectionState.value) {
            val payload = mapOf(
                "conversationId" to conversationId,
                "content" to content
            )
            val json = gson.toJson(payload)
            Log.d("WebSocketManager", "Sending WebSocket message: $json")
            ws.send(json)
        } else {
            Log.e("WebSocketManager", "Cannot send message, WebSocket is not connected")
        }
    }
}
