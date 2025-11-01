# BrowseSnap - Complete Setup & Deployment Guide

## 🚀 Quick Start Guide

### Prerequisites
- Android Studio Iguana (2023.2.1) or later
- JDK 17 or higher
- Android SDK API 35
- Kotlin 2.0.20
- Gradle 8.5+

### Project Setup

#### Step 1: Clone and Initialize
```bash
git clone https://github.com/ujwaltheja/BrowseSnap.git
cd BrowseSnap
```

#### Step 2: Project Structure
```
BrowseSnap/
├── settings.gradle.kts
├── build.gradle.kts
├── gradle.properties
├── core/
│   ├── build.gradle.kts
│   └── src/main/kotlin/com/tvbrowser/core/
│       ├── domain/models/
│       │   ├── Command.kt
│       │   └── Response.kt
│       ├── network/
│       │   ├── WebSocketClient.kt
│       │   └── WebSocketServer.kt
│       └── util/
│           └── Extensions.kt
├── mobile/
│   ├── build.gradle.kts
│   ├── src/main/
│   │   ├── AndroidManifest.xml
│   │   ├── kotlin/com/tvbrowser/mobile/
│   │   │   ├── MobileApplication.kt
│   │   │   ├── MainActivity.kt
│   │   │   ├── data/
│   │   │   │   ├── entity/
│   │   │   │   │   ├── BrowsingHistory.kt
│   │   │   │   │   └── PairedTV.kt
│   │   │   │   ├── dao/
│   │   │   │   │   ├── BrowsingHistoryDao.kt
│   │   │   │   │   └── PairedTVDao.kt
│   │   │   │   ├── database/
│   │   │   │   │   └── AppDatabase.kt
│   │   │   │   └── repository/
│   │   │   │       └── TVRepository.kt
│   │   │   ├── di/
│   │   │   │   └── AppModule.kt
│   │   │   ├── ui/
│   │   │   │   ├── screens/
│   │   │   │   │   ├── HomeScreen.kt
│   │   │   │   │   ├── SearchScreen.kt
│   │   │   │   │   ├── PairingScreen.kt
│   │   │   │   │   ├── RemoteControlScreen.kt
│   │   │   │   │   └── HistoryScreen.kt
│   │   │   │   └── theme/
│   │   │   │       ├── Color.kt
│   │   │   │       ├── Type.kt
│   │   │   │       └── Theme.kt
│   │   │   └── viewmodel/
│   │   │       └── MainViewModel.kt
│   │   └── res/
│   │       ├── values/
│   │       │   ├── strings.xml
│   │       │   ├── colors.xml
│   │       │   └── themes.xml
│   │       └── mipmap-*/
│   │           └── ic_launcher.png
└── tv/
    ├── build.gradle.kts
    └── src/main/
        ├── AndroidManifest.xml
        ├── kotlin/com/tvbrowser/tv/
        │   ├── TVApplication.kt
        │   ├── TVActivity.kt
        │   ├── ui/
        │   │   ├── screens/
        │   │   │   ├── TVMainScreen.kt
        │   │   │   ├── TVPairingScreen.kt
        │   │   │   ├── TVBrowserScreen.kt
        │   │   │   └── TVVideoPlayerScreen.kt
        │   │   └── theme/
        │   │       └── TVTheme.kt
        │   └── viewmodel/
        │       └── TVViewModel.kt
        └── res/
            ├── values/
            │   ├── strings.xml
            │   └── themes.xml
            └── mipmap-*/
                └── ic_banner.png
```

#### Step 3: Resource Files

**mobile/src/main/res/values/strings.xml**
```xml
<resources>
    <string name="app_name">BrowseSnap</string>
    <string name="pair_tv">Pair with TV</string>
    <string name="scan_qr">Scan QR Code</string>
    <string name="enter_manually">Enter Manually</string>
    <string name="search_hint">Enter URL or search query</string>
    <string name="no_history">No browsing history</string>
    <string name="connected">Connected</string>
    <string name="disconnected">Disconnected</string>
</resources>
```

**mobile/src/main/res/values/themes.xml**
```xml
<resources>
    <style name="Theme.BrowseSnap" parent="android:Theme.Material.Light.NoActionBar">
        <item name="android:statusBarColor">@color/purple_700</item>
    </style>
</resources>
```

**tv/src/main/res/values/strings.xml**
```xml
<resources>
    <string name="app_name">BrowseSnap TV</string>
    <string name="banner_text">BrowseSnap - Mobile TV Browser Control</string>
    <string name="pair_instruction">Scan QR code or enter IP and PIN on mobile</string>
</resources>
```

**tv/src/main/res/values/themes.xml**
```xml
<resources>
    <style name="Theme.BrowseSnap.TV" parent="Theme.Leanback">
        <item name="android:windowBackground">@android:color/black</item>
        <item name="android:colorPrimary">@color/tv_primary</item>
    </style>
</resources>
```

