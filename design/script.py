
# Create a comprehensive file structure for the BrowseSnap project
import json

files_to_create = {
    "root": {
        "settings.gradle.kts": "Root settings",
        "build.gradle.kts": "Root build configuration",
        "gradle.properties": "Gradle properties"
    },
    "core": {
        "build.gradle.kts": "Core module build",
        "domain/models/Command.kt": "Command models",
        "domain/models/Response.kt": "Response models",
        "network/WebSocketClient.kt": "WebSocket client",
        "network/WebSocketServer.kt": "WebSocket server",
        "util/Extensions.kt": "Utility extensions"
    },
    "mobile": {
        "build.gradle.kts": "Mobile build",
        "AndroidManifest.xml": "Mobile manifest",
        "MainActivity.kt": "Main activity",
        "data/entity/BrowsingHistory.kt": "History entity",
        "data/entity/PairedTV.kt": "Paired TV entity",
        "data/dao/BrowsingHistoryDao.kt": "History DAO",
        "data/dao/PairedTVDao.kt": "Paired TV DAO",
        "data/database/AppDatabase.kt": "Room database",
        "data/repository/TVRepository.kt": "TV repository",
        "di/AppModule.kt": "Dependency injection",
        "ui/screens/HomeScreen.kt": "Home screen",
        "ui/screens/PairingScreen.kt": "Pairing screen",
        "ui/screens/SearchScreen.kt": "Search screen",
        "ui/screens/RemoteControlScreen.kt": "Remote control",
        "ui/screens/HistoryScreen.kt": "History screen",
        "ui/theme/Theme.kt": "Material theme",
        "ui/theme/Color.kt": "Colors",
        "ui/theme/Type.kt": "Typography",
        "viewmodel/MainViewModel.kt": "Main ViewModel"
    },
    "tv": {
        "build.gradle.kts": "TV build",
        "AndroidManifest.xml": "TV manifest",
        "TVActivity.kt": "TV main activity",
        "server/TVWebSocketServer.kt": "TV WebSocket server",
        "ui/screens/TVMainScreen.kt": "TV main screen",
        "ui/screens/TVPairingScreen.kt": "TV pairing screen",
        "ui/screens/TVBrowserScreen.kt": "TV browser screen",
        "ui/screens/TVVideoPlayerScreen.kt": "TV video player",
        "ui/theme/TVTheme.kt": "TV theme",
        "viewmodel/TVViewModel.kt": "TV ViewModel"
    }
}

# Display the complete file structure
print("Complete BrowseSnap Project Structure:")
print("=" * 80)
for module, files in files_to_create.items():
    print(f"\n📁 {module.upper()}/")
    if isinstance(files, dict):
        for file_path, description in files.items():
            indent = "  " * (file_path.count("/"))
            filename = file_path.split("/")[-1]
            print(f"{indent}├─ {filename} ({description})")

print("\n" + "=" * 80)
print("Total files to create:", sum(len(f) if isinstance(f, dict) else 1 for f in files_to_create.values()))
