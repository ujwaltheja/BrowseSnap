# BrowseSnap - Quick Start Checklist & Commands

## Before You Start (Setup - 2 hours)

### Environment Setup
```bash
# Clone repository
git clone https://github.com/ujwaltheja/BrowseSnap.git
cd BrowseSnap

# Create feature branch
git checkout -b feature/websocket-foundation
git push -u origin feature/websocket-foundation

# Verify Android SDK
android list sdk
# Should have API 34 installed
```

### Verify Tools
- [ ] Android Studio 2023.2+ installed
- [ ] Kotlin 1.9.0+ plugin enabled
- [ ] Gradle 8.0+ available
- [ ] Java 17+ JDK set

### Project Structure Creation
```bash
# Create multi-module structure
mkdir -p mobile-app/src/main/java/com/browsesnap
mkdir -p tv-app/src/main/java/com/browsesnap
mkdir -p shared/src/main/java/com/browsesnap

# Copy build.gradle templates provided
# Update settings.gradle to include modules
```

---

## Week 1: WebSocket Foundation (28 hours)

### Day 1-2: Mobile WebSocket Client Setup

**Tasks:**
- [ ] Create `TVCommandClient.kt` in `shared` module
- [ ] Add OkHttp dependency to `build.gradle`
- [ ] Create `Command.kt` data class
- [ ] Create `CommandSerializer.kt`

**Code to implement:**
```kotlin
// shared/src/main/java/com/browsesnap/network/TVCommandClient.kt
interface WebSocketListener {
    fun onConnected()
    fun onDisconnected()
    fun onError(exception: Exception)
}

class TVCommandClient(
    private val baseUrl: String,
    private val listener: WebSocketListener
)
```

**Test:**
```kotlin
// Should be able to create client instance
val client = TVCommandClient("ws://localhost:8765", listener)
```

**Deliverable:** Client can be instantiated, compiles without errors

---

### Day 3-5: TV WebSocket Server Setup

**Tasks:**
- [ ] Create `TVWebSocketServer.kt` in `shared` module
- [ ] Add Java-WebSocket dependency
- [ ] Create `CommandHandler.kt`
- [ ] Implement connection manager

**Code structure:**
```kotlin
// shared/src/main/java/com/browsesnap/network/TVWebSocketServer.kt
class TVWebSocketServer(private val port: Int = 8765) {
    fun start(): Boolean
    fun stop()
    fun broadcast(message: String)
}
```

**Manual Test:**
```bash
# Start server on Android emulator
# Connect via `adb forward tcp:8765 tcp:8765`
# Try connecting from telnet
telnet 127.0.0.1 8765
```

**Deliverable:** Server starts, accepts connections, logs messages

---

### Day 6-7: Integration & Testing

**Tasks:**
- [ ] Write unit tests for serialization
- [ ] Test command sending/receiving
- [ ] Document API in README

**Commands:**
```bash
# Run tests
./gradlew test

# Build project
./gradlew build

# Create documentation
echo "# WebSocket Protocol Documentation" > WEBSOCKET.md
```

**Deliverable:** Tests pass, documentation created, basic flow works

---

## Week 2: Device Pairing (44 hours)

### Day 8-9: QR Code Generation (TV)

**Tasks:**
- [ ] Add QR library dependency
- [ ] Create `PairingManager.kt`
- [ ] Implement QR generation logic

**Gradle:**
```gradle
dependencies {
    implementation 'com.google.zxing:core:3.5.1'
}
```

**Code:**
```kotlin
class PairingManager(private val context: Context) {
    fun generatePairingQR(ipAddress: String, port: Int): Bitmap
    fun savePairingInfo(deviceName: String, ipAddress: String, port: Int)
}
```

**Test:**
```kotlin
// Should generate valid QR code
val bitmap = manager.generatePairingQR("192.168.1.100", 8765)
assert(bitmap != null)
```

---

### Day 10-12: QR Code Scanning (Mobile)

**Tasks:**
- [ ] Add ML Kit dependency
- [ ] Create `QRScanner.kt`
- [ ] Implement camera permission handling

**Gradle:**
```gradle
dependencies {
    implementation 'com.google.mlkit:barcode-scanning:17.0.2'
}
```