#### Step 4: ProGuard Rules

**mobile/proguard-rules.pro**
```proguard
# Keep WebSocket classes
-keep class org.java_websocket.** { *; }
-keep class com.tvbrowser.core.** { *; }

# Keep Room
-keep class * extends androidx.room.RoomDatabase
-keep @androidx.room.Entity class *
-dontwarn androidx.room.paging.**

# Keep Kotlin serialization
-keepattributes *Annotation*, InnerClasses
-dontnote kotlinx.serialization.AnnotationsKt
-keepclassmembers class kotlinx.serialization.json.** {
    *** Companion;
}
-keepclasseswithmembers class kotlinx.serialization.json.** {
    kotlinx.serialization.KSerializer serializer(...);
}

# Keep ZXing
-keep class com.google.zxing.** { *; }
-dontwarn com.google.zxing.**

# OkHttp
-dontwarn okhttp3.**
-keep class okhttp3.** { *; }
```

**tv/proguard-rules.pro**
```proguard
# Keep ExoPlayer
-keep class androidx.media3.** { *; }
-dontwarn androidx.media3.**

# Keep WebView
-keepclassmembers class * extends android.webkit.WebViewClient {
    public void *(android.webkit.WebView, java.lang.String, android.graphics.Bitmap);
    public boolean *(android.webkit.WebView, java.lang.String);
}
-keepclassmembers class * extends android.webkit.WebChromeClient {
    public void *(android.webkit.WebView, java.lang.String);
}

# Keep WebSocket
-keep class org.java_websocket.** { *; }
-keep class com.tvbrowser.core.** { *; }
```

### Build & Run

#### Build Commands
```bash
# Clean build
./gradlew clean

# Build all modules
./gradlew build

# Build mobile APK
./gradlew :mobile:assembleDebug

# Build TV APK
./gradlew :tv:assembleDebug

# Install on connected device
./gradlew :mobile:installDebug
./gradlew :tv:installDebug
```

#### Run Configurations

**Mobile App**:
- Module: `mobile`
- Launch Activity: `com.tvbrowser.mobile.MainActivity`
- Deployment Target: Android 8.0+ (API 26)

**TV App**:
- Module: `tv`
- Launch Activity: `com.tvbrowser.tv.TVActivity`
- Deployment Target: Android TV 7.0+ (API 24)

### Testing Guide

#### 1. Set Up Test Environment
```bash
# Start Android emulator (Mobile)
emulator -avd Pixel_5_API_33

# Start Android TV emulator
emulator -avd Android_TV_1080p_API_33
```

#### 2. Network Setup
- Ensure both devices are on the same network
- For emulators, use port forwarding:
```bash
adb -s emulator-5554 forward tcp:8888 tcp:8888
```

#### 3. Pairing Process
1. Launch TV app
2. Note IP address and PIN displayed
3. Launch Mobile app
4. Tap "Pair with TV"
5. Either:
   - Scan QR code shown on TV
   - Or enter IP and PIN manually
6. Connection established ✅

#### 4. Feature Testing

**URL Opening**:
1. Go to Search tab
2. Enter "https://www.youtube.com"
3. Tap "Open URL"
4. Verify URL opens on TV

**Video Playback**:
1. Enter video URL (e.g., `.mp4` file)
2. Tap "Play Video"
3. Verify video plays on TV with controls

**Remote Control**:
1. Go to Remote tab
2. Test navigation (Back/Forward)
3. Test playback controls (Play/Pause/Stop)
4. Test volume control

**History**:
1. Go to History tab
2. View recent activities
3. Tap item to replay
4. Test clear history

### Troubleshooting

#### Connection Issues

**Problem**: Mobile can't connect to TV
```
Solution:
1. Verify both on same WiFi network
2. Check TV app shows "Server running"
3. Verify IP address is correct
4. Try disabling firewall temporarily
5. Check port 8888 is not blocked
```

**Problem**: QR code won't scan
```
Solution:
1. Ensure good lighting
2. Grant camera permissions
3. Try manual pairing instead
4. Check QR code is fully visible
```

#### Build Issues

**Problem**: Gradle sync fails
```bash
# Clear Gradle cache
./gradlew clean
rm -rf ~/.gradle/caches/
./gradlew build --refresh-dependencies
```

**Problem**: Kotlin compiler error
```bash
# Update Kotlin plugin
# In build.gradle.kts:
kotlin("android") version "2.0.20"
```

**Problem**: Room schema export error
```
Solution:
Add to build.gradle.kts:
ksp {
    arg("room.schemaLocation", "$projectDir/schemas")
}
```

### Performance Optimization

#### Mobile App
```kotlin
// Enable R8 optimization
android {
    buildTypes {
        release {
            isMinifyEnabled = true
            isShrinkResources = true
            proguardFiles(...)
        }
    }
}
```

