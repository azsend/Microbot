#!/bin/bash

# RuneLite Update Script for Microbot
# This script safely updates the RuneLite codebase while preserving custom Microbot files

set -e  # Exit on any error

echo "🔄 Starting RuneLite update process for Microbot..."

# Navigate to project root
SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
PROJECT_ROOT="$(dirname "$SCRIPT_DIR")"
cd "$PROJECT_ROOT"

# Colors for output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m' # No Color

# Function to print colored output
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

# Check if we're on main branch
CURRENT_BRANCH=$(git branch --show-current)
if [ "$CURRENT_BRANCH" != "main" ]; then
    print_error "Not on main branch. Please switch to main branch first."
    exit 1
fi

# Check for uncommitted changes
if [ -n "$(git status --porcelain)" ]; then
    print_warning "You have uncommitted changes. Please commit or stash them first."
    git status --short
    exit 1
fi

print_status "Fetching latest changes from upstream RuneLite..."
git fetch upstream || {
    print_error "Failed to fetch from upstream. Please check your remote configuration."
    print_status "To add upstream: git remote add upstream https://github.com/runelite/runelite.git"
    exit 1
}

print_status "Checking for updates..."
LOCAL_COMMIT=$(git rev-parse HEAD)
UPSTREAM_COMMIT=$(git rev-parse upstream/master)

if [ "$LOCAL_COMMIT" = "$UPSTREAM_COMMIT" ]; then
    print_success "Already up to date with upstream RuneLite!"
    exit 0
fi

print_status "Updates available. Creating backup branch..."
BACKUP_BRANCH="backup-$(date +%Y%m%d-%H%M%S)"
git checkout -b "$BACKUP_BRANCH"
git checkout main

print_status "Merging upstream changes..."
if git merge upstream/master --no-edit --strategy=recursive -X ours; then
    print_success "Merge completed successfully!"
else
    print_warning "Merge conflicts detected. This is expected for custom files."
    print_status "Checking merge status..."

    # Check if there are any merge conflicts in our custom files
    CONFLICTS=$(git status --porcelain | grep -E "^(UU|AA)" | grep -E "(microbot|plugins/microbot)" || true)

    if [ -n "$CONFLICTS" ]; then
        print_warning "Conflicts detected in Microbot files:"
        echo "$CONFLICTS"

        print_status "Resolving conflicts by keeping our versions..."
        for file in $(git status --porcelain | grep -E "^(UU|AA)" | grep -E "(microbot|plugins/microbot)" | awk '{print $2}'); do
            if [ -f "$file" ]; then
                git checkout --ours "$file"
                git add "$file"
                print_status "Resolved: $file (kept our version)"
            fi
        done
    fi

    # Complete the merge
    if git commit --no-edit; then
        print_success "Merge completed with conflict resolution!"
    else
        print_error "Failed to complete merge. Please resolve conflicts manually."
        exit 1
    fi
fi

print_success "RuneLite update completed successfully!"
print_status "Backup branch created: $BACKUP_BRANCH"
print_status "You can delete it later with: git branch -D $BACKUP_BRANCH"

print_status "Next steps:"
echo "  1. Test your build: ./ci/build.sh"
echo "  2. Test your application functionality"
echo "  3. If everything works, push to origin: git push origin main"
echo "  4. Clean up backup: git branch -D $BACKUP_BRANCH"
echo "  5. Deploy to RuneLite: ./ci/deploy-macos.sh (macOS only)"