**Manifest:**
```xml
<uses-permission android:name="android.permission.CAMERA" />
```

**Code:**
```kotlin
class QRScanner(private val context: Context) {
    fun scanQRCode(imageProxy: ImageProxy, onSuccess: (String, Int) -> Unit)
}
```

---

### Day 13-14: PIN Fallback & Storage

**Tasks:**
- [ ] Create PIN generation logic
- [ ] Add SharedPreferences storage
- [ ] Implement device list management

**Code:**
```kotlin
// Pairing flow
1. Generate PIN on TV
2. User enters PIN on mobile
3. Store pairing info
4. Load on app startup
```

**Test Data:**
```kotlin
val pairingData = mapOf(
    "device_1" to "Living Room|192.168.1.100|8765"
)
```

---

## Week 3: Mobile App Foundation (24 hours)

### Day 15-16: SearchModule Implementation

**Tasks:**
- [ ] Create search UI with Compose
- [ ] Implement WebView or search bar
- [ ] Add search result display

**Compose Structure:**
```kotlin
@Composable
fun SearchScreen() {
    Column {
        SearchBar()
        ResultsList()
    }
}
```

**Test:**
- [ ] Search bar accepts input
- [ ] Results display properly
- [ ] No crashes on empty search

---

### Day 17-18: HistoryModule & Room Database

**Tasks:**
- [ ] Create Room entity and DAO
- [ ] Implement database operations
- [ ] Add history screen

**SQL Schema:**
```sql
CREATE TABLE search_history (
    id INTEGER PRIMARY KEY,
    url TEXT NOT NULL,
    action TEXT NOT NULL,
    timestamp INTEGER NOT NULL
)
```

---

## Week 4: Mobile UI Completion (32 hours)

### Tasks for Full Week
- [ ] Complete search screen UI
- [ ] Implement "Send to TV" buttons
- [ ] Add history screen
- [ ] Create device pairing screen
- [ ] Add connection status indicator

**UI Structure:**
```
HomeScreen
├── SearchBar + Results
├── HistoryList
└── ConnectionStatus

PairingScreen
├── QR Scanner OR PIN Entry
├── Device List
└── Connect Button
```

---

## Week 5: TV App Foundation (20 hours)

### Tasks
- [ ] Create TV WebView module
- [ ] Implement basic navigation
- [ ] Add control overlay UI
- [ ] Test with Android TV emulator

**Key Files:**
- `TVWebViewHandler.kt`
- `TVControlsOverlay.kt`
- `TVRemoteNavigation.kt`

---

## Week 6: Video Player & TV UI (44 hours)

### Tasks
- [ ] Integrate ExoPlayer
- [ ] Implement video playback controls
- [ ] Create TV-friendly UI
- [ ] Add D-pad navigation support

**Critical:** Test with actual TV remote or TV emulator

---

## Week 7: Error Handling & Security (24 hours)

### Tasks
- [ ] Implement reconnection logic
- [ ] Add error messages
- [ ] Implement WSS/TLS
- [ ] Add input validation

---

## Week 8: Testing & Polish (12 hours)

### Tasks
- [ ] Write comprehensive tests
- [ ] Performance optimization
- [ ] Bug fixes
- [ ] Final documentation

---

## Key Git Commands to Know

```bash
# Daily workflow
git add .
git commit -m "feat: implement WebSocket client"
git push origin feature/websocket-foundation

# When stuck
git status                    # See what changed
git diff                      # See actual changes
git log --oneline -5          # See recent commits
git reset --hard HEAD~1       # Undo last commit (careful!)

# Create PR when ready
# Go to GitHub → New Pull Request
# Select your feature branch
# Add description
# Request review
```

---

## Common Gradle Commands

```bash
# Build
./gradlew build                    # Full build
./gradlew assembleDebug            # Debug APK
./gradlew assembleRelease          # Release APK

# Test
./gradlew test                     # Unit tests
./gradlew connectedAndroidTest     # Instrumented tests

# Clean
./gradlew clean                    # Remove build files
./gradlew cleanBuildCache          # Clear cache

# Dependencies
./gradlew dependencies             # List dependencies
./gradlew dependencyUpdates        # Check for updates
```

---

## Debugging Commands

