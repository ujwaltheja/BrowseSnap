# BrowseSnap - Technical Deep Dive & Code Patterns

## Quick Reference: Architecture Pattern

```
┌─────────────────────┐
│   Mobile App        │
│  (Jetpack Compose)  │
│                     │
│  • Search Bar       │
│  • History List     │
│  • Send to TV BTN   │
└──────────┬──────────┘
           │
           │ WebSocket (JSON Commands)
           │ ws://TV_IP:8765
           │
┌──────────▼──────────┐
│   TV App            │
│  (WebSocket Server) │
│                     │
│  • WebView/Player   │
│  • Command Handler  │
│  • UI Controls      │
└─────────────────────┘
```

---

## 1. Command Schema & Serialization

### Standardized Command Format
```json
{
  "action": "play_video",
  "url": "https://youtube.com/watch?v=abc123",
  "timestamp": 1698765432000,
  "device_id": "mobile_device_1",
  "metadata": {
    "title": "Sample Video",
    "thumbnail": "https://example.com/thumb.jpg"
  }
}
```

### Supported Actions
```kotlin
enum class TVAction(val actionName: String) {
    PLAY_VIDEO("play_video"),
    OPEN_URL("open_url"),
    NAVIGATE_BACK("navigate_back"),
    NAVIGATE_FORWARD("navigate_forward"),
    PAUSE("pause"),
    RESUME("resume"),
    STOP("stop"),
    ADJUST_VOLUME("adjust_volume"),
    SET_SUBTITLES("set_subtitles"),
    FULL_SCREEN("full_screen"),
    EXIT_FULL_SCREEN("exit_full_screen"),
    SEND_TEXT("send_text")
}

data class Command(
    val action: String,
    val url: String,
    val timestamp: Long = System.currentTimeMillis(),
    val deviceId: String = "",
    val metadata: Map<String, String> = emptyMap()
)

// Serialization helper
class CommandSerializer {
    fun serialize(command: Command): String = Gson().toJson(command)
    
    fun deserialize(json: String): Command = 
        Gson().fromJson(json, Command::class.java)
    
    fun validate(command: Command): Boolean {
        return command.action.isNotEmpty() && 
               command.url.isNotEmpty()
    }
}
```

---

## 2. Complete WebSocket Implementation

### Mobile Client (Full Implementation)
```kotlin
interface WebSocketListener {
    fun onConnected()
    fun onDisconnected()
    fun onError(exception: Exception)
    fun onMessageReceived(message: String)
}

class TVCommandClientImpl(
    private val baseUrl: String,
    private val listener: WebSocketListener
) {
    private var webSocket: WebSocket? = null
    private val client = OkHttpClient.Builder()
        .connectTimeout(15, TimeUnit.SECONDS)
        .readTimeout(15, TimeUnit.SECONDS)
        .pingInterval(20, TimeUnit.SECONDS)
        .build()
    
    private val serializer = CommandSerializer()
    
    fun connect() {
        val request = Request.Builder()
            .url(baseUrl)
            .build()
        
        webSocket = client.newWebSocket(request, object : WebSocketListener() {
            override fun onOpen(webSocket: WebSocket, response: Response) {
                Log.d(TAG, "Connected to TV")
                listener.onConnected()
            }
            
            override fun onMessage(webSocket: WebSocket, text: String) {
                Log.d(TAG, "Received: $text")
                listener.onMessageReceived(text)
            }
            
            override fun onClosing(webSocket: WebSocket, code: Int, reason: String) {
                Log.d(TAG, "Connection closing: $reason")
                listener.onDisconnected()
            }
            
            override fun onFailure(webSocket: WebSocket, t: Throwable, response: Response?) {
                Log.e(TAG, "Connection failed", t)
                listener.onError(Exception(t))
            }
        })
    }
    
    fun sendCommand(command: Command) {
        try {
            val json = serializer.serialize(command)
            if (serializer.validate(command)) {
                webSocket?.send(json)
                Log.d(TAG, "Command sent: ${command.action}")
            } else {
                listener.onError(IllegalArgumentException("Invalid command"))
            }
        } catch (e: Exception) {
            listener.onError(e)
        }
    }
    
    fun disconnect() {
        webSocket?.close(1000, "Client closed")
        webSocket = null
    }
    
    companion object {
        private const val TAG = "TVCommandClient"
    }
}
```

