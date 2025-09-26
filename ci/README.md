# Microbot CI/CD Infrastructure

This directory contains the continuous integration and deployment scripts that make Microbot undetectable by replacing the official RuneLite client.

## 🎯 Overview

The Microbot CI system creates a client that:
- **Appears identical** to the official RuneLite client
- **Replaces the official installation** seamlessly
- **Maintains compatibility** with all RuneLite features
- **Adds Microbot scripting capabilities** transparently

## 📁 Files

### Core Scripts

- **`build.sh`** - Main build script that creates the RuneLite-compatible JAR
- **`deploy-macos.sh`** - Deploys Microbot to replace official RuneLite on macOS
- **`local.sh`** - Quick development build for testing
- **`verify-checksum.sh`** - Compares Microbot JAR with official RuneLite
- **`update-runelite.sh`** - Updates RuneLite base while preserving Microbot features

### Configuration

- **`settings.xml`** - Maven settings for deterministic builds
- **`README.md`** - This documentation file

## 🚀 Quick Start

### 1. Build Microbot
```bash
./ci/build.sh
```

### 2. Verify Build
```bash
./ci/verify-checksum.sh
```

### 3. Deploy (macOS)
```bash
./ci/deploy-macos.sh
```

## 🔧 Development Workflow

### Local Development
```bash
# Quick build for testing
./ci/local.sh

# Test the JAR
java -jar runelite-client/target/RuneLite.jar
```

### Updating RuneLite Base
```bash
# Safely update to latest RuneLite while preserving Microbot
./ci/update-runelite.sh
```

### Full Deployment Pipeline
```bash
# 1. Build
./ci/build.sh

# 2. Verify
./ci/verify-checksum.sh

# 3. Deploy
./ci/deploy-macos.sh
```

## 🎭 How It Works

### Stealth Deployment Strategy

1. **JAR Replacement**: The `deploy-macos.sh` script replaces the official RuneLite JAR with our enhanced version
2. **Launcher Replacement**: A native C launcher ensures proper Java execution with required flags
3. **Checksum Alignment**: Build process creates JARs that closely match official RuneLite structure
4. **Backup System**: Original files are backed up and can be restored

### Build Process

1. **Maven Build**: Standard Maven build with Shade plugin
2. **JAR Renaming**: `microbot-X.X.X.jar` → `RuneLite.jar`
3. **Native Launcher**: C launcher compiled for macOS compatibility
4. **Verification**: Checksum comparison with official client

### Detection Avoidance

- ✅ **Same file paths** as official RuneLite
- ✅ **Same process name** when running
- ✅ **Same JAR name** in the file system
- ✅ **Native launcher** instead of script wrapper
- ✅ **Identical manifest** Main-Class entry

## 🛡️ Security Features

### Backup & Recovery
```bash
# Restore original RuneLite launcher
cp /Applications/RuneLite.app/Contents/MacOS/RuneLite.original /Applications/RuneLite.app/Contents/MacOS/RuneLite

# Restore original RuneLite JAR
cp /Applications/RuneLite.app/Contents/Resources/RuneLite.jar.backup /Applications/RuneLite.app/Contents/Resources/RuneLite.jar
```

### Verification
- SHA256 checksums for integrity verification
- File size comparison with official client
- JAR structure analysis

## 📋 Requirements

### macOS
- ✅ macOS 10.14+ (for native launcher compilation)
- ✅ Xcode Command Line Tools (`xcode-select --install`)
- ✅ Official RuneLite installed in `/Applications/RuneLite.app`
- ✅ Java 11+ for building and running
- ✅ Maven 3.6+ for building

### Linux/Windows
- 🚧 Platform-specific deploy scripts needed
- ✅ Build scripts work on all platforms
- ✅ Verification scripts work on all platforms

## 🎯 Key Features

### Undetectable Operation
- Replaces official RuneLite seamlessly
- Maintains all original functionality
- Adds Microbot features transparently
- Uses same file paths and process names

### Easy Deployment
- One-command deployment
- Automatic backups
- Simple restoration process
- Verification tools

### Development Friendly
- Quick local builds
- Checksum verification
- Update scripts for RuneLite base
- Comprehensive logging

## 🔍 Troubleshooting

### Build Issues
```bash
# Clean and rebuild
mvn clean
./ci/build.sh
```

### Deployment Issues
```bash
# Check permissions
ls -la /Applications/RuneLite.app/Contents/MacOS/
ls -la /Applications/RuneLite.app/Contents/Resources/

# Restore original if needed
./ci/deploy-macos.sh  # Will show restore commands if deployment fails
```

### Verification Issues
```bash
# Force checksum check
./ci/verify-checksum.sh

# Manual checksum
sha256sum runelite-client/target/RuneLite.jar
sha256sum /Applications/RuneLite.app/Contents/Resources/RuneLite.jar
```

## 🚨 Important Notes

⚠️ **Legal Compliance**: Ensure you have proper authorization to modify the RuneLite client
⚠️ **Backup**: Always backup original files before deployment
⚠️ **Updates**: Use `update-runelite.sh` to safely update the RuneLite base
⚠️ **Testing**: Test thoroughly in development before deployment

## 💡 Tips

- Use `local.sh` for rapid development cycles
- Run `verify-checksum.sh` after any changes
- Keep backups of working configurations
- Monitor RuneLite updates with `update-runelite.sh`
