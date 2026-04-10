# Technology Stack

**Analysis Date:** 2026-04-08

## Languages

**Primary:**
- Java 21 - All application code in `src/main/java/com/ismaelSS/`

## Runtime

**Environment:**
- Java 21 (JDK 21)
- JVM target: 21

**Build Tool:**
- Maven 3.x (pom.xml based)
- Maven compiler source/target: 21

## Frameworks

**GUI Framework:**
- JavaFX 21 - Desktop UI framework
  - `javafx-controls` - UI controls
  - `javafx-fxml` - FXML-based UI definitions
  - `javafx-graphics` - Graphics rendering

**Build/Dev:**
- `javafx-maven-plugin` 0.0.8 - JavaFX application packaging

## Key Dependencies

**Translation:**
- `space.dynomake:libretranslate-java` 1.0.9 - LibreTranslate API client
  - Used in: `src/main/java/com/ismaelSS/TranslateService.java`
  - API endpoint: `http://localhost:5000/translate`

**OCR:**
- `net.sourceforge.tess4j:tess4j` 5.3.0 - Tesseract OCR wrapper
  - Used in: `src/main/java/com/ismaelSS/TextExtractor.java`
  - Data path: `C:/Program Files/Tesseract-OCR/tessdata`

**JSON Processing:**
- `com.fasterxml.jackson.core:jackson-databind` 2.17.0 - JSON serialization/deserialization
  - Used in: `src/main/java/com/ismaelSS/storage/PreferencesManager.java`

**Native Windows Integration:**
- `net.java.dev.jna:jna-platform` 5.18.1 - Java Native Access for Windows API
  - Used in: `src/main/java/com/ismaelSS/nativewin/WinOverlayUtil.java`
  - Used in: `src/main/java/com/ismaelSS/nativewin/WindowHandleUtil.java`

- `com.github.kwhat:jnativehook` 2.2.2 - Global keyboard/mouse hooks
  - Used in: `src/main/java/com/ismaelSS/HotkeyManager.java`

**Code Generation:**
- `org.projectlombok:lombok` 1.18.44 - Annotation processor for boilerplate reduction
  - Used throughout codebase for data classes

## Testing

**Framework:**
- Not detected - No test dependencies in pom.xml

**Test Structure:**
- No test source directory (`src/test/`) present

## Configuration

**Build Config:**
- `pom.xml` - Maven project configuration
- JavaFX modules required: `javafx.controls,javafx.fxml`

**External Requirements:**
- Tesseract OCR engine installed at `C:/Program Files/Tesseract-OCR/tessdata`
- LibreTranslate server running at `http://localhost:5000`

---

*Stack analysis: 2026-04-08*