### TV Server (Full Implementation)
```kotlin
class TVWebSocketServerImpl(
    private val port: Int = 8765,
    private val commandHandler: CommandHandler
) {
    private var server: WebSocketServer? = null
    private val connections = Collections.synchronizedSet(mutableSetOf<WebSocket>())
    private val serializer = CommandSerializer()
    
    fun start() {
        try {
            server = object : WebSocketServer(InetSocketAddress(port)) {
                override fun onStart() {
                    Log.d(TAG, "Server started on port $port")
                }
                
                override fun onOpen(conn: WebSocket, handshake: ClientHandshake) {
                    connections.add(conn)
                    Log.d(TAG, "Client connected: ${conn.remoteSocketAddress}")
                    sendAck(conn, "connected")
                }
                
                override fun onMessage(conn: WebSocket, message: String) {
                    try {
                        val command = serializer.deserialize(message)
                        if (serializer.validate(command)) {
                            commandHandler.execute(command)
                            sendAck(conn, "executed")
                        } else {
                            sendError(conn, "Invalid command format")
                        }
                    } catch (e: Exception) {
                        Log.e(TAG, "Error processing message", e)
                        sendError(conn, e.message ?: "Unknown error")
                    }
                }
                
                override fun onClose(conn: WebSocket, code: Int, reason: String, remote: Boolean) {
                    connections.remove(conn)
                    Log.d(TAG, "Client disconnected: ${conn.remoteSocketAddress}")
                }
                
                override fun onError(conn: WebSocket, ex: Exception) {
                    Log.e(TAG, "Error", ex)
                }
                
                private fun sendAck(conn: WebSocket, status: String) {
                    val response = mapOf("status" to status, "timestamp" to System.currentTimeMillis())
                    conn.send(Gson().toJson(response))
                }
                
                private fun sendError(conn: WebSocket, message: String) {
                    val error = mapOf("status" to "error", "message" to message)
                    conn.send(Gson().toJson(error))
                }
            }
            server?.start()
        } catch (e: Exception) {
            Log.e(TAG, "Failed to start server", e)
        }
    }
    
    fun stop() {
        try {
            server?.stop()
        } catch (e: Exception) {
            Log.e(TAG, "Error stopping server", e)
        }
    }
    
    fun broadcast(message: String) {
        for (conn in connections) {
            conn.send(message)
        }
    }
    
    companion object {
        private const val TAG = "TVWebSocketServer"
    }
}
```

---

## 3. Command Handler Implementation

```kotlin
interface CommandExecutor {
    fun execute(command: Command): Result<Unit>
}

class CommandHandler(
    private val webViewExecutor: WebViewExecutor,
    private val videoPlayerExecutor: VideoPlayerExecutor,
    private val navigationExecutor: NavigationExecutor
) {
    fun execute(command: Command): Result<Unit> = runCatching {
        when (command.action) {
            "open_url" -> webViewExecutor.openUrl(command.url)
            "play_video" -> videoPlayerExecutor.playVideo(command.url)
            "navigate_back" -> navigationExecutor.back()
            "navigate_forward" -> navigationExecutor.forward()
            "pause" -> videoPlayerExecutor.pause()
            "resume" -> videoPlayerExecutor.resume()
            "stop" -> videoPlayerExecutor.stop()
            "adjust_volume" -> {
                val volume = command.metadata["volume"]?.toIntOrNull() ?: 50
                navigationExecutor.setVolume(volume)
            }
            "full_screen" -> navigationExecutor.toggleFullScreen(true)
            "exit_full_screen" -> navigationExecutor.toggleFullScreen(false)
            else -> throw IllegalArgumentException("Unknown action: ${command.action}")
        }
    }
}
```

---

## 4. ViewModel Pattern for Mobile App

```kotlin
class MobileViewModel(
    private val repository: TVCommandRepository,
    private val historyRepository: HistoryRepository
) : ViewModel() {
    
    private val _connectionState = MutableLiveData<ConnectionState>(ConnectionState.Disconnected)
    val connectionState: LiveData<ConnectionState> = _connectionState
    
    private val _searchResults = MutableLiveData<List<SearchResult>>()
    val searchResults: LiveData<List<SearchResult>> = _searchResults
    
    private val _history = MutableLiveData<List<HistoryItem>>()
    val history: LiveData<List<HistoryItem>> = _history
    
    private val _error = MutableLiveData<String?>()
    val error: LiveData<String?> = _error
    
    fun connectToTV(ipAddress: String, port: Int) {
        viewModelScope.launch {
            try {
                repository.connectToTV("ws://$ipAddress:$port")
                _connectionState.postValue(ConnectionState.Connected)
            } catch (e: Exception) {
                _connectionState.postValue(ConnectionState.Error(e.message ?: "Unknown error"))
                _error.postValue(e.message)
            }
        }
    }
    
    fun sendCommandToTV(action: String, url: String, metadata: Map<String, String> = emptyMap()) {
        viewModelScope.launch {
            try {
                val command = Command(
                    action = action,
                    url = url,
                    metadata = metadata
                )
                repository.sendCommand(command)
                historyRepository.addToHistory(url, action)
                loadHistory()
            } catch (e: Exception) {
                _error.postValue(e.message)
            }
        }
    }
    
    fun search(query: String) {
        viewModelScope.launch {
            // Implement search logic
            // Could be local search or web search API integration
        }
    }
    
    fun loadHistory() {
        viewModelScope.launch {
            _history.postValue(historyRepository.getHistory())
        }
    }
    
    fun disconnect() {
        viewModelScope.launch {
            repository.disconnect()
            _connectionState.postValue(ConnectionState.Disconnected)
        }
    }
    
    sealed class ConnectionState {
        object Connected : ConnectionState()
        object Disconnected : ConnectionState()
        data class Error(val message: String) : ConnectionState()
    }
}
```

---

## 5. Database Schema (Room)

```kotlin
@Entity(tableName = "search_history")
data class HistoryEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val url: String,
    val title: String = "",
    val action: String, // "play_video" or "open_url"
    val timestamp: Long = System.currentTimeMillis(),
    val thumbnailUrl: String? = null
)

@Dao
interface HistoryDao {
    @Insert
    suspend fun insert(history: HistoryEntity)
    
    @Query("SELECT * FROM search_history ORDER BY timestamp DESC LIMIT :limit")
    suspend fun getRecent(limit: Int = 50): List<HistoryEntity>
    
    @Query("SELECT * FROM search_history WHERE title LIKE :query OR url LIKE :query ORDER BY timestamp DESC")
    suspend fun search(query: String): List<HistoryEntity>
    
    @Query("DELETE FROM search_history WHERE timestamp < :olderThan")
    suspend fun deleteOlderThan(olderThan: Long)
    
    @Query("SELECT COUNT(*) FROM search_history")
    suspend fun getCount(): Int
}

@Database(entities = [HistoryEntity::class], version = 1)
abstract class BrowseSnapDatabase : RoomDatabase() {
    abstract fun historyDao(): HistoryDao
    
    companion object {
        @Volatile
        private var INSTANCE: BrowseSnapDatabase? = null
        
        fun getInstance(context: Context): BrowseSnapDatabase =
            INSTANCE ?: synchronized(this) {
                INSTANCE ?: Room.databaseBuilder(
                    context.applicationContext,
                    BrowseSnapDatabase::class.java,
                    "browsesnap.db"
                ).build().also { INSTANCE = it }
            }
    }
}
```

---

## 6. Error Handling & Retry Logic

```kotlin
class RobustWebSocketClient(
    private val baseUrl: String,
    private val listener: WebSocketListener
) {
    private var reconnectAttempts = 0
    private val maxRetries = 10
    private val initialBackoffMs = 1000L
    
    fun connectWithRetry() {
        try {
            connect()
        } catch (e: Exception) {
            scheduleReconnect()
        }
    }
    
    private fun scheduleReconnect() {
        if (reconnectAttempts >= maxRetries) {
            Log.e(TAG, "Max reconnection attempts reached")
            listener.onError(Exception("Failed to reconnect after $maxRetries attempts"))
            return
        }
        
        reconnectAttempts++
        val delayMs = initialBackoffMs * (2.0.pow(reconnectAttempts - 1)).toLong()
        
        Handler(Looper.getMainLooper()).postDelayed({
            Log.d(TAG, "Attempting reconnect (attempt $reconnectAttempts)")
            connectWithRetry()
        }, delayMs)
    }
    
    private fun connect() {
        // Implementation
    }
    
    companion object {
        private const val TAG = "RobustWebSocketClient"
    }
}
```

---

## 7. Security: WSS Implementation

```kotlin
class SecureWebSocketClient(
    baseUrl: String,
    private val listener: WebSocketListener,
    private val trustManager: X509TrustManager? = null,
    private val apiKey: String? = null
) {
    
    private val client = createSecureClient()
    
    private fun createSecureClient(): OkHttpClient {
        val builder = OkHttpClient.Builder()
            .connectTimeout(15, TimeUnit.SECONDS)
            .readTimeout(15, TimeUnit.SECONDS)
        
        // Add API key if provided
        apiKey?.let {
            builder.addInterceptor { chain ->
                val request = chain.request().newBuilder()
                    .addHeader("Authorization", "Bearer $it")
                    .build()
                chain.proceed(request)
            }
        }
        
        // Configure SSL/TLS
        if (trustManager != null) {
            try {
                val sslContext = SSLContext.getInstance("TLS").apply {
                    init(null, arrayOf(trustManager), SecureRandom())
                }
                builder.sslSocketFactory(sslContext.socketFactory, trustManager)
            } catch (e: Exception) {
                Log.e(TAG, "Error configuring SSL", e)
            }
        }
        
        return builder.build()
    }
    
    companion object {
        private const val TAG = "SecureWebSocketClient"
    }
}
```