```bash
# View logs
adb logcat                         # All logs
adb logcat | grep browsesnap       # Filter logs
adb logcat -c                      # Clear logs

# Install/Run app
adb install app-debug.apk
adb shell am start -n com.browsesnap/.MainActivity

# Forward ports
adb forward tcp:8765 tcp:8765
```

---

## Emergency Troubleshooting

### Build Fails
```bash
# Try
./gradlew clean
./gradlew build --refresh-dependencies

# If still fails, check:
# - Gradle version in gradle/wrapper/gradle-wrapper.properties
# - targetSdk in build.gradle
# - Java version (java -version)
```

### WebSocket Not Connecting
```kotlin
// Check:
1. Is server running? (adb forward shows port)
2. Correct IP address and port?
3. Firewall allowing connection?
4. Network on same subnet?

// Debug code:
Log.d("WSClient", "Attempting to connect to: $baseUrl")
Log.d("WSClient", "Connection result: $result")
```

### QR Code Scanning Not Working
```kotlin
// Verify:
1. Camera permission granted? (Check Settings)
2. Lighting adequate for QR?
3. ML Kit downloaded? (Play Services)
4. Camera hardware available?

// Debug:
try {
    scanner.process(image)
        .addOnSuccessListener { barcodes ->
            Log.d("Scanner", "Found ${barcodes.size} barcodes")
        }
} catch (e: Exception) {
    Log.e("Scanner", "Error", e)
}
```

### Room Database Issues
```kotlin
// Migration error? Try:
.fallbackToDestructiveMigration()

// Or increase version:
@Database(entities = [...], version = 2)

// Can't insert data? Check:
suspend fun testInsert() {
    try {
        historyDao.insert(HistoryEntity(...))
        Log.d("DB", "Insert successful")
    } catch (e: Exception) {
        Log.e("DB", "Insert failed", e)
    }
}
```

---

## Performance Checkpoints

Test these at each phase:

| Metric | Target | Check Command |
|--------|--------|--------------|
| Build Time | <30s | `time ./gradlew build` |
| App Launch | <3s | Manual + logcat |
| WebSocket Connection | <500ms | Log timestamps |
| Command Execution | <500ms | Log before/after |
| Memory Usage | <100MB | Android Monitor |

---

## Documentation Checklist

Keep these updated:
- [ ] README.md - Project overview
- [ ] ARCHITECTURE.md - Design decisions
- [ ] SETUP.md - How to set up dev environment
- [ ] API.md - WebSocket command API
- [ ] TESTING.md - How to run tests
- [ ] DEPLOYMENT.md - Release process

---

## Success Indicators by Week

**Week 1**: 
- ✅ WebSocket client created
- ✅ WebSocket server created
- ✅ Commands serialize/deserialize
- ✅ Project builds without errors

**Week 2**:
- ✅ QR code generates on TV
- ✅ QR code scans on mobile
- ✅ Devices can pair
- ✅ Pairing info persists

**Week 3**:
- ✅ Mobile UI displays properly
- ✅ Search functionality works
- ✅ History stores data
- ✅ No crashes in basic flows

**Week 4**:
- ✅ All mobile UI screens complete
- ✅ "Send to TV" button functional
- ✅ Pairing flow seamless

**Week 5-6**:
- ✅ TV app displays WebView
- ✅ TV app plays videos
- ✅ D-pad navigation works

**Week 7-8**:
- ✅ Reconnection logic works
- ✅ Tests pass >70% coverage
- ✅ No major crashes
- ✅ Ready for beta testing

---

## When to Ask for Help

❌ **Don't wait for**:
- Compilation errors (fix immediately)
- Logic bugs (debug, then ask)
- Simple crashes (check logs first)

✅ **Ask quickly for**:
- Architecture decisions
- Third-party library issues
- Performance problems
- Security concerns

---

## Next Action

👉 **RIGHT NOW**: 
1. Read `BrowseSnap-Implementation-Guide.md`
2. Read `BrowseSnap-Technical-Guide.md`
3. Set up your project structure
4. Make first commit

👉 **THIS WEEK**: 
1. Implement WebSocket foundation
2. Write first tests
3. Deploy to emulator
4. Document what you built

**You've got this! 🚀**
