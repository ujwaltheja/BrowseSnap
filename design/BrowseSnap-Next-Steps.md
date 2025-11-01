# 🎯 BrowseSnap Next Steps - Immediate Action Plan

**Current Status After Commit 0a2583a**: 34.1% Complete ✅  
**What's Working**: Infrastructure foundation (Database, Repositories, ViewModels)  
**What's Broken**: Everything else (can't run, can't connect, can't display UI)

---

## 🚨 CRITICAL BLOCKER

### The #1 Problem Right Now
**WebSocket communication is completely missing.**

Without it:
- ❌ Mobile app can't send commands to TV
- ❌ TV app can't receive any commands
- ❌ Entire app is non-functional
- ❌ Nothing works end-to-end

**This is YOUR #1 PRIORITY** - Start immediately.

---

## ⚡ WHAT TO BUILD NEXT (Priority Order)

### PHASE 1: WebSocket Communication (1 week)
**Status**: 🔴 **CRITICAL - START NOW**  
**Estimated Time**: 20-24 hours  
**Blocker Level**: Blocks everything else

#### 1.1 Mobile WebSocket Client
```kotlin
// File: mobile/src/main/kotlin/com/tvbrowser/mobile/network/TVCommandClient.kt

class TVCommandClient(
    private val baseUrl: String,
    private val listener: WebSocketListener
) {
    private var webSocket: WebSocket? = null
    
    fun connect(onSuccess: () -> Unit, onError: (String) -> Unit) {
        // Use OkHttp to establish WebSocket connection
    }
    
    fun sendCommand(command: Command) {
        // Serialize command to JSON and send via WebSocket
    }
    
    fun disconnect() {
        // Clean close of connection
    }
}
```

**Dependencies to add**:
```gradle
// In mobile/build.gradle.kts
dependencies {
    implementation 'com.squareup.okhttp3:okhttp:4.11.0'
    implementation 'com.google.code.gson:gson:2.10.1'
}
```

**Test checklist**:
- [ ] Create OkHttp client
- [ ] Establish WebSocket connection to ws://localhost:8888
- [ ] Send test JSON command
- [ ] Receive response
- [ ] Handle disconnection gracefully

---

#### 1.2 TV WebSocket Server
```kotlin
// File: tv/src/main/kotlin/com/tvbrowser/tv/server/TVWebSocketServer.kt

class TVWebSocketServer(private val port: Int = 8888) {
    private var server: WebSocketServer? = null
    
    fun start() {
        // Use Java-WebSocket to listen for connections
    }
    
    fun stop() {
        // Graceful shutdown
    }
    
    fun broadcast(message: String) {
        // Send message to all connected clients
    }
}
```

**Dependencies to add**:
```gradle
// In tv/build.gradle.kts
dependencies {
    implementation 'org.java-websocket:Java-WebSocket:1.5.4'
}
```

**Test checklist**:
- [ ] Server starts on port 8888
- [ ] Accepts WebSocket connections
- [ ] Receives JSON commands
- [ ] Handles multiple clients
- [ ] Graceful error handling

---

#### 1.3 Command Serialization
```kotlin
// File: core/src/main/kotlin/com/tvbrowser/core/domain/models/Command.kt

@Serializable
sealed class TVCommand {
    @Serializable
    data class OpenUrl(val url: String) : TVCommand()
    
    @Serializable
    data class PlayVideo(val url: String) : TVCommand()
    
    @Serializable
    data class NavigateBack : TVCommand()
    
    @Serializable
    data class Pause : TVCommand()
    
    @Serializable
    data class Resume : TVCommand()
    
    // ... more commands
}

object CommandSerializer {
    fun serialize(command: TVCommand): String = Gson().toJson(command)
    fun deserialize(json: String): TVCommand = Gson().fromJson(json, TVCommand::class.java)
}
```

**Test checklist**:
- [ ] Commands serialize to JSON
- [ ] JSON deserializes back to commands
- [ ] No data loss in serialization
- [ ] All command types work

---

### PHASE 2: Mobile UI Screens (1-2 weeks)
**Status**: 🟡 **MEDIUM - Can start after Phase 1**  
**Estimated Time**: 32-40 hours  
**Blocker Level**: Blocks user interaction

