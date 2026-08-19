#!/bin/bash
set -e

# This script is hard-coded for my personal development environment. Providing it on GitHub solely as reference for how builds were generated. You will need to set up your own build workflow. An LLM can likely adapt this file for you if you are not sure where to begin.
# -Christopher

echo "Building fabric mod..."
JAVA_HOME=~/.jdks/graalvm-25.1.3+9.1 GRADLE_OPTS="--enable-native-access=ALL-UNNAMED" ./gradlew clean :vulkan:build

echo "Copying to sandbox directory..."
# Use find to locate the exact jar (ignoring -sources.jar and -dev.jar) and copy it
find vulkan/build/libs -name "DistantHorizons-VulkanMod-fabric-*-mc26.1.2.jar" ! -name "*-sources.jar" ! -name "*-dev.jar" -exec cp {} /home/cwright/Projects/sandbox/ \;

echo "Build and copy complete!"