#### TV App
```kotlin
// Enable hardware acceleration
android {
    defaultConfig {
        ndk {
            abiFilters += listOf("armeabi-v7a", "arm64-v8a")
        }
    }
}
```

### Deployment

#### Generate Signed APKs

**Mobile App**:
```bash
./gradlew :mobile:bundleRelease
# Output: mobile/build/outputs/bundle/release/mobile-release.aab
```

**TV App**:
```bash
./gradlew :tv:assembleRelease
# Output: tv/build/outputs/apk/release/tv-release.apk
```

#### Play Store Preparation

1. **Create Keystore**:
```bash
keytool -genkey -v -keystore browsesnap.keystore \
  -alias browsesnap -keyalg RSA -keysize 2048 -validity 10000
```

2. **Add to gradle.properties**:
```properties
BROWSESNAP_KEYSTORE_PATH=../browsesnap.keystore
BROWSESNAP_KEYSTORE_PASSWORD=your_password
BROWSESNAP_KEY_ALIAS=browsesnap
BROWSESNAP_KEY_PASSWORD=your_key_password
```

3. **Configure signing in build.gradle.kts**:
```kotlin
android {
    signingConfigs {
        create("release") {
            storeFile = file(project.property("BROWSESNAP_KEYSTORE_PATH") as String)
            storePassword = project.property("BROWSESNAP_KEYSTORE_PASSWORD") as String
            keyAlias = project.property("BROWSESNAP_KEY_ALIAS") as String
            keyPassword = project.property("BROWSESNAP_KEY_PASSWORD") as String
        }
    }
    
    buildTypes {
        release {
            signingConfig = signingConfigs.getByName("release")
        }
    }
}
```

### Advanced Features

#### Add Notification Support (Mobile)
```kotlin
// In TVRepository.kt
fun sendNotification(title: String, message: String) {
    val notificationManager = context.getSystemService(
        Context.NOTIFICATION_SERVICE
    ) as NotificationManager
    
    // Build and show notification
}
```

#### Add Voice Commands (Mobile)
```kotlin
// Integrate with Android Speech Recognition
implementation("androidx.compose.material3:material3:1.3.1")
```

#### Add Cast Support
```kotlin
// Add Google Cast SDK
implementation("com.google.android.gms:play-services-cast-framework:21.5.0")
```

### Monitoring & Analytics

#### Add Timber Logging
```kotlin
// Already integrated
if (BuildConfig.DEBUG) {
    Timber.plant(Timber.DebugTree())
}
```

#### Add Crash Reporting
```kotlin
// Add Firebase Crashlytics
implementation("com.google.firebase:firebase-crashlytics-ktx:19.2.1")
```

### Documentation

#### API Documentation
```bash
# Generate KDoc
./gradlew dokkaHtml
# Output: build/dokka/html/index.html
```

#### Architecture Diagram
```
┌─────────────────────┐         WebSocket (Port 8888)         ┌─────────────────────┐
│   Mobile App        │◄──────────────────────────────────────►│   TV App            │
│                     │         JSON Commands/Responses        │                     │
│  ┌───────────────┐  │                                        │  ┌───────────────┐  │
│  │ UI (Compose)  │  │                                        │  │ UI (Compose)  │  │
│  └───────┬───────┘  │                                        │  └───────┬───────┘  │
│          │          │                                        │          │          │
│  ┌───────▼────────┐ │                                        │  ┌───────▼────────┐ │
│  │  ViewModel     │ │                                        │  │  ViewModel     │ │
│  └───────┬────────┘ │                                        │  └───────┬────────┘ │
│          │          │                                        │          │          │
│  ┌───────▼────────┐ │                                        │  ┌───────▼────────┐ │
│  │  Repository    │ │                                        │  │  WS Server     │ │
│  └───────┬────────┘ │                                        │  └───────┬────────┘ │
│          │          │                                        │          │          │
│  ┌───────▼────────┐ │                                        │  ┌───────▼────────┐ │
│  │  Room DB       │ │                                        │  │  WebView       │ │
│  └────────────────┘ │                                        │  │  ExoPlayer     │ │
│                     │                                        │  └────────────────┘ │
└─────────────────────┘                                        └─────────────────────┘
```

### License
```
MIT License

Copyright (c) 2025 Ujwal Theja

Permission is hereby granted, free of charge, to any person obtaining a copy
of this software and associated documentation files (the "Software"), to deal
in the Software without restriction...
```

### Support
- GitHub Issues: https://github.com/ujwaltheja/BrowseSnap/issues
- Email: support@browsesnap.dev
- Documentation: https://browsesnap.dev/docs

---

## 🎉 You're All Set!

Your BrowseSnap app is now fully configured and ready for development and deployment. Happy coding! 🚀