#### 2.1 Main Compose Screens
```kotlin
// File: mobile/src/main/kotlin/com/tvbrowser/mobile/ui/screens/HomeScreen.kt

@Composable
fun HomeScreen(viewModel: EnhancedMobileViewModel) {
    val connectionStatus by viewModel.connectionStatus.collectAsState()
    val pairedDevices by viewModel.pairedDevices.collectAsState()
    val recentHistory by viewModel.recentHistory.collectAsState()
    
    Column(modifier = Modifier.fillMaxSize()) {
        // Connection status header
        ConnectionStatusCard(connectionStatus)
        
        // Paired devices list
        PairedDevicesList(pairedDevices)
        
        // Recent URLs/videos
        RecentHistoryList(recentHistory)
        
        // Action buttons
        Row {
            Button(onClick = { /* pair new TV */ }) {
                Text("Pair New TV")
            }
            Button(onClick = { /* search */ }) {
                Text("Search")
            }
        }
    }
}
```

**Screens to create**:
1. **HomeScreen** - Main dashboard
2. **SearchScreen** - Search for content
3. **PairingScreen** - Pair with TV (QR/PIN)
4. **RemoteControlScreen** - Play/pause/volume
5. **SettingsScreen** - App settings

**Dependencies needed**:
```gradle
dependencies {
    implementation "androidx.compose.ui:ui:1.5.4"
    implementation "androidx.compose.material3:material3:1.1.1"
    implementation "androidx.lifecycle:lifecycle-viewmodel-compose:2.6.1"
}
```

**Test checklist**:
- [ ] Screens compose without errors
- [ ] ViewModels wire correctly
- [ ] Navigation works
- [ ] No memory leaks
- [ ] Responsive layout

---

### PHASE 3: TV App Foundation (2-3 weeks)
**Status**: 🔴 **CRITICAL - Can start after Phase 1**  
**Estimated Time**: 56-80 hours  
**Blocker Level**: Blocks core functionality

#### 3.1 Command Handler
```kotlin
// File: tv/src/main/kotlin/com/tvbrowser/tv/server/CommandHandler.kt

class CommandHandler(
    private val webViewHandler: WebViewHandler,
    private val videoPlayerHandler: VideoPlayerHandler
) {
    fun execute(command: TVCommand) {
        when (command) {
            is TVCommand.OpenUrl -> webViewHandler.loadUrl(command.url)
            is TVCommand.PlayVideo -> videoPlayerHandler.play(command.url)
            is TVCommand.NavigateBack -> webViewHandler.back()
            is TVCommand.Pause -> videoPlayerHandler.pause()
            is TVCommand.Resume -> videoPlayerHandler.resume()
            // ... handle other commands
        }
    }
}
```

**Components to build**:
1. **WebViewHandler** - Load and navigate URLs
2. **VideoPlayerHandler** - Play videos with ExoPlayer
3. **NavigationHandler** - D-pad navigation

**Dependencies**:
```gradle
dependencies {
    implementation "androidx.media3:media3-exoplayer:1.1.0"
    implementation "androidx.media3:media3-ui:1.1.0"
}
```

---

#### 3.2 TV UI Layout
```kotlin
// File: tv/src/main/kotlin/com/tvbrowser/tv/ui/screens/BrowserScreen.kt

@Composable
fun BrowserScreen(viewModel: TVViewModel) {
    val currentUrl by viewModel.currentUrl.collectAsState()
    
    Box(modifier = Modifier.fillMaxSize()) {
        // WebView for browsing
        WebViewContainer(currentUrl)
        
        // Control overlay (appears on D-pad press)
        ControlOverlay(
            onPlay = { /* play video */ },
            onPause = { /* pause */ },
            onBack = { /* back */ }
        )
        
        // Connection status indicator
        ConnectionIndicator()
    }
}
```

**TV-specific considerations**:
- D-pad navigation (no touch)
- Large text/buttons (viewing distance)
- Remote control support
- Always-on capability

---

### PHASE 4: Pairing Flow (1 week)
**Status**: 🟡 **MEDIUM - Can do after Phase 1+2**  
**Estimated Time**: 24-32 hours  
**Blocker Level**: Convenience feature (MVP can work without)

#### 4.1 QR Code Generation (TV)
```kotlin
class PairingManager {
    fun generateQRCode(ipAddress: String, port: Int): Bitmap {
        val content = "BrowseSnap://$ipAddress:$port"
        val writer = QRCodeWriter()
        val bitMatrix = writer.encode(content, BarcodeFormat.QR_CODE, 512, 512)
        // Convert to Bitmap and return
    }
}
```

#### 4.2 QR Code Scanning (Mobile)
```kotlin
class QRScanner {
    fun scanQRCode(imageProxy: ImageProxy) {
        val scanner = BarcodeScanning.getClient()
        // Process image and extract QR data
        // Parse IP:port from QR content
    }
}
```

