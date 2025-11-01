package com.tvbrowser.core.network

import com.tvbrowser.core.domain.models.TVCommand
import com.tvbrowser.core.domain.models.TVResponse
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import okhttp3.*
import timber.log.Timber
import java.util.concurrent.TimeUnit

class TVWebSocketClient(
    private val serverUrl: String
) {
    private var webSocket: WebSocket? = null
    private val client = OkHttpClient.Builder()
        .connectTimeout(10, TimeUnit.SECONDS)
        .readTimeout(10, TimeUnit.SECONDS)
        .writeTimeout(10, TimeUnit.SECONDS)
        .pingInterval(30, TimeUnit.SECONDS)
        .build()

    private val _connectionState = MutableStateFlow<ConnectionState>(ConnectionState.Disconnected)
    val connectionState: StateFlow<ConnectionState> = _connectionState.asStateFlow()

    private val _responses = MutableStateFlow<TVResponse?>(null)
    val responses: StateFlow<TVResponse?> = _responses.asStateFlow()

    private val webSocketListener = object : WebSocketListener() {
        override fun onOpen(webSocket: WebSocket, response: Response) {
            Timber.d("WebSocket connected")
            _connectionState.value = ConnectionState.Connected
        }

        override fun onMessage(webSocket: WebSocket, text: String) {
            Timber.d("Received message: $text")
            TVResponse.fromJson(text)?.let { response ->
                _responses.value = response
            }
        }

        override fun onClosing(webSocket: WebSocket, code: Int, reason: String) {
            Timber.d("WebSocket closing: $code - $reason")
            _connectionState.value = ConnectionState.Disconnecting
        }

        override fun onClosed(webSocket: WebSocket, code: Int, reason: String) {
            Timber.d("WebSocket closed: $code - $reason")
            _connectionState.value = ConnectionState.Disconnected
        }

        override fun onFailure(webSocket: WebSocket, t: Throwable, response: Response?) {
            Timber.e(t, "WebSocket failure")
            _connectionState.value = ConnectionState.Error(t.message ?: "Unknown error")
        }
    }

    fun connect() {
        if (_connectionState.value is ConnectionState.Connected) {
            Timber.w("Already connected")
            return
        }

        _connectionState.value = ConnectionState.Connecting
        val request = Request.Builder()
            .url(serverUrl)
            .build()

        webSocket = client.newWebSocket(request, webSocketListener)
    }

    fun sendCommand(command: TVCommand): Boolean {
        return try {
            val json = TVCommand.toJson(command)
            webSocket?.send(json) ?: false
        } catch (e: Exception) {
            Timber.e(e, "Failed to send command")
            false
        }
    }

    fun disconnect() {
        webSocket?.close(1000, "Client disconnecting")
        webSocket = null
    }

    sealed class ConnectionState {
        object Disconnected : ConnectionState()
        object Connecting : ConnectionState()
        object Connected : ConnectionState()
        object Disconnecting : ConnectionState()
        data class Error(val message: String) : ConnectionState()
    }
}
