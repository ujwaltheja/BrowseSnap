package com.tvbrowser.tv.viewmodel

import android.app.Application
import android.webkit.WebView
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import androidx.media3.common.MediaItem
import androidx.media3.common.Player
import androidx.media3.exoplayer.ExoPlayer
import com.tvbrowser.core.domain.models.TVCommand
import com.tvbrowser.core.network.TVWebSocketServerImpl
import com.tvbrowser.core.util.NetworkUtils
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import timber.log.Timber

    val currentView: StateFlow<TVView> = _currentView.asStateFlow()

    private val _currentUrl = MutableStateFlow<String?>(null)
    val currentUrl: StateFlow<String?> = _currentUrl.asStateFlow()

    private val _isPlaying = MutableStateFlow(false)
    val isPlaying: StateFlow<Boolean> = _isPlaying.asStateFlow()

    private val _pin = MutableStateFlow(NetworkUtils.generatePin())
    val pin: StateFlow<String> = _pin.asStateFlow()

    private val _ipAddress = MutableStateFlow<String?>(null)
    val ipAddress: StateFlow<String?> = _ipAddress.asStateFlow()

    private val _connectionCount = MutableStateFlow(0)
    val connectionCount: StateFlow<Int> = _connectionCount.asStateFlow()

    private var exoPlayer: ExoPlayer? = null
    private var webView: WebView? = null

    init {
        _ipAddress.value = NetworkUtils.getLocalIpAddress()

        // Monitor server state
        viewModelScope.launch {
            webSocketServer.serverState.collect { state ->
                when (state) {
                    is TVWebSocketServerImpl.ServerState.Running -> {
                        _serverState.value = ServerState.Running(state.port)
                        Timber.d("Server running on port: ${state.port}")
                    }
                    is TVWebSocketServerImpl.ServerState.Error -> {
                        _serverState.value = ServerState.Error(state.message)
                        Timber.e("Server error: ${state.message}")
                    }
                    is TVWebSocketServerImpl.ServerState.Stopped -> {
                        _serverState.value = ServerState.Stopped
                        Timber.d("Server stopped")
                    }
                }
            }
        }

        // Monitor connection count
        viewModelScope.launch {
            webSocketServer.connectionCount.collect { count ->
                _connectionCount.value = count
                if (count > 0 && _currentView.value == TVView.Pairing) {
                    _currentView.value = TVView.Browser
                }
            }
        }

        // Handle incoming commands
        viewModelScope.launch {
            webSocketServer.commands.collect { command ->
                handleCommand(command)
            }
        }
    }

    fun startServer() {
        viewModelScope.launch {
            try {
                webSocketServer.start()
                Timber.d("WebSocket server started")
            } catch (e: Exception) {
                Timber.e(e, "Failed to start server")
                _serverState.value = ServerState.Error(e.message ?: "Unknown error")
            }
        }
    }

    fun stopServer() {
        webSocketServer.stopServer()
        exoPlayer?.release()
        exoPlayer = null
    }

    private fun handleCommand(command: TVCommand) {
        Timber.d("Handling command: ${command::class.simpleName}")

        when (command) {
            is TVCommand.OpenUrl -> {
                openUrl(command.url)
            }
            is TVCommand.PlayVideo -> {
                playVideo(command.videoUrl)
            }
            is TVCommand.NavigateBack -> {
                webView?.goBack()
            }
            is TVCommand.NavigateForward -> {
                webView?.goForward()
            }
            is TVCommand.Pause -> {
                exoPlayer?.pause()
                _isPlaying.value = false
            }
            is TVCommand.Resume -> {
                exoPlayer?.play()
                _isPlaying.value = true
            }
            is TVCommand.Stop -> {
                exoPlayer?.stop()
                _isPlaying.value = false
                _currentView.value = TVView.Browser
            }
            is TVCommand.SetVolume -> {
                exoPlayer?.volume = command.volume
            }
            is TVCommand.Seek -> {
                exoPlayer?.seekTo(command.positionMs)
            }
            is TVCommand.Register -> {
                if (command.pin == _pin.value) {
                    Timber.d("Device registered: ${command.deviceName}")
                }
            }
            is TVCommand.Ping -> {
                // Respond with pong
            }
        }
    }

    private fun openUrl(url: String) {
        _currentUrl.value = url
        _currentView.value = TVView.Browser
        Timber.d("Opening URL: $url")
    }

    private fun playVideo(videoUrl: String) {
        _currentUrl.value = videoUrl
        _currentView.value = TVView.VideoPlayer
        _isPlaying.value = true
        Timber.d("Playing video: $videoUrl")
    }

    fun initializePlayer(player: ExoPlayer) {
        exoPlayer = player

        player.addListener(object : Player.Listener {
            override fun onIsPlayingChanged(isPlaying: Boolean) {
                _isPlaying.value = isPlaying
            }

            override fun onPlaybackStateChanged(playbackState: Int) {
                when (playbackState) {
                    Player.STATE_ENDED -> {
                        _isPlaying.value = false
                    }
                    Player.STATE_READY -> {
                        Timber.d("Player ready")
                    }
                    Player.STATE_BUFFERING -> {
                        Timber.d("Player buffering")
                    }
                }
            }
        })
    }

    fun initializeWebView(webView: WebView) {
        this.webView = webView
    }

    fun playMedia(url: String) {
        exoPlayer?.let { player ->
            val mediaItem = MediaItem.fromUri(url)
            player.setMediaItem(mediaItem)
            player.prepare()
            player.play()
        }
    }

    fun getQRCodeContent(): String {
        val ip = _ipAddress.value ?: "unknown"
        val pin = _pin.value
        return "browsesnap://pair?ip=$ip&pin=$pin&name=Android TV"
    }

    sealed class ServerState {
        object Stopped : ServerState()
        data class Running(val port: Int) : ServerState()
        data class Error(val message: String) : ServerState()
    }

    sealed class TVView {
        object Pairing : TVView()
        object Browser : TVView()
        object VideoPlayer : TVView()
    }
}