**Dependencies**:
```gradle
dependencies {
    implementation 'com.google.mlkit:barcode-scanning:17.0.2'
    implementation 'com.google.zxing:core:3.5.1'
}
```

---

## 📋 WEEKLY BREAKDOWN

### WEEK 1 (RIGHT NOW!)
**Goal**: Get WebSocket working end-to-end

- [ ] Day 1-2: Implement mobile WebSocket client
- [ ] Day 3-4: Implement TV WebSocket server
- [ ] Day 5: Implement command serialization
- [ ] Day 6-7: Integration testing and debugging

**Definition of Done**:
- Mobile can connect to TV via WebSocket
- Commands can be sent and received
- No crashes

**Test**: 
```bash
# Terminal 1: Run TV app on emulator/device
./gradlew :tv:run

# Terminal 2: Run mobile app on emulator/device
./gradlew :mobile:run

# Manual test: Send command from mobile, see TV respond
```

---

### WEEK 2
**Goal**: Basic mobile UI working

- [ ] Day 1-2: Home screen UI
- [ ] Day 3-4: Search screen UI
- [ ] Day 5-6: Remote control screen UI
- [ ] Day 7: Navigation & wiring to ViewModels

**Definition of Done**:
- App doesn't crash
- Can navigate between screens
- ViewModels connected to UI

---

### WEEK 3
**Goal**: Basic TV app functionality

- [ ] Day 1-3: WebView implementation
- [ ] Day 4-5: Basic command execution
- [ ] Day 6-7: TV UI layout

**Definition of Done**:
- TV can open URLs from mobile
- Basic navigation works
- No crashes

---

### WEEK 4
**Goal**: Video playback working

- [ ] Day 1-3: ExoPlayer integration
- [ ] Day 4-5: Video player UI
- [ ] Day 6-7: Play/pause/seek controls

**Definition of Done**:
- Can play videos from mobile command
- Playback controls work

---

### WEEKS 5-6
**Goal**: Add pairing and features

- [ ] QR code generation/scanning
- [ ] PIN-based pairing
- [ ] Error handling
- [ ] Testing

---

## 🔥 DO THIS TODAY

1. **Create WebSocket Client file**
   ```bash
   touch mobile/src/main/kotlin/com/tvbrowser/mobile/network/TVCommandClient.kt
   ```

2. **Add OkHttp dependency**
   - Edit `mobile/build.gradle.kts`
   - Add: `implementation 'com.squareup.okhttp3:okhttp:4.11.0'`

3. **Implement basic client**
   ```kotlin
   class TVCommandClient(private val url: String) {
       private val client = OkHttpClient()
       private var webSocket: WebSocket? = null
       
       fun connect() {
           val request = Request.Builder().url(url).build()
           webSocket = client.newWebSocket(request, object : WebSocketListener() {
               override fun onOpen(webSocket: WebSocket, response: Response) {
                   Log.d("TVClient", "Connected")
               }
               override fun onMessage(webSocket: WebSocket, text: String) {
                   Log.d("TVClient", "Received: $text")
               }
           })
       }
   }
   ```

4. **Push commit**
   ```bash
   git add .
   git commit -m "feat: implement WebSocket client foundation"
   git push origin feature/websocket-client
   ```

5. **Test locally**
   - Run mobile app
   - Check logcat for "Connected" message

---

## 📊 AFTER THIS COMMIT

| Metric | Before | After (WebSocket) |
|--------|--------|-------------------|
| Overall % | 34.1% | 40-45% |
| Networking | 0% | ✅ 100% |
| Infrastructure | ✅ 100% | ✅ 100% |
| Working Features | 0 | 1 (basic commands) |
| Commits | 2 | 3 |

---

## ⚠️ COMMON MISTAKES TO AVOID

❌ **DON'T**: Start with UI before WebSocket works  
✅ **DO**: Get WebSocket working first

❌ **DON'T**: Build all features before testing  
✅ **DO**: Test constantly

❌ **DON'T**: Ignore errors and warnings  
✅ **DO**: Fix them immediately

❌ **DON'T**: Work on TV app while mobile incomplete  
✅ **DO**: Keep both in sync

❌ **DON'T**: Skip the command serialization layer  
✅ **DO**: Build it properly once

---

## 🎉 YOU'RE 34% THERE!

The hard infrastructure work is done. Now comes the fun part - building the communication and features!

**Next milestone**: WebSocket working (Week 1) → 40-45% complete  
**Your goal**: Commit working WebSocket by next Friday

**Let's go! 🚀**

