# BrowseSnap - Mobile TV Browser Control System

BrowseSnap is a two-part Android application system that allows you to control web browsing and video playback on your Android TV from your mobile device via WebSocket communication.

## Features

### Mobile App
- **QR Code Pairing**: Quickly pair with your TV by scanning a QR code
- **PIN Pairing**: Manual pairing option with IP address and PIN
- **Search & Browse**: Search for content and send URLs to your TV
- **History Tracking**: Keep track of recently accessed URLs and videos
- **Remote Control**: Control TV browsing with navigation and playback commands
- **Paired Device Management**: Manage multiple paired TVs

### TV App
- **WebSocket Server**: Runs a secure WebSocket server for receiving commands
- **Web Browser**: Full-featured WebView for browsing websites
- **Video Player**: ExoPlayer integration for smooth video playback
- **QR Code Generation**: Display QR code for easy pairing
- **Command Execution**: Process and execute commands from mobile devices

## Architecture

```
┌─────────────────────┐
│   Mobile App        │
│  (Jetpack Compose)  │
│                     │
│  • Search Bar       │
│  • History List     │
│  • Remote Control   │
│  • Pairing UI       │
└──────────┬──────────┘
           │
           │ WebSocket (JSON Commands)
           │ ws://TV_IP:8888
           │
┌──────────▼──────────┐
│   TV App            │
│  (WebSocket Server) │
│                     │
│  • WebView Browser  │
│  • Video Player     │
│  • Command Handler  │
│  • QR Generation    │
└─────────────────────┘
```

## Project Structure

```
BrowseSnap/
├── app/                    # Legacy module (can be ignored)
├── core/                   # Shared core library
│   └── src/main/kotlin/com/tvbrowser/core/
│       ├── domain/models/         # Command & Response models
│       ├── network/               # WebSocket client/server
│       └── util/                  # Extension functions
├── mobile/                 # Mobile application
│   └── src/main/kotlin/com/tvbrowser/mobile/
│       ├── data/
│       │   ├── entity/            # Room database entities
│       │   ├── dao/               # Data Access Objects
│       │   ├── database/          # Room database
│       │   └── repository/        # Repository layer
│       ├── di/                    # Dependency injection
│       ├── ui/
│       │   ├── screens/           # Compose screens
│       │   └── theme/             # Material theme
│       └── viewmodel/             # ViewModels
└── tv/                     # TV application
    └── src/main/kotlin/com/tvbrowser/tv/
        ├── server/                # WebSocket server
        ├── ui/
        │   ├── screens/           # Compose screens for TV
        │   └── theme/             # Material theme
        └── viewmodel/             # ViewModels
```

## Technology Stack

### Mobile App
- **Language**: Kotlin
- **UI**: Jetpack Compose (Material 3)
- **Architecture**: MVVM with Repository pattern
- **Database**: Room
- **Networking**: OkHttp WebSocket
- **QR Scanning**: ZXing + ML Kit
- **DI**: Manual dependency injection
- **Logging**: Timber

### TV App
- **Language**: Kotlin
- **UI**: Jetpack Compose for TV
- **Architecture**: MVVM
- **Networking**: Java-WebSocket Server
- **Video Player**: ExoPlayer (Media3)
- **QR Generation**: ZXing

### Core Library
- **Networking**: OkHttp WebSocket client, Java-WebSocket server
- **Serialization**: Kotlin Serialization (JSON)
- **Security**: Token-based authentication, PIN validation

## Setup Instructions

### Prerequisites
- Android Studio Flamingo or later
- JDK 17 or later
- Android SDK API 26+ (Android 8.0) for mobile
- Android SDK API 24+ for TV
- Gradle 8.0+

### Build & Run

1. **Clone the repository**
   ```bash
   git clone https://github.com/ujwaltheja/BrowseSnap.git
   cd BrowseSnap
   ```

2. **Open in Android Studio**
   - Open Android Studio
   - Select "Open an Existing Project"
   - Navigate to the BrowseSnap directory
   - Wait for Gradle sync to complete

3. **Build the project**
   ```bash
   ./gradlew build
   ```

4. **Run the Mobile App**
   - Select `mobile` configuration
   - Choose your Android device or emulator
   - Click Run

5. **Run the TV App**
   - Select `tv` configuration
   - Choose your Android TV device or TV emulator
   - Click Run

### Configuration

#### Mobile App
No special configuration needed. The app will automatically discover paired TVs and store them locally.

#### TV App
- The WebSocket server runs on port **8888** by default
- Ensure your firewall allows connections on this port
- Both devices must be on the same network

## Usage

### Pairing Process

1. **On TV App**:
   - Launch the TV app
   - Navigate to the Pairing screen
   - Note the QR code and/or 4-digit PIN displayed
   - Note the IP address shown

2. **On Mobile App**:
   - Launch the Mobile app
   - Tap "Pair with TV"
   - Either:
     - Scan the QR code displayed on TV
     - Or enter the IP address and PIN manually
   - Wait for connection confirmation

