package com.tvbrowser.tv.server

import kotlinx.serialization.json.Json
import org.java_websocket.WebSocket
import org.java_websocket.handshake.ClientHandshake
import org.java_websocket.server.WebSocketServer
import timber.log.Timber
import com.tvbrowser.core.domain.models.TVCommand
import com.tvbrowser.core.domain.models.TVResponse
import java.net.InetSocketAddress
import java.util.concurrent.ConcurrentHashMap

class TVWebSocketServer(port: Int) : WebSocketServer(InetSocketAddress(port)) {
    private val connectedClients = ConcurrentHashMap<String, WebSocket>()
    private val allowedOrigins = setOf(
        "app://mobile-controller",
        "localhost",
        "127.0.0.1"
    )
    private val json = Json {
        ignoreUnknownKeys = true
        isLenient = true
    }

    var onCommandReceived: ((TVCommand) -> Unit)? = null
    var onClientConnected: ((String) -> Unit)? = null
    var onClientDisconnected: ((String) -> Unit)? = null

    override fun onOpen(conn: WebSocket, handshake: ClientHandshake) {
        try {
            val clientId = conn.remoteSocketAddress.toString()
            
            // Validate origin
            val origin = handshake.getFieldValue("Origin")
            if (origin != null && !validateOrigin(origin)) {
                conn.close(1008, "Unauthorized origin")
                return
            }

            // Validate auth token
            val authHeader = handshake.getFieldValue("Authorization")
            if (authHeader == null || !authHeader.startsWith("Bearer ")) {
                conn.close(1008, "Missing or invalid authentication")
                return
            }

            connectedClients[clientId] = conn
            onClientConnected?.invoke(clientId)
            Timber.d("Client connected: $clientId")

            // Send welcome message
            val response = TVResponse.Status(
                state = "ready",
                currentUrl = null,
                playbackPosition = null
            )
            conn.send(json.encodeToString(TVResponse.serializer(), response))
        } catch (e: Exception) {
            Timber.e(e, "Error in onOpen")
            conn.close(1011, "Server error")
        }
    }

    override fun onMessage(conn: WebSocket, message: String) {
        try {
            if (message.length > 65536) {
                conn.send(json.encodeToString(
                    TVResponse.serializer(),
                    TVResponse.Error("Message too large", 413)
                ))
                return
            }

            val command = json.decodeFromString(TVCommand.serializer(), message)
            onCommandReceived?.invoke(command)

            Timber.d("Received command: $command")
            conn.send(json.encodeToString(
                TVResponse.serializer(),
                TVResponse.Success
            ))
        } catch (e: Exception) {
            Timber.e(e, "Failed to parse message: $message")
            conn.send(json.encodeToString(
                TVResponse.serializer(),
                TVResponse.Error("Invalid command format", 400)
            ))
        }
    }

    override fun onClose(conn: WebSocket, code: Int, reason: String, remote: Boolean) {
        val clientId = conn.remoteSocketAddress.toString()
        connectedClients.remove(clientId)
        onClientDisconnected?.invoke(clientId)
        Timber.d("Client disconnected: $clientId - $reason")
    }

    override fun onError(conn: WebSocket?, ex: Exception) {
        Timber.e(ex, "WebSocket error")
    }

    override fun onStart() {
        Timber.d("WebSocket server started on port: ${address.port}")
    }

    fun broadcast(response: TVResponse) {
        val json = json.encodeToString(TVResponse.serializer(), response)
        for ((_, conn) in connectedClients) {
            if (conn.isOpen) {
                conn.send(json)
            }
        }
    }

    private fun validateOrigin(origin: String): Boolean {
        return allowedOrigins.any { origin.contains(it) }
    }
}
