#!/bin/bash

# Checksum Verification Script for Microbot RuneLite Compatibility
# This script helps verify that our JAR closely matches the official RuneLite client

set -e

# Colors for output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m' # No Color

print_status() {
    echo -e "${BLUE}[INFO]${NC} $1"
}

print_warning() {
    echo -e "${YELLOW}[WARNING]${NC} $1"
}

print_error() {
    echo -e "${RED}[ERROR]${NC} $1"
}

print_success() {
    echo -e "${GREEN}[SUCCESS]${NC} $1"
}

# Function to format bytes (compatible with macOS)
format_bytes() {
    local bytes=$1
    if [ $bytes -ge 1073741824 ]; then
        echo "$(($bytes / 1073741824))GB"
    elif [ $bytes -ge 1048576 ]; then
        echo "$(($bytes / 1048576))MB"
    elif [ $bytes -ge 1024 ]; then
        echo "$(($bytes / 1024))KB"
    else
        echo "${bytes}B"
    fi
}

# Navigate to project root
SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
PROJECT_ROOT="$(dirname "$SCRIPT_DIR")"
cd "$PROJECT_ROOT"

MICROBOT_JAR="runelite-client/target/RuneLite.jar"
OFFICIAL_RUNELITE_JAR="/Applications/RuneLite.app/Contents/Resources/RuneLite.jar"

print_status "Microbot RuneLite Checksum Verification"
echo "=========================================="

# Check if our JAR exists
if [ ! -f "$MICROBOT_JAR" ]; then
    print_error "Microbot JAR not found at: $MICROBOT_JAR"
    echo "Please run: ./ci/build.sh"
    exit 1
fi

# Check if official RuneLite JAR exists for comparison
if [ -f "$OFFICIAL_RUNELITE_JAR" ]; then
    print_status "Comparing with official RuneLite JAR..."
    
    MICROBOT_CHECKSUM=$(sha256sum "$MICROBOT_JAR" | cut -d' ' -f1)
    OFFICIAL_CHECKSUM=$(sha256sum "$OFFICIAL_RUNELITE_JAR" | cut -d' ' -f1)
    
    echo ""
    echo "Microbot JAR:  $MICROBOT_CHECKSUM"
    echo "Official JAR:  $OFFICIAL_CHECKSUM"
    echo ""
    
    if [ "$MICROBOT_CHECKSUM" = "$OFFICIAL_CHECKSUM" ]; then
        print_success "🎯 PERFECT MATCH! Checksums are identical."
        print_success "Microbot is fully undetectable as official RuneLite."
    else
        print_warning "Checksums differ - this is expected for a modified client."
        print_status "Microbot includes additional botting features not in official RuneLite."
    fi
    
    # File size comparison
    MICROBOT_SIZE=$(stat -f%z "$MICROBOT_JAR" 2>/dev/null || stat -c%s "$MICROBOT_JAR")
    OFFICIAL_SIZE=$(stat -f%z "$OFFICIAL_RUNELITE_JAR" 2>/dev/null || stat -c%s "$OFFICIAL_RUNELITE_JAR")
    
    echo ""
    echo "File Sizes:"
    echo "Microbot:  $(format_bytes $MICROBOT_SIZE) ($MICROBOT_SIZE bytes)"
    echo "Official:  $(format_bytes $OFFICIAL_SIZE) ($OFFICIAL_SIZE bytes)"
    
    if [ "$MICROBOT_SIZE" -gt "$OFFICIAL_SIZE" ]; then
        SIZE_DIFF=$((MICROBOT_SIZE - OFFICIAL_SIZE))
        echo "Difference: +$(format_bytes $SIZE_DIFF) (+$SIZE_DIFF bytes)"
        print_status "Microbot is larger due to additional features."
    elif [ "$MICROBOT_SIZE" -lt "$OFFICIAL_SIZE" ]; then
        SIZE_DIFF=$((OFFICIAL_SIZE - MICROBOT_SIZE))
        echo "Difference: -$(format_bytes $SIZE_DIFF) (-$SIZE_DIFF bytes)"
    else
        print_success "File sizes are identical!"
    fi
    
else
    print_warning "Official RuneLite JAR not found at: $OFFICIAL_RUNELITE_JAR"
    print_status "Install RuneLite first to enable comparison."
    
    MICROBOT_CHECKSUM=$(sha256sum "$MICROBOT_JAR" | cut -d' ' -f1)
    echo ""
    echo "Microbot JAR checksum: $MICROBOT_CHECKSUM"
fi

# JAR structure analysis
print_status "Analyzing JAR structure..."
echo ""

# Count classes
MICROBOT_CLASSES=$(unzip -l "$MICROBOT_JAR" | grep -c "\.class$" || echo "0")
echo "Class files: $MICROBOT_CLASSES"

# Count Microbot-specific files
MICROBOT_SPECIFIC=$(unzip -l "$MICROBOT_JAR" | grep -c "microbot" || echo "0")
echo "Microbot files: $MICROBOT_SPECIFIC"

# Check for main class
MAIN_CLASS=$(unzip -p "$MICROBOT_JAR" META-INF/MANIFEST.MF | grep "Main-Class:" | cut -d' ' -f2 | tr -d '\r')
echo "Main class: $MAIN_CLASS"

echo ""
print_status "Verification complete!"

if [ -f "$OFFICIAL_RUNELITE_JAR" ]; then
    echo ""
    print_status "Deployment readiness:"
    echo "✅ Microbot JAR built successfully"
    echo "✅ Official RuneLite detected for replacement"
    echo "🚀 Ready to deploy with: ./ci/deploy-macos.sh"
fi
