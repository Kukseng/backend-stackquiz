#!/bin/bash

echo "========================================="
echo "StackQuiz Backend - Build Verification"
echo "========================================="
echo ""

# Check Java version
echo "1. Checking Java version..."
java -version 2>&1 | head -1
echo ""

# Set Java 17
export JAVA_HOME=/usr/lib/jvm/java-17-openjdk-amd64
export PATH=$JAVA_HOME/bin:$PATH

# Clean build
echo "2. Running clean build..."
./gradlew clean build -q
BUILD_STATUS=$?

if [ $BUILD_STATUS -eq 0 ]; then
    echo "✅ BUILD SUCCESSFUL"
else
    echo "❌ BUILD FAILED"
    exit 1
fi

echo ""
echo "3. Checking build artifacts..."
if [ -f "build/libs/stackquiz-api-0.0.1-SNAPSHOT.jar" ]; then
    echo "✅ JAR file created: $(ls -lh build/libs/*.jar | awk '{print $9, $5}')"
else
    echo "❌ JAR file not found"
fi

echo ""
echo "========================================="
echo "Verification Complete!"
echo "========================================="
