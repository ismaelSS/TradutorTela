# Architecture

**Analysis Date:** 2026-04-08

## Pattern Overview

**Overall:** MVC + Service Layers with JavaFX UI

**Key Characteristics:**
- JavaFX UI layer drives the application flow
- Service classes handle business logic (translation, OCR, screen capture)
- Layout/Region model objects represent captured screen areas
- Windows native integration via JNA for overlay transparency

## Layers

**UI Layer (`com.ismaelSS.layoutManagerView`):**
- Purpose: JavaFX UI and orchestration
- Location: `src/main/java/com/ismaelSS/layoutManagerView/`
- Contains: `LayoutManagerView.java` (main UI), `OverlayWindow.java` (translation overlay)
- Depends on: ScreenCapture, TextExtractor, TranslateService, HotkeyManager
- Used by: Main application entry

**Service Layer (`com.ismaelSS`):**
- Purpose: Core business logic
- Location: `src/main/java/com/ismaelSS/`
- Contains:
  - `ScreenCapture.java` - Captures window regions via Windows API (PrintWindow)
  - `ScreenSelector.java` - Interactive area selection with drag rectangle
  - `TextExtractor.java` - OCR via Tesseract
  - `TranslateService.java` - Translation orchestration
  - `HotkeyManager.java` - Global hotkey registration
- Depends on: JNA (Windows API), Tesseract OCR, LibreTranslate, jNativeHook
- Used by: LayoutManagerView

**Model Layer (`com.ismaelSS.layouts`):**
- Purpose: Data models for layouts and regions
- Location: `src/main/java/com/ismaelSS/layouts/`
- Contains: `Layout.java`, `Region.java`
- Depends on: None
- Used by: UI layer, storage

**Storage Layer (`com.ismaelSS.storage`):**
- Purpose: Persistence for layouts and preferences
- Location: `src/main/java/com/ismaelSS/storage/`
- Contains: `LayoutStorage.java`, `PreferencesManager.java`
- Depends on: Jackson
- Used by: UI layer

**Translation Layer (`com.ismaelSS.translate`):**
- Purpose: Language management and API assembly
- Location: `src/main/java/com/ismaelSS/translate/`
- Contains: `LanguageExtended.java`, `LanguageValidator.java`, `TranslateRequisitionAssembler.java`
- Depends on: LibreTranslate library
- Used by: TranslateService

**Native Integration Layer (`com.ismaelSS.nativewin`):**
- Purpose: Windows-specific window manipulation
- Location: `src/main/java/com/ismaelSS/nativewin/`
- Contains: `WindowHandleUtil.java`, `WinOverlayUtil.java`
- Depends on: JNA (User32)
- Used by: OverlayWindow, ScreenCapture

## Data Flow

**Main Translation Pipeline:**

1. **Screen Selection** - User drags rectangle via `ScreenSelector` → creates `Region` object
2. **Window Capture** - `ScreenCapture.captureWindowRegion(hwnd, region)` uses JNA PrintWindow to capture target window
3. **OCR** - `TextExtractor.extract(image)` runs Tesseract OCR on captured image
4. **Translation** - `TranslateService.translate(text, sourceLang, targetLang)` calls LibreTranslate API
5. **Overlay Display** - `OverlayWindow` displays translated text over original screen position

**Quick Capture (Hotkey: Shift+1):**
- `HotkeyManager` detects global hotkey
- Opens `ScreenSelector` for immediate area selection
- Captures → OCR → Translate → displays `quickOverlay`

**Continuous Translation (Layout Mode):**
- User selects target window and layout with regions
- `startLoop()` runs every 2 seconds via ScheduledExecutor
- For each region: capture → OCR → translate → update overlay

## Key Abstractions

**Region:**
- Purpose: Represents a screen area to capture and translate
- Examples: `src/main/java/com/ismaelSS/layouts/Region.java`
- Pattern: POJO with x, y, width, height

**Layout:**
- Purpose: Collection of regions applied to a specific window
- Examples: `src/main/java/com/ismaelSS/layouts/Layout.java`
- Pattern: Named container for multiple Region objects

**OnAreaSelected (Interface):**
- Purpose: Callback for screen selection completion
- Examples: `src/main/java/com/ismaelSS/OnAreaSelected.java`
- Pattern: Functional interface callback

## Entry Points

**Main Application:**
- Location: `src/main/java/com/ismaelSS/Main.java`
- Triggers: JavaFX Application launch
- Responsibilities: Initializes TranslateService, TextExtractor, creates LayoutManagerView

**UI Controller:**
- Location: `src/main/java/com/ismaelSS/layoutManagerView/LayoutManagerView.java`
- Triggers: User interactions and hotkey events
- Responsibilities: Orchestrates capture→translate→display pipeline

## Error Handling

**Strategy:** Silent fallback with console output

**Patterns:**
- `TranslateService.translate()` catches exceptions, returns original text
- `TextExtractor.extract()` catches exceptions, returns empty string
- Screen capture failures logged but don't crash the loop

## Cross-Cutting Concerns

**Logging:** `System.out.println()` - Not structured logging framework

**Validation:** `LanguageValidator` verifies source/target language pairs against supportedLanguages.json

**Authentication:** API key support in TranslateRequisitionAssembler (currently "unknown")

**Window Transparency:** JNA calls to SetWindowLong/SetLayeredWindowAttributes for click-through overlays