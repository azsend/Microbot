#!/bin/bash

# Local development script for Microbot
# Quick build and test without deployment

set -e -x

echo "🔧 Local Microbot development build..."

# Navigate to project root
SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
PROJECT_ROOT="$(dirname "$SCRIPT_DIR")"
cd "$PROJECT_ROOT"

# Clean previous builds
echo "Cleaning previous builds..."
mvn clean -q

# Build EthanVann plugins if they exist
if [ -d "EthanVannPlugins" ]; then
    echo "Building EthanVann plugins..."
    cd EthanVannPlugins
    if [ -f "./gradlew" ]; then
        chmod a+x ./gradlew
        ./gradlew build publishToMavenLocal
    fi
    cd ..
fi

# Build Microbot with optimized settings for development
echo "Building Microbot..."
mvn package -DskipTests=true -Dlombok.skip=true -q

# Create RuneLite-compatible JAR for testing
MICROBOT_JAR="runelite-client/target/microbot-2.0.13.jar"
RUNELITE_JAR="runelite-client/target/RuneLite.jar"

if [ -f "$MICROBOT_JAR" ]; then
    echo "Creating RuneLite-compatible JAR for testing..."
    cp "$MICROBOT_JAR" "$RUNELITE_JAR"
    
    echo "✅ Local build completed successfully!"
    echo ""
    echo "Generated files:"
    echo "  📦 $MICROBOT_JAR (Original Microbot JAR)"
    echo "  🎭 $RUNELITE_JAR (RuneLite-compatible JAR)"
    echo ""
    echo "🚀 Test locally with:"
    echo "   java -jar $RUNELITE_JAR"
    echo ""
    echo "📊 Verify checksums with:"
    echo "   ./ci/verify-checksum.sh"
    echo ""
    echo "🔧 Deploy to RuneLite with:"
    echo "   ./ci/deploy-macos.sh"
else
    echo "❌ Build failed - JAR not found"
    exit 1
fi
