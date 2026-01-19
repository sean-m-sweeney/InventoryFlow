# Code Signing Guide

This guide explains how to sign InventoryFlow for distribution on macOS and Windows.

## Why Sign Your App?

| Without Signing | With Signing |
|-----------------|--------------|
| macOS: "App is damaged" error or Gatekeeper blocks | App opens normally |
| Windows: SmartScreen "Unknown publisher" warning | Shows your company name |
| Users may not trust the download | Professional, trusted appearance |

---

## macOS Code Signing & Notarization

### Requirements
- Apple Developer account ($99/year): https://developer.apple.com/programs/
- A Mac for building (or GitHub Actions with macOS runner)

### Step 1: Create Certificates

1. Log into [Apple Developer Portal](https://developer.apple.com/account)
2. Go to **Certificates, Identifiers & Profiles**
3. Click **+** to create a new certificate
4. Select **Developer ID Application** (for distribution outside App Store)
5. Follow the CSR creation process using Keychain Access
6. Download and install the certificate

### Step 2: Get Your Team ID

1. Go to [Apple Developer Membership](https://developer.apple.com/account/#/membership)
2. Note your **Team ID** (10-character string)

### Step 3: Create App-Specific Password

For notarization, you need an app-specific password:

1. Go to [Apple ID Account](https://appleid.apple.com/account/manage)
2. Sign in and go to **Security** → **App-Specific Passwords**
3. Click **Generate Password**
4. Save this password securely

### Step 4: Local Build Configuration

Set these environment variables before running `scripts/build-macos.sh`:

```bash
# Your certificate name (find with: security find-identity -v -p codesigning)
export MACOS_SIGNING_IDENTITY="Developer ID Application: Your Name (TEAMID)"

# For notarization
export APPLE_ID="your@email.com"
export APPLE_APP_PASSWORD="xxxx-xxxx-xxxx-xxxx"  # App-specific password
export APPLE_TEAM_ID="XXXXXXXXXX"
```

### Step 5: GitHub Actions Configuration

Add these secrets to your GitHub repository (Settings → Secrets → Actions):

| Secret | Description |
|--------|-------------|
| `MACOS_CERTIFICATE` | Base64-encoded .p12 certificate |
| `MACOS_CERTIFICATE_PWD` | Password for the .p12 file |
| `KEYCHAIN_PWD` | Any password for temporary keychain |
| `MACOS_SIGNING_IDENTITY` | Certificate name for codesign |
| `APPLE_ID` | Your Apple ID email |
| `APPLE_APP_PASSWORD` | App-specific password |
| `APPLE_TEAM_ID` | Your Team ID |

**To export certificate as Base64:**
```bash
# Export from Keychain (will prompt for password)
security export -k ~/Library/Keychains/login.keychain-db \
  -t identities -f pkcs12 -o certificate.p12

# Convert to Base64
base64 -i certificate.p12 | pbcopy  # Copies to clipboard
```

---

## Windows Code Signing

### Option A: EV Code Signing Certificate (Recommended)

Extended Validation certificates provide immediate SmartScreen trust.

#### Providers
- [DigiCert](https://www.digicert.com/signing/code-signing-certificates) - ~$500/year
- [Sectigo](https://sectigo.com/ssl-certificates-tls/code-signing) - ~$400/year
- [GlobalSign](https://www.globalsign.com/en/code-signing-certificate) - ~$400/year

#### Requirements
- Business registration documents
- Hardware token (USB) for private key storage
- Identity verification process (takes 1-5 business days)

#### Local Signing
```batch
REM Set environment variables
set WINDOWS_CERT_PATH=C:\path\to\certificate.pfx
set WINDOWS_CERT_PASSWORD=your-password

REM Run build script
scripts\build-windows.bat
```

### Option B: Azure Key Vault Signing (for CI/CD)

Store your certificate in Azure Key Vault for secure CI/CD signing.

1. Create an Azure Key Vault
2. Import your code signing certificate
3. Create a Service Principal with access to the vault
4. Add these GitHub secrets:

| Secret | Description |
|--------|-------------|
| `AZURE_KEY_VAULT_URI` | `https://your-vault.vault.azure.net/` |
| `AZURE_CLIENT_ID` | Service Principal App ID |
| `AZURE_CLIENT_SECRET` | Service Principal secret |
| `AZURE_TENANT_ID` | Azure AD Tenant ID |
| `AZURE_CERT_NAME` | Certificate name in Key Vault |

### Option C: Standard Code Signing Certificate

Cheaper (~$200/year) but doesn't provide immediate SmartScreen trust. Your app needs to build reputation through downloads before warnings disappear.

---

## Testing Signed Builds

### macOS
```bash
# Verify signature
codesign -dv --verbose=4 /Applications/InventoryFlow.app

# Check notarization
spctl -a -vv /Applications/InventoryFlow.app
```

### Windows
```powershell
# Verify signature
Get-AuthenticodeSignature "C:\Program Files\InventoryFlow\InventoryFlow.exe"
```

---

## Distributing Unsigned Builds (Not Recommended)

If you must distribute without signing:

### macOS Users
Tell users to run:
```bash
xattr -cr /Applications/InventoryFlow.app
```
Or: Right-click → Open → Open (bypass Gatekeeper once)

### Windows Users
- Click "More info" on SmartScreen warning
- Click "Run anyway"

**Note:** Many users won't trust unsigned software, and some organizations block unsigned apps entirely.

---

## Cost Summary

| Platform | Certificate Type | Annual Cost |
|----------|------------------|-------------|
| macOS | Apple Developer Program | $99 |
| Windows | EV Code Signing | $400-500 |
| Windows | Standard Code Signing | $200-300 |
| **Total (recommended)** | macOS + Windows EV | **~$600/year** |

---

## Timeline

1. **Day 1**: Apply for Apple Developer Program and order EV certificate
2. **Day 2-5**: Complete identity verification for EV certificate
3. **Day 5-7**: Receive hardware token, set up certificates
4. **Day 7+**: Build and distribute signed applications
