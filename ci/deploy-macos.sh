#!/bin/bash

# Deploy script for macOS RuneLite with Microbot integration
# This script replaces the official RuneLite installation with Microbot

set -e

echo "Deploying Microbot as RuneLite replacement..."

# Check if we're on macOS
if [[ "$OSTYPE" != "darwin"* ]]; then
    echo "This script is only for macOS"
    exit 1
fi

# Check if the launcher binary was created
if [ ! -f "../artifacts/runelite_launcher_macos" ]; then
    echo "Error: runelite_launcher_macos not found. Run build.sh first."
    exit 1
fi

# Check if the RuneLite JAR was created
RUNELITE_JAR="../runelite-client/target/RuneLite.jar"
if [ ! -f "$RUNELITE_JAR" ]; then
    echo "Error: RuneLite.jar not found at $RUNELITE_JAR. Run build.sh first."
    exit 1
fi

# Find the RuneLite app
RUNELITE_APP=""
for app in "/Applications/RuneLite.app"; do
    if [ -d "$app" ]; then
        RUNELITE_APP="$app"
        break
    fi
done

if [ -z "$RUNELITE_APP" ]; then
    echo "Error: RuneLite.app not found in /Applications/"
    echo "Please install RuneLite first from: https://runelite.net"
    exit 1
fi

echo "Found RuneLite app: $RUNELITE_APP"

# Backup the original launcher
if [ -f "$RUNELITE_APP/Contents/MacOS/RuneLite" ]; then
    echo "Backing up original RuneLite launcher..."
    cp "$RUNELITE_APP/Contents/MacOS/RuneLite" "$RUNELITE_APP/Contents/MacOS/RuneLite.original" 2>/dev/null || {
        echo "Note: Could not backup launcher (already backed up or permission issue)"
    }
fi

# Backup the original JAR
if [ -f "$RUNELITE_APP/Contents/Resources/RuneLite.jar" ]; then
    echo "Backing up original RuneLite JAR..."
    cp "$RUNELITE_APP/Contents/Resources/RuneLite.jar" "$RUNELITE_APP/Contents/Resources/RuneLite.jar.backup" 2>/dev/null || {
        echo "Note: Could not backup JAR (already backed up or permission issue)"
    }
fi

# Copy our launcher
echo "Installing Microbot launcher..."
cp ../artifacts/runelite_launcher_macos "$RUNELITE_APP/Contents/MacOS/RuneLite"
chmod +x "$RUNELITE_APP/Contents/MacOS/RuneLite"

# Copy our JAR
echo "Installing Microbot client JAR..."
cp "$RUNELITE_JAR" "$RUNELITE_APP/Contents/Resources/RuneLite.jar"

# Verify the installation
echo "Verifying installation..."
if [ -x "$RUNELITE_APP/Contents/MacOS/RuneLite" ] && [ -f "$RUNELITE_APP/Contents/Resources/RuneLite.jar" ]; then
    echo "✅ Microbot deployment successful!"
    echo ""
    echo "🎯 RuneLite will now launch with:"
    echo "  - Microbot scripting framework"
    echo "  - Rs2* utility classes for automation"
    echo "  - Enhanced game interaction capabilities"
    echo "  - Fullscreen compatibility fix"
    echo ""
    echo "⚠️  To restore original RuneLite:"
    echo "  - Launcher: cp '$RUNELITE_APP/Contents/MacOS/RuneLite.original' '$RUNELITE_APP/Contents/MacOS/RuneLite'"
    echo "  - JAR: cp '$RUNELITE_APP/Contents/Resources/RuneLite.jar.backup' '$RUNELITE_APP/Contents/Resources/RuneLite.jar'"
    echo ""
    echo "🚀 You can now launch RuneLite normally - it will run Microbot instead!"
else
    echo "❌ Deployment failed - please check file permissions"
    exit 1
fi
