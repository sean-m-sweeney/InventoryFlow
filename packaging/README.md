# App Icons

Place your app icons in this directory before building installers.

## Required Files

### macOS
- `macos/InventoryFlow.icns` - macOS icon file (required for .dmg)

To create .icns from a PNG:
```bash
# Create iconset directory
mkdir InventoryFlow.iconset

# Create all required sizes (from a 1024x1024 source.png)
sips -z 16 16 source.png --out InventoryFlow.iconset/icon_16x16.png
sips -z 32 32 source.png --out InventoryFlow.iconset/icon_16x16@2x.png
sips -z 32 32 source.png --out InventoryFlow.iconset/icon_32x32.png
sips -z 64 64 source.png --out InventoryFlow.iconset/icon_32x32@2x.png
sips -z 128 128 source.png --out InventoryFlow.iconset/icon_128x128.png
sips -z 256 256 source.png --out InventoryFlow.iconset/icon_128x128@2x.png
sips -z 256 256 source.png --out InventoryFlow.iconset/icon_256x256.png
sips -z 512 512 source.png --out InventoryFlow.iconset/icon_256x256@2x.png
sips -z 512 512 source.png --out InventoryFlow.iconset/icon_512x512.png
sips -z 1024 1024 source.png --out InventoryFlow.iconset/icon_512x512@2x.png

# Convert to icns
iconutil -c icns InventoryFlow.iconset -o macos/InventoryFlow.icns
```

### Windows
- `windows/InventoryFlow.ico` - Windows icon file (required for .exe)

To create .ico, use an online converter or ImageMagick:
```bash
# Using ImageMagick (install with: brew install imagemagick)
convert source.png -define icon:auto-resize=256,128,64,48,32,16 windows/InventoryFlow.ico
```

## Icon Design Guidelines

- Use a 1024x1024 PNG as the source
- Keep important details visible at 16x16
- Use a simple, recognizable shape
- Consider using a warehouse/inventory box icon with a flow/sync symbol
