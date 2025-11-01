# Build Commands

This document provides the commands to build the TV and mobile modules separately in the BrowseSnap project.

## Build Commands

### Build entire module (with tests)
```bash
./gradlew tv:build
./gradlew mobile:build
```

### Build debug APK only
```bash
./gradlew tv:assembleDebug
./gradlew mobile:assembleDebug
```

### Build release APK only
```bash
./gradlew tv:assembleRelease
./gradlew mobile:assembleRelease
```

### Clean and build
```bash
./gradlew tv:clean tv:build
./gradlew mobile:clean mobile:build
```

### Windows batch file
For Windows, you can also use:
```bash
gradlew.bat tv:build
gradlew.bat mobile:build
```

## Additional Useful Commands

### Run tests only
```bash
./gradlew tv:test
./gradlew mobile:test
```

### Install debug build to connected device
```bash
./gradlew tv:installDebug
./gradlew mobile:installDebug
```

### Check dependencies
```bash
./gradlew tv:dependencies
./gradlew mobile:dependencies
```

### Update dependencies
```bash
./gradlew --refresh-dependencies tv:build
./gradlew --refresh-dependencies mobile:build
```

### Check for dependency updates
```bash
./gradlew dependencyUpdates
```

## Quick Reference

| Command | Description |
|---------|-------------|
| `./gradlew tv:build` | Full build of TV module with tests |
| `./gradlew mobile:build` | Full build of mobile module with tests |
| `./gradlew tv:assembleDebug` | Build debug APK for TV |
| `./gradlew mobile:assembleDebug` | Build debug APK for mobile |
| `./gradlew tv:assembleRelease` | Build release APK for TV |
| `./gradlew mobile:assembleRelease` | Build release APK for mobile |
| `./gradlew tv:clean tv:build` | Clean and rebuild TV module |
| `./gradlew mobile:clean mobile:build` | Clean and rebuild mobile module |
| `./gradlew --refresh-dependencies tv:build` | Update dependencies and build TV module |
| `./gradlew --refresh-dependencies mobile:build` | Update dependencies and build mobile module |
| `./gradlew dependencyUpdates` | Check for available dependency updates |
