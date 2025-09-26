#!/bin/bash

set -e -x

CACHEDIR="$HOME/.cache/runelite"
mkdir -p "${CACHEDIR}"
GLSLANG_ARCHIVE="${CACHEDIR}/glslang.zip"
GLSLANG_DIR="${CACHEDIR}/glslang"
GLSLANG_RELEASE='https://github.com/KhronosGroup/glslang/releases/download/8.13.3743/glslang-master-osx-Release.zip'
GLSLANG_CHECKSUM='7c75f198c0172128ac22338351bda716e1a8779b0f83cfe1f81c0c56e6cce8cb'

if [ ! -f "${GLSLANG_ARCHIVE}" ] || [ ! -d "${GLSLANG_DIR}" ]; then
  wget -O "${GLSLANG_ARCHIVE}" "${GLSLANG_RELEASE}"
  # Verify checksum
  ACTUAL_CHECKSUM=$(sha256sum "${GLSLANG_ARCHIVE}" | cut -d' ' -f1)
  if [ "${ACTUAL_CHECKSUM}" != "${GLSLANG_CHECKSUM}" ]; then
    echo "Checksum verification failed!"
    exit 1
  fi
  unzip -o -q "${GLSLANG_ARCHIVE}" -d "${GLSLANG_DIR}"
fi

# Navigate to project root
SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
PROJECT_ROOT="$(dirname "$SCRIPT_DIR")"
cd "$PROJECT_ROOT"

# Build the entire project
mvn package -DskipTests=true -Dmaven.test.skip=true

# Rename JAR to match official RuneLite naming convention
MICROBOT_JAR="runelite-client/target/microbot-2.0.13.jar"
RUNELITE_JAR="runelite-client/target/RuneLite.jar"

if [ -f "$MICROBOT_JAR" ]; then
    echo "Creating RuneLite-compatible JAR..."
    cp "$MICROBOT_JAR" "$RUNELITE_JAR"
    echo "Created: $RUNELITE_JAR"
fi

# macOS specific: Create launcher binary with fullscreen fix
if [[ "$OSTYPE" == "darwin"* ]]; then
    echo "Building macOS launcher with fullscreen fix..."

    # Create a temporary C launcher source
    cat > /tmp/runelite_launcher.c << 'EOF'
#include <stdio.h>
#include <stdlib.h>
#include <string.h>
#include <unistd.h>
#include <libgen.h>

int main(int argc, char *argv[]) {
    char exe_path[1024];
    char jar_path[1024];

    if (realpath(argv[0], exe_path) == NULL) {
        fprintf(stderr, "Failed to get executable path\n");
        return 1;
    }

    char *exe_dir = dirname(exe_path);
    snprintf(jar_path, sizeof(jar_path), "%s/../Resources/RuneLite.jar", exe_dir);

    if (access(jar_path, F_OK) == -1) {
        fprintf(stderr, "Error: RuneLite.jar not found at %s\n", jar_path);
        return 1;
    }

    int java_argc = 4 + argc - 1;
    char **java_argv = malloc(sizeof(char*) * (java_argc + 1));

    if (!java_argv) {
        fprintf(stderr, "Memory allocation failed\n");
        return 1;
    }

    int idx = 0;
    java_argv[idx++] = "java";
    java_argv[idx++] = "--add-exports";
    java_argv[idx++] = "java.desktop/com.apple.eawt=ALL-UNNAMED";
    java_argv[idx++] = "-jar";
    java_argv[idx++] = jar_path;

    for (int i = 1; i < argc; i++) {
        java_argv[idx++] = argv[i];
    }
    java_argv[idx] = NULL;

    execvp("java", java_argv);
    perror("execvp failed");
    free(java_argv);
    return 1;
}
EOF

    # Create artifacts directory if it doesn't exist
    mkdir -p ./artifacts

    # Compile the launcher
    gcc -o ./artifacts/runelite_launcher_macos /tmp/runelite_launcher.c

    echo "macOS launcher binary created successfully"
fi

echo "Build completed successfully!"
echo "Generated files:"
echo "  - $RUNELITE_JAR (Microbot client disguised as RuneLite)"
if [[ "$OSTYPE" == "darwin"* ]]; then
    echo "  - ./artifacts/runelite_launcher_macos (Native launcher)"
fi