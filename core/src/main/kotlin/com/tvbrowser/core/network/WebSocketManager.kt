package com.tvbrowser.core.network

import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.serialization.json.Json
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response
import okhttp3.WebSocket
import okhttp3.WebSocketListener
import timber.log.Timber
import com.tvbrowser.core.domain.models.TVCommand
import com.tvbrowser.core.domain.models.TVResponse
import java.util.concurrent.TimeUnit
import kotlinx.coroutines.runBlocking

class WebSocketManager(
    private val serverUrl: String,
    private val authToken: String? = null
) {
    private val client = OkHttpClient.Builder()
        .readTimeout(0, TimeUnit.MILLISECONDS)
        .connectTimeout(10, TimeUnit.SECONDS)
        .pingInterval(30, TimeUnit.SECONDS)
        .retryOnConnectionFailure(true)
        .build()

    private var webSocket: WebSocket? = null
    private val json = Json {
        ignoreUnknownKeys = true
        isLenient = true
    }

    private val _commandResponse = MutableSharedFlow<TVResponse>(replay = 1)
    val commandResponse = _commandResponse.asSharedFlow()

    private val _connectionState = MutableSharedFlow<ConnectionState>(replay = 1)
    val connectionState = _connectionState.asSharedFlow()

    fun connect(): Boolean {
        return try {
            val builder = Request.Builder().url(serverUrl)
            authToken?.let {
                builder.addHeader("Authorization", "Bearer $it")
            }
            val request = builder.build()

            webSocket = client.newWebSocket(request, createListener())
            true
        } catch (e: Exception) {
            Timber.e(e, "Failed to create WebSocket connection")
            false
        }
    }

    fun disconnect() {
        webSocket?.close(1000, "Client disconnecting")
        webSocket = null
    }

    suspend fun sendCommand(command: TVCommand): Boolean {
        return try {
            val json = Json.encodeToString(TVCommand.serializer(), command)
            webSocket?.send(json) ?: run {
                Timber.e("WebSocket not connected")
                false
            }
            true
        } catch (e: Exception) {
            Timber.e(e, "Failed to send command")
            false
        }
    }

    fun isConnected(): Boolean = webSocket != null

    private fun createListener() = object : WebSocketListener() {
        override fun onOpen(webSocket: WebSocket, response: Response) {
            Timber.d("WebSocket connected")
            runBlocking {
                _connectionState.emit(ConnectionState.CONNECTED)
            }
        }

        override fun onMessage(webSocket: WebSocket, text: String) {
            try {
                val response = json.decodeFromString(TVResponse.serializer(), text)
                runBlocking {
                    _commandResponse.emit(response)
                }
            } catch (e: Exception) {
                Timber.e(e, "Failed to parse response: $text")
            }
        }

        override fun onClosing(webSocket: WebSocket, code: Int, reason: String) {
            webSocket.close(1000, null)
            runBlocking {
                _connectionState.emit(ConnectionState.DISCONNECTED)
            }
            Timber.d("WebSocket closing: $reason")
        }

        override fun onFailure(webSocket: WebSocket, t: Throwable, response: Response?) {
            Timber.e(t, "WebSocket failure")
            runBlocking {
                _connectionState.emit(ConnectionState.ERROR)
            }
        }
    }
}

enum class ConnectionState {
    CONNECTED, DISCONNECTED, ERROR, RECONNECTING
}
