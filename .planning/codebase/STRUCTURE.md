# Codebase Structure

**Analysis Date:** 2026-04-08

## Directory Layout

```
tradutorDeTela/
├── src/main/java/com/ismaelSS/
│   ├── layouts/                    # Data models
│   ├── layoutManagerView/          # UI components  
│   ├── nativewin/                 # Windows native integration
│   ├── storage/                   # Persistence
│   ├── translate/                 # Translation helpers
│   ├── Main.java                  # Application entry
│   ├── ScreenSelector.java         # Area selection
│   ├── ScreenCapture.java          # Window capture (JNA)
│   ├── ScreenCaptureCopy.java    # Robot capture (legacy)
│   ├── TextExtractor.java         # OCR (Tesseract)
│   ├── TranslateService.java      # Translation service
│   ├── HotkeyManager.java         # Global hotkeys
│   └── OnAreaSelected.java       # Callback interface
├── src/main/resources/            # Resources
│   └── supportedLanguages.json    # Language pairs
└── pom.xml                      # Maven build
```

## Directory Purposes

**Root Package (`com.ismaelSS`):**
- Purpose: Core application logic and entry points
- Contains: Main, services, utilities, callbacks

**`layouts/`:**
- Purpose: Data models for screen regions and layouts
- Contains: `Layout.java`, `Region.java`
- Key files: `Region.java` - POJO with x/y/width/height

**`layoutManagerView/`:**
- Purpose: JavaFX UI components
- Contains: `LayoutManagerView.java` (main TabPane UI), `OverlayWindow.java` (translation overlay)
- Key files: `LayoutManagerView.java` - 356-line main controller

**`nativewin/`:**
- Purpose: Windows-specific native integration
- Contains: `WindowHandleUtil.java` (get HWND from Stage), `WinOverlayUtil.java` (transparent/click-through)
- Key files: Uses JNA User32 for window manipulation

**`storage/`:**
- Purpose: JSON file persistence
- Contains: `LayoutStorage.java` (layouts.json), `PreferencesManager.java` (preferences.json)
- Key files: Uses Jackson ObjectMapper for JSON serialization

**`translate/`:**
- Purpose: Language management and translation API helpers
- Contains: `LanguageExtended.java` (enum), `LanguageValidator.java`, `TranslateRequisitionAssembler.java`
- Key files: `LanguageExtended.java` - 78 language codes

## Key File Locations

**Entry Points:**
- `src/main/java/com/ismaelSS/Main.java` - JavaFX Application entry point

**Configuration:**
- `pom.xml` - Maven dependencies (Tesseract, LibreTranslate, JNA, JavaFX, jNativeHook)

**Core Logic:**
- `ScreenCapture.java` - JNA-based window capture (PrintWindow)
- `TextExtractor.java` - Tesseract OCR wrapper
- `TranslateService.java` - Translation orchestration
- `LayoutManagerView.java` - UI controller and pipeline orchestrator

**Testing:**
- No test directory detected (may not exist)

## Naming Conventions

**Java Classes:**
- PascalCase: `ScreenCapture.java`, `LayoutManagerView.java`, `LanguageValidator.java`

**Packages:**
- lowercase: `layouts/`, `layoutManagerView/`, `nativewin/`, `storage/`, `translate/`

**Interfaces:**
- PascalCase with suffix: `OnAreaSelected.java` (callback interface)

**Enums:**
- PascalCase: `LanguageExtended.java`

## Where to Add New Code

**New UI Feature:**
- Primary code: `src/main/java/com/ismaelSS/layoutManagerView/LayoutManagerView.java`

**New Service:**
- Primary code: `src/main/java/com/ismaelSS/` (root package)

**New Model:**
- Implementation: `src/main/java/com/ismaelSS/layouts/`

**Native Windows Feature:**
- Implementation: `src/main/java/com/ismaelSS/nativewin/`

**Persistence Feature:**
- Implementation: `src/main/java/com/ismaelSS/storage/`

## Special Directories

**`src/main/resources/`:**
- Purpose: JSON configuration files
- Generated: No
- Committed: Yes
- Contains: `supportedLanguages.json` - language pair mappings