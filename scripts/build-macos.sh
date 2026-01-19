#!/bin/bash
set -e

# InventoryFlow macOS Build Script
# Creates a signed and notarized .dmg installer

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
PROJECT_DIR="$(dirname "$SCRIPT_DIR")"
APP_NAME="InventoryFlow"
APP_VERSION="1.0.0"

# Colors for output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
NC='\033[0m' # No Color

echo -e "${GREEN}========================================${NC}"
echo -e "${GREEN}  Building $APP_NAME for macOS${NC}"
echo -e "${GREEN}========================================${NC}"

cd "$PROJECT_DIR"

# Load signing configuration if available
if [ -f "$SCRIPT_DIR/signing-config.sh" ]; then
    echo -e "\n${GREEN}Loading signing configuration...${NC}"
    source "$SCRIPT_DIR/signing-config.sh"
fi

# Check for required tools
echo -e "\n${YELLOW}Checking prerequisites...${NC}"

if ! command -v java &> /dev/null; then
    echo -e "${RED}Error: Java not found. Install JDK 17+${NC}"
    exit 1
fi

JAVA_VERSION=$(java -version 2>&1 | head -n 1 | cut -d'"' -f2 | cut -d'.' -f1)
if [ "$JAVA_VERSION" -lt 17 ]; then
    echo -e "${RED}Error: Java 17+ required, found Java $JAVA_VERSION${NC}"
    exit 1
fi
echo "  Java $JAVA_VERSION ✓"

if ! command -v mvn &> /dev/null; then
    echo -e "${RED}Error: Maven not found. Install with: brew install maven${NC}"
    exit 1
fi
echo "  Maven ✓"

if ! command -v jpackage &> /dev/null; then
    echo -e "${RED}Error: jpackage not found. Ensure JDK 17+ is properly installed${NC}"
    exit 1
fi
echo "  jpackage ✓"

# Check for icon
ICON_PATH="$PROJECT_DIR/packaging/macos/InventoryFlow.icns"
if [ ! -f "$ICON_PATH" ]; then
    echo -e "${YELLOW}Warning: No icon found at $ICON_PATH${NC}"
    echo -e "${YELLOW}Building without custom icon...${NC}"
    ICON_OPTION=""
else
    echo "  Icon ✓"
    ICON_OPTION="--icon $ICON_PATH"
fi

# Clean and build
echo -e "\n${YELLOW}Building application...${NC}"
mvn clean package -DskipTests

# Create installer directory
INSTALLER_DIR="$PROJECT_DIR/target/installer"
mkdir -p "$INSTALLER_DIR"

# Copy main JAR to lib directory for unified input
cp "$PROJECT_DIR/target/inventoryflow-$APP_VERSION.jar" "$PROJECT_DIR/target/lib/"

# Code signing configuration
# Set these environment variables for signed builds:
#   MACOS_SIGNING_IDENTITY - Your Developer ID Application certificate name
#   APPLE_ID - Your Apple ID email
#   APPLE_APP_PASSWORD - App-specific password for notarization
#   APPLE_TEAM_ID - Your Apple Developer Team ID

SIGN_OPTIONS=""
if [ -n "$MACOS_SIGNING_IDENTITY" ]; then
    echo -e "\n${GREEN}Code signing enabled${NC}"
    SIGN_OPTIONS="--mac-sign --mac-signing-key-user-name \"$MACOS_SIGNING_IDENTITY\""
else
    echo -e "\n${YELLOW}Code signing disabled (MACOS_SIGNING_IDENTITY not set)${NC}"
    echo "For distribution, set up code signing - see docs/CODE_SIGNING.md"
fi

# Run jpackage in classpath mode (avoids jlink issues with automatic modules)
echo -e "\n${YELLOW}Creating .dmg installer...${NC}"

jpackage \
    --type dmg \
    --name "$APP_NAME" \
    --app-version "$APP_VERSION" \
    --vendor "Valley Technology Partners, LLC" \
    --copyright "Copyright 2024 Valley Technology Partners, LLC" \
    --description "Shopify Inventory Management" \
    --input "$PROJECT_DIR/target/lib" \
    --main-jar "inventoryflow-$APP_VERSION.jar" \
    --main-class com.inventoryflow.App \
    --dest "$INSTALLER_DIR" \
    --mac-package-name "$APP_NAME" \
    --mac-package-identifier "com.inventoryflow" \
    --java-options "-Dfile.encoding=UTF-8" \
    --java-options "-Dapple.awt.application.appearance=system" \
    $ICON_OPTION \
    $SIGN_OPTIONS

# Notarization (if credentials are set)
DMG_FILE="$INSTALLER_DIR/$APP_NAME-$APP_VERSION.dmg"

if [ -f "$DMG_FILE" ] && [ -n "$APPLE_ID" ] && [ -n "$APPLE_APP_PASSWORD" ] && [ -n "$APPLE_TEAM_ID" ]; then
    echo -e "\n${YELLOW}Notarizing application...${NC}"

    xcrun notarytool submit "$DMG_FILE" \
        --apple-id "$APPLE_ID" \
        --password "$APPLE_APP_PASSWORD" \
        --team-id "$APPLE_TEAM_ID" \
        --wait

    echo -e "\n${YELLOW}Stapling notarization ticket...${NC}"
    xcrun stapler staple "$DMG_FILE"

    echo -e "\n${GREEN}Notarization complete!${NC}"
else
    if [ -f "$DMG_FILE" ]; then
        echo -e "\n${YELLOW}Skipping notarization (credentials not set)${NC}"
    fi
fi

# Summary
echo -e "\n${GREEN}========================================${NC}"
echo -e "${GREEN}  Build Complete!${NC}"
echo -e "${GREEN}========================================${NC}"

if [ -f "$DMG_FILE" ]; then
    echo -e "\nInstaller: ${GREEN}$DMG_FILE${NC}"
    echo -e "Size: $(du -h "$DMG_FILE" | cut -f1)"
else
    echo -e "\n${RED}Error: DMG file not created${NC}"
    exit 1
fi
