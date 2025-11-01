# BrowseSnap - Setup Guide

This guide will help you set up the BrowseSnap development environment and run the applications.

## Table of Contents
1. [System Requirements](#system-requirements)
2. [Development Environment Setup](#development-environment-setup)
3. [Project Setup](#project-setup)
4. [Building the Project](#building-the-project)
5. [Running on Devices](#running-on-devices)
6. [Troubleshooting](#troubleshooting)

## System Requirements

### Hardware Requirements
- **Computer**: 8GB RAM minimum (16GB recommended)
- **Disk Space**: 10GB free space
- **Android Device**: Android 8.0+ (API 26+) for mobile
- **Android TV**: Android 7.0+ (API 24+) for TV

### Software Requirements
- **Operating System**: Windows 10/11, macOS 11+, or Linux (Ubuntu 20.04+)
- **Java Development Kit**: JDK 17 or later
- **Android Studio**: Flamingo (2022.2.1) or later
- **Android SDK**: API 24, 26, 34, 35
- **Gradle**: 8.0+ (included in project)

## Development Environment Setup

### Step 1: Install JDK 17

#### On macOS (using Homebrew):
```bash
brew install openjdk@17
sudo ln -sfn /opt/homebrew/opt/openjdk@17/libexec/openjdk.jdk /Library/Java/JavaVirtualMachines/openjdk-17.jdk
```

#### On Ubuntu/Linux:
```bash
sudo apt update
sudo apt install openjdk-17-jdk
```

#### On Windows:
- Download from [Oracle JDK](https://www.oracle.com/java/technologies/downloads/#java17) or [Adoptium](https://adoptium.net/)
- Run the installer
- Set JAVA_HOME environment variable

Verify installation:
```bash
java -version
# Should show: openjdk version "17.x.x"
```

### Step 2: Install Android Studio

1. Download Android Studio from [developer.android.com](https://developer.android.com/studio)
2. Run the installer
3. During setup, ensure you install:
   - Android SDK
   - Android SDK Platform (API 34, 35)
   - Android Virtual Device (AVD)

### Step 3: Configure Android SDK

1. Open Android Studio
2. Go to **Tools > SDK Manager**
3. Install:
   - **SDK Platforms**: Android 14.0 (API 34), Android 15.0 (API 35)
   - **SDK Tools**:
     - Android SDK Build-Tools
     - Android SDK Platform-Tools
     - Android Emulator
     - Google Play services

## Project Setup

### Step 1: Clone the Repository

```bash
git clone https://github.com/ujwaltheja/BrowseSnap.git
cd BrowseSnap
```

### Step 2: Open in Android Studio

1. Launch Android Studio
2. Select **File > Open**
3. Navigate to the `BrowseSnap` directory
4. Click **OK**
5. Wait for Gradle sync to complete (this may take several minutes on first run)

### Step 3: Configure Build Variants

Android Studio should automatically detect the modules:
- `:app` (legacy, can be ignored)
- `:core` (shared library)
- `:mobile` (mobile application)
- `:tv` (TV application)

## Building the Project

### Using Android Studio

1. Select **Build > Make Project** or press `Ctrl+F9` (Windows/Linux) or `Cmd+F9` (macOS)
2. Wait for build to complete
3. Check the Build output for any errors

### Using Gradle Command Line

#### Full Build
```bash
./gradlew build
```

#### Build Mobile App Only
```bash
./gradlew :mobile:build
```

#### Build TV App Only
```bash
./gradlew :tv:build
```

#### Clean Build
```bash
./gradlew clean build
```

### Build Outputs

After successful build, APKs are located at:
- Mobile Debug: `mobile/build/outputs/apk/debug/mobile-debug.apk`
- Mobile Release: `mobile/build/outputs/apk/release/mobile-release.apk`
- TV Debug: `tv/build/outputs/apk/debug/tv-debug.apk`
- TV Release: `tv/build/outputs/apk/release/tv-release.apk`

## Running on Devices

### Option 1: Using Android Studio

#### Running Mobile App

1. Connect your Android phone via USB (with USB debugging enabled)
   - Or start an Android Emulator: **Tools > Device Manager > Create Device**
2. Select **mobile** from the configuration dropdown (top toolbar)
3. Select your device from the device dropdown
4. Click the **Run** button (green play icon) or press `Shift+F10`

#### Running TV App

1. Connect your Android TV via USB or ADB over network
   - Or start an Android TV Emulator: **Tools > Device Manager > Create Device > TV**
2. Select **tv** from the configuration dropdown
3. Select your TV device from the device dropdown
4. Click the **Run** button

### Option 2: Using ADB Command Line

#### Install Mobile App
```bash
# Connect device
adb devices

# Install debug APK
adb install mobile/build/outputs/apk/debug/mobile-debug.apk

# Launch app
adb shell am start -n com.tvbrowser.mobile/.MainActivity
```

#### Install TV App
```bash
# Connect TV device
adb connect <TV_IP_ADDRESS>:5555

# Install debug APK
adb install tv/build/outputs/apk/debug/tv-debug.apk

# Launch app
adb shell am start -n com.tvbrowser.tv/.TVMainActivity
```

### Option 3: ADB Over Wi-Fi

#### For Mobile Device
```bash
# Connect device via USB first
adb tcpip 5555
adb connect <PHONE_IP>:5555
# You can now disconnect USB cable
```

#### For TV Device
```bash
# Enable ADB over network on TV settings
adb connect <TV_IP>:5555
adb devices
```

## Device Configuration

### Mobile Device Setup

1. **Enable Developer Options**:
   - Go to Settings > About Phone
   - Tap "Build Number" 7 times
   - Go back to Settings > System > Developer Options

2. **Enable USB Debugging**:
   - In Developer Options, enable "USB Debugging"

3. **Grant Permissions** (when app first runs):
   - Camera (for QR scanning)
   - Network access

### TV Device Setup

1. **Enable Developer Options**:
   - Go to Settings > Device Preferences > About
   - Scroll to "Build" and click it 7 times

2. **Enable ADB Debugging**:
   - Go to Settings > Device Preferences > Developer Options
   - Enable "USB debugging" and "Network debugging"

3. **Note the TV's IP Address**:
   - Settings > Network & Internet > Your Wi-Fi Network
   - Write down the IP address

## Testing the Setup

### Quick Test Flow

1. **Start TV App**:
   - Launch on Android TV
   - Should see Pairing screen with QR code and PIN
   - Note the IP address displayed

2. **Start Mobile App**:
   - Launch on Android phone
   - Tap "Pair with TV"
   - Either scan QR code or enter IP + PIN

3. **Test Connection**:
   - On mobile, go to Search
   - Enter a URL (e.g., "google.com")
   - Tap to send to TV
   - Verify TV browser opens the URL

## Troubleshooting

### Gradle Sync Failed

**Error**: "Plugin with id 'com.android.application' not found"

**Solution**:
```bash
# Update Gradle wrapper
./gradlew wrapper --gradle-version 8.1

# Sync again in Android Studio
```

### Android SDK Not Found

**Error**: "Android SDK location not found"

**Solution**:
1. Open `local.properties` (or create it in project root)
2. Add:
   ```
   sdk.dir=/path/to/Android/sdk
   ```
   - macOS: `/Users/{username}/Library/Android/sdk`
   - Windows: `C:\\Users\\{username}\\AppData\\Local\\Android\\sdk`
   - Linux: `/home/{username}/Android/sdk`

### ADB Device Not Found

**Error**: "No devices found"

**Solution**:
```bash
# Restart ADB server
adb kill-server
adb start-server

# Check devices
adb devices

# If still not found, check USB cable and USB debugging setting
```

### Build Takes Too Long

**Solution**:
- Enable Gradle daemon: Add `org.gradle.daemon=true` to `gradle.properties`
- Increase heap size: Add `org.gradle.jvmargs=-Xmx4096m` to `gradle.properties`
- Enable parallel builds: Add `org.gradle.parallel=true`

### Emulator Won't Start

**Solution**:
1. Check BIOS: Ensure virtualization (VT-x/AMD-V) is enabled
2. Windows: Ensure Hyper-V is disabled or use Hyper-V accelerated emulator
3. Try creating a new AVD with lower resolution

### App Crashes on Startup

**Solution**:
```bash
# Check logs
adb logcat | grep "tvbrowser"

# Common issues:
# - Missing permissions in manifest
# - Database migration errors
# - Network security config
```

## Environment Variables (Optional)

Add to your `~/.bashrc` or `~/.zshrc`:

```bash
# Android SDK
export ANDROID_HOME=$HOME/Library/Android/sdk  # macOS
# export ANDROID_HOME=$HOME/Android/sdk  # Linux
# export ANDROID_HOME=%LOCALAPPDATA%\Android\sdk  # Windows

export PATH=$PATH:$ANDROID_HOME/emulator
export PATH=$PATH:$ANDROID_HOME/platform-tools
export PATH=$PATH:$ANDROID_HOME/tools/bin

# Java
export JAVA_HOME=/Library/Java/JavaVirtualMachines/openjdk-17.jdk/Contents/Home  # macOS
# export JAVA_HOME=/usr/lib/jvm/java-17-openjdk-amd64  # Linux
```

## Network Configuration

### Firewall Rules

If connection fails, ensure firewall allows:
- Port **8888** (WebSocket server on TV)
- UDP ports for network discovery (if implementing mDNS)

#### On macOS:
```bash
# Allow incoming connections (one-time)
sudo /usr/libexec/ApplicationFirewall/socketfilterfw --add /path/to/app
```

#### On Windows:
- Open Windows Defender Firewall
- Add rule for port 8888 TCP

#### On Linux:
```bash
sudo ufw allow 8888/tcp
```

## Next Steps

After setup is complete:
1. Read [README.md](README.md) for usage instructions
2. Review architecture documentation in `design/` folder
3. Check [ARCHITECTURE.md](design/BrowseSnap-Technical-Guide.md) for technical details
4. Start developing!

## Getting Help

- **GitHub Issues**: [https://github.com/ujwaltheja/BrowseSnap/issues](https://github.com/ujwaltheja/BrowseSnap/issues)
- **Documentation**: Check the `design/` folder for detailed guides
- **Logs**: Always check Logcat output when debugging

---

Happy coding! 🚀
