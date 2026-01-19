# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Build and Run Commands

```bash
# Build the project
mvn clean compile

# Run the application
mvn javafx:run

# Run tests
mvn test

# Run a single test class
mvn test -Dtest=ProductTest

# Package as JAR
mvn package

# Build native installer (macOS)
./scripts/build-macos.sh

# Build native installer (Windows)
scripts\build-windows.bat
```

## Architecture Overview

InventoryFlow is a JavaFX 21 desktop application using Maven that manages Shopify inventory through the GraphQL Admin API.

### Core Components

**App.java** - Application entry point and scene manager. Controls navigation between login and dashboard views using `App.setRoot(fxmlName)`.

**Controllers** (`controller/`):
- `LoginController` - Handles 4-digit PIN authentication and initial setup (Shopify credentials configuration)
- `DashboardController` - Manages the product TableView with image loading, SKU filtering, and inventory sync

**Services** (`service/`):
- `ShopifyService` - All Shopify GraphQL API communication. Uses cursor-based pagination to fetch products. Returns `CompletableFuture<List<Product>>` for async operations.

**Database** (`util/DatabaseManager.java`):
- Singleton managing SQLite storage (`inventoryflow.db`)
- Stores hashed PIN (SHA-256) and encrypted Shopify token (AES-GCM)
- Encryption key from `INVENTORYFLOW_SECRET` env var (falls back to default for development)

### Data Flow

1. User authenticates with PIN → `DatabaseManager.validatePin()`
2. Dashboard loads → `ShopifyService.fetchProducts()` via GraphQL
3. Products display in TableView with `FilteredList` for SKU search
4. Sync button triggers full inventory refresh

### Security Considerations

- Shopify access token is encrypted at rest using AES-GCM
- PIN is hashed, not stored in plaintext
- Token is never logged (verify no logging statements reference the token value)
- Set `INVENTORYFLOW_SECRET` environment variable in production

### UI Structure

Views are FXML-based in `resources/fxml/` with a shared dark theme CSS (`resources/css/dark-theme.css`). The product table uses JavaFX properties for data binding with custom cell factories for image rendering and inventory color-coding.

### Help System

- `HelpDialog` (`util/HelpDialog.java`) - Programmatic dialogs for setup guide and quick help
- Setup guide auto-displays on first launch; accessible via "View Setup Guide" button during setup
- Dashboard has "?" button showing quick help with feature explanations
- `docs/setup-guide.html` - Standalone HTML manual for external distribution

### Distribution

Native installers are built using jpackage (JDK 17+):
- **macOS**: `scripts/build-macos.sh` → produces `.dmg`
- **Windows**: `scripts/build-windows.bat` → produces `.exe`
- **CI/CD**: `.github/workflows/build-release.yml` builds both platforms on tag push

Code signing (see `docs/CODE_SIGNING.md`):
- macOS requires Apple Developer ID ($99/yr) + notarization
- Windows requires EV Code Signing Certificate ($400-500/yr)

App icons go in `packaging/macos/` (.icns) and `packaging/windows/` (.ico)