---

## 8. Testing Examples

```kotlin
class CommandHandlerTest {
    private lateinit var commandHandler: CommandHandler
    private lateinit var mockWebViewExecutor: WebViewExecutor
    private lateinit var mockVideoPlayerExecutor: VideoPlayerExecutor
    private lateinit var mockNavigationExecutor: NavigationExecutor
    
    @Before
    fun setUp() {
        mockWebViewExecutor = mock()
        mockVideoPlayerExecutor = mock()
        mockNavigationExecutor = mock()
        
        commandHandler = CommandHandler(
            mockWebViewExecutor,
            mockVideoPlayerExecutor,
            mockNavigationExecutor
        )
    }
    
    @Test
    fun testPlayVideoCommand() {
        val command = Command(
            action = "play_video",
            url = "https://example.com/video.mp4"
        )
        
        val result = commandHandler.execute(command)
        
        assertTrue(result.isSuccess)
        verify(mockVideoPlayerExecutor).playVideo("https://example.com/video.mp4")
    }
    
    @Test
    fun testOpenUrlCommand() {
        val command = Command(
            action = "open_url",
            url = "https://example.com"
        )
        
        val result = commandHandler.execute(command)
        
        assertTrue(result.isSuccess)
        verify(mockWebViewExecutor).openUrl("https://example.com")
    }
    
    @Test
    fun testInvalidCommand() {
        val command = Command(
            action = "invalid_action",
            url = "https://example.com"
        )
        
        val result = commandHandler.execute(command)
        
        assertFalse(result.isSuccess)
    }
}

class WebSocketClientTest {
    private lateinit var client: TVCommandClientImpl
    private lateinit var mockListener: WebSocketListener
    
    @Before
    fun setUp() {
        mockListener = mock()
        client = TVCommandClientImpl("ws://localhost:8765", mockListener)
    }
    
    @Test
    fun testCommandSerialization() {
        val command = Command(
            action = "play_video",
            url = "https://example.com/video.mp4"
        )
        
        val serializer = CommandSerializer()
        val json = serializer.serialize(command)
        val deserialized = serializer.deserialize(json)
        
        assertEquals(command.action, deserialized.action)
        assertEquals(command.url, deserialized.url)
    }
}
```

---

## 9. Gradle Build Configuration

### build.gradle (Project Level)
```gradle
plugins {
    id 'com.android.application' version '8.0.0' apply false
    id 'com.android.library' version '8.0.0' apply false
    id 'org.jetbrains.kotlin.android' version '1.9.0' apply false
}

ext {
    compileSdk = 34
    targetSdk = 34
    minSdk = 24
    
    deps = [
        // Core
        kotlinStdlib: 'org.jetbrains.kotlin:kotlin-stdlib:1.9.0',
        coreKtx: 'androidx.core:core-ktx:1.12.0',
        appcompat: 'androidx.appcompat:appcompat:1.6.1',
        
        // Compose
        composeBom: 'androidx.compose:compose-bom:2023.10.00',
        composeUi: 'androidx.compose.ui:ui',
        composeMaterial3: 'androidx.compose.material3:material3',
        composeRuntime: 'androidx.compose.runtime:runtime',
        
        // WebSocket
        okhttp: 'com.squareup.okhttp3:okhttp:4.11.0',
        javaWebsocket: 'org.java-websocket:Java-WebSocket:1.5.4',
        
        // Video
        media3ExoPlayer: 'androidx.media3:media3-exoplayer:1.1.0',
        
        // Database
        room: 'androidx.room:room-runtime:2.5.2',
        roomKtx: 'androidx.room:room-ktx:2.5.2',
        
        // JSON
        gson: 'com.google.code.gson:gson:2.10.1',
        
        // QR Code
        mlKitVision: 'com.google.mlkit:vision-common:17.3.0',
        mlKitBarcode: 'com.google.mlkit:barcode-scanning:17.0.2',
        
        // Testing
        junit: 'junit:junit:4.13.2',
        mockito: 'org.mockito:mockito-core:5.2.1',
        espresso: 'androidx.test.espresso:espresso-core:3.5.1'
    ]
}
```

---

## Key Takeaways

1. **Start Simple**: Begin with basic WebSocket communication before adding features
2. **Test Early**: Write tests for command handling and serialization first
3. **Handle Errors**: Implement reconnection and error handling from day one
4. **Modularize**: Keep mobile and TV logic separate for easier maintenance
5. **Security**: Plan for WSS/TLS from the start, don't add it as an afterthought
6. **Performance**: Monitor connection times and command execution latency

Good luck! This is a well-scoped project that will teach you a lot about real-time Android apps. 🚀