### Sending Content to TV

1. **Search Method**:
   - Open the Search screen on mobile
   - Enter your search query or URL
   - Tap the result to send to TV

2. **History Method**:
   - View recent URLs on the Home screen
   - Tap any URL to resend to TV

3. **Remote Control**:
   - Use the Remote Control screen for:
     - Back/Forward navigation
     - Play/Pause/Stop controls
     - Volume adjustment

## Commands

The system supports the following commands:

| Command | Description | Mobile → TV |
|---------|-------------|-------------|
| `OpenUrl` | Open a URL in WebView | ✓ |
| `PlayVideo` | Play video in ExoPlayer | ✓ |
| `NavigateBack` | Navigate back in browser | ✓ |
| `NavigateForward` | Navigate forward in browser | ✓ |
| `Pause` | Pause video playback | ✓ |
| `Resume` | Resume video playback | ✓ |
| `Stop` | Stop video playback | ✓ |
| `SetVolume` | Adjust volume | ✓ |
| `Seek` | Seek to position in video | ✓ |
| `Register` | Register mobile device | ✓ |

## Database Schema

### Mobile App

#### `browsing_history` table
| Column | Type | Description |
|--------|------|-------------|
| id | INTEGER | Primary key (auto-increment) |
| url | TEXT | The URL accessed |
| title | TEXT | Page title (if available) |
| action | TEXT | Action type (open_url, play_video) |
| timestamp | INTEGER | Unix timestamp |
| thumbnailUrl | TEXT | Thumbnail URL (optional) |
| deviceId | TEXT | TV device ID (optional) |

#### `paired_tvs` table
| Column | Type | Description |
|--------|------|-------------|
| deviceId | TEXT | Primary key, unique device ID |
| deviceName | TEXT | Friendly name for the TV |
| ipAddress | TEXT | TV IP address |
| port | INTEGER | WebSocket port (default: 8888) |
| pin | TEXT | Pairing PIN (optional) |
| lastConnected | INTEGER | Last connection timestamp |
| createdAt | INTEGER | Creation timestamp |

## Development

### Adding New Commands

1. **Define the command in `core/domain/models/Command.kt`**:
   ```kotlin
   @Serializable
   data class MyNewCommand(val param: String) : TVCommand()
   ```

2. **Handle it in `TVViewModel` on TV**:
   ```kotlin
   is TVCommand.MyNewCommand -> {
       // Handle the command
   }
   ```

3. **Send it from Mobile**:
   ```kotlin
   viewModel.sendCommand(TVCommand.MyNewCommand("value"))
   ```

### Running Tests

```bash
# Unit tests
./gradlew test

# Instrumented tests (requires device/emulator)
./gradlew connectedAndroidTest
```

### Code Style

This project follows the [Kotlin Coding Conventions](https://kotlinlang.org/docs/coding-conventions.html).

## Security Considerations

- **Local Network Only**: WebSocket connections are designed for local network use
- **No Internet Exposure**: Do not expose the TV's WebSocket port to the internet
- **PIN Protection**: Use PIN-based pairing for additional security
- **Token Authentication**: Auth tokens are supported for additional security

## Troubleshooting

### Mobile app can't find TV
- Ensure both devices are on the same Wi-Fi network
- Check that the TV's WebSocket server is running
- Verify the TV's IP address is correct
- Check firewall settings on TV

### Connection drops frequently
- Check Wi-Fi signal strength
- Ensure TV is not going into sleep mode
- Verify network stability

### QR code won't scan
- Ensure adequate lighting
- Camera permissions are granted on mobile
- Try manual PIN entry instead

### Videos won't play
- Check video URL is accessible
- Verify video format is supported by ExoPlayer
- Check network bandwidth

## Known Limitations

- Requires both devices on the same local network
- No cloud synchronization of history
- No multi-user support
- No SSL/TLS encryption (local network only)

## Future Enhancements

- [ ] Cloud synchronization
- [ ] User accounts and profiles
- [ ] Bookmarks and favorites
- [ ] Voice control integration
- [ ] Ad blocking
- [ ] Download management
- [ ] Multi-TV simultaneous control
- [ ] Network service discovery (mDNS)

## Contributing

Contributions are welcome! Please follow these steps:

1. Fork the repository
2. Create a feature branch (`git checkout -b feature/AmazingFeature`)
3. Commit your changes (`git commit -m 'Add some AmazingFeature'`)
4. Push to the branch (`git push origin feature/AmazingFeature`)
5. Open a Pull Request

## License

This project is licensed under the MIT License - see the LICENSE file for details.

## Authors

- Ujwal Theja - Initial work

## Acknowledgments

- Android Jetpack team for excellent libraries
- OkHttp team for reliable networking
- ZXing for QR code functionality
- ExoPlayer team for video playback

---

**Built with ❤️ using Kotlin and Jetpack Compose**
