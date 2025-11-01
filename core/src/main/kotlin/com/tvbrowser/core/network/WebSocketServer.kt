package com.tvbrowser.core.network

import com.tvbrowser.core.domain.models.TVCommand
import com.tvbrowser.core.domain.models.TVResponse
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import org.java_websocket.WebSocket
import org.java_websocket.handshake.ClientHandshake
import org.java_websocket.server.WebSocketServer
import timber.log.Timber
import java.net.InetSocketAddress

class TVWebSocketServerImpl(
    port: Int = 8888
) : WebSocketServer(InetSocketAddress(port)) {

    private val _commands = MutableSharedFlow<TVCommand>(replay = 0)
    val commands: SharedFlow<TVCommand> = _commands.asSharedFlow()

    private val _connectionCount = MutableStateFlow(0)
    val connectionCount: StateFlow<Int> = _connectionCount.asStateFlow()

    private val _serverState = MutableStateFlow<ServerState>(ServerState.Stopped)
    val serverState: StateFlow<ServerState> = _serverState.asStateFlow()

    init {
        isReuseAddr = true
        connectionLostTimeout = 30
    }

    override fun onStart() {
        Timber.d("WebSocket server started on port: $port")
        _serverState.value = ServerState.Running(port)
    }

    override fun onOpen(conn: WebSocket, handshake: ClientHandshake) {
        Timber.d("New connection from: ${conn.remoteSocketAddress}")
        _connectionCount.value = connections.size

        // Send welcome message
        sendResponse(
            conn,
            TVResponse.StatusUpdate(
                status = "connected",
                details = mapOf("message" to "Connected to TV")
            )
        )
    }

    override fun onMessage(conn: WebSocket, message: String) {
        Timber.d("Received message: $message")

        TVCommand.fromJson(message)?.let { command ->
            kotlinx.coroutines.GlobalScope.launch {
                _commands.emit(command)
            }

            // Send acknowledgment
            sendResponse(
                conn,
                TVResponse.CommandAck(
                    commandType = command::class.simpleName ?: "Unknown",
                    success = true,
                    message = "Command received"
                )
            )
        } ?: run {
            sendResponse(
                conn,
                TVResponse.Error(
                    errorCode = "INVALID_COMMAND",
                    errorMessage = "Failed to parse command"
                )
            )
        }
    }

    override fun onClose(conn: WebSocket, code: Int, reason: String, remote: Boolean) {
        Timber.d("Connection closed: $reason")
        _connectionCount.value = connections.size
    }

    override fun onError(conn: WebSocket?, ex: Exception) {
        Timber.e(ex, "WebSocket server error")
        _serverState.value = ServerState.Error(ex.message ?: "Unknown error")
    }

    fun sendResponse(conn: WebSocket, response: TVResponse) {
        try {
            val json = TVResponse.toJson(response)
            conn.send(json)
        } catch (e: Exception) {
            Timber.e(e, "Failed to send response")
        }
    }

    fun broadcast(response: TVResponse) {
        try {
            val json = TVResponse.toJson(response)
            broadcast(json)
        } catch (e: Exception) {
            Timber.e(e, "Failed to broadcast response")
        }
    }

    fun stopServer() {
        try {
            stop(1000)
            _serverState.value = ServerState.Stopped
        } catch (e: Exception) {
            Timber.e(e, "Failed to stop server")
        }
    }

    sealed class ServerState {
        object Stopped : ServerState()
        data class Running(val port: Int) : ServerState()
        data class Error(val message: String) : ServerState()
    }
}
