# External Integrations

**Analysis Date:** 2026-04-08

## Translation APIs

**LibreTranslate (Primary Translation Service):**
- SDK: `space.dynomake:libretranslate-java` 1.0.9
- Endpoint: `http://localhost:5000/translate`
- Implementation: `src/main/java/com/ismaelSS/TranslateService.java`
- Configuration: API URL set statically in static block
- Language support: Custom `LanguageExtended` enum in `src/main/java/com/ismaelSS/translate/`

## OCR Engine

**Tesseract OCR:**
- Library: `net.sourceforge.tess4j:tess4j` 5.3.0
- Data path: `C:/Program Files/Tesseract-OCR/tessdata`
- Language: English (`eng`)
- Configuration:
  - Character whitelist: `ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz.,!?;:`
  - Page segmentation mode: 6
  - OCR engine mode: 1
  - DPI: 300
  - Preserve interword spaces: enabled
- Implementation: `src/main/java/com/ismaelSS/TextExtractor.java`

## Native Windows Integration

**JNA (Java Native Access):**
- Library: `net.java.dev.jna:jna-platform` 5.18.1
- Purpose: Windows API access for overlay window functionality

**WinOverlayUtil** (`src/main/java/com/ismaelSS/nativewin/WinOverlayUtil.java`):
- `user32.dll` - Window manipulation
  - `PrintWindow` - Capture window content to HDC
  - `GetWindowLong`/`SetWindowLong` - Window style manipulation
  - `SetLayeredWindowAttributes` - Alpha transparency

**Window Handle Utility** (`src/main/java/com/ismaelSS/nativewin/WindowHandleUtil.java`):
- Purpose: Extract native Windows HWND from JavaFX Stage

**jnativehook** (`com.github.kwhat:jnativehook` 2.2.2):
- Purpose: Global keyboard shortcuts
- Implementation: `src/main/java/com/ismaelSS/HotkeyManager.java`

## Data Storage

**Preferences (JSON):**
- Format: JSON via Jackson (`com.fasterxml.jackson.core:jackson-databind`)
- File: `preferences.json` (project root)
- Implementation: `src/main/java/com/ismaelSS/storage/PreferencesManager.java`
- Stored data:
  - Source language code
  - Target language code

**Layout Storage** (`src/main/java/com/ismaelSS/layouts/LayoutStorage.java`):
- Stores screen region layouts

## File Formats

**Configuration:**
- JSON (`preferences.json`) - User preferences

**Image Outputs:**
- PNG - Intermediate OCR processing (debug: `original.png`)

**Layout Data:**
- Custom format via `Layout` and `LayoutStorage` classes

## UI Framework

**JavaFX:**
- Version: 21
- FXML support: Yes (via `javafx-fxml`)
- Key components: Stage, Scene, Controls, FXML

## Environment Configuration

**Required External Services:**
- LibreTranslate server at `http://localhost:5000`

**Required External Software:**
- Tesseract OCR engine installed at `C:/Program Files/Tesseract-OCR/`

**Runtime Requirements:**
- Windows OS (JNA bindings are Windows-specific)
- Display for screen capture functionality

---

*Integration audit: 2026-04-08*