# Codebase Concerns

**Analysis Date:** 2026-04-08

## Known Bugs

### ScreenCapture: Hardcoded offset values for region cropping
- **Files:** `src/main/java/com/ismaelSS/ScreenCapture.java:72-75`
- **Issue:** Magic number offsets (+10, -3, -2) are hardcoded for region cropping and likely only work for specific screen configurations. These offsets will cause misaligned cropping on different DPI settings, display scales, or window decorations.
- **Impact:** Translated overlays may appear offset from the actual text regions on many systems.
- **Fix approach:** Calculate offsets dynamically based on window borders, DPI scaling, and actual window rect vs captured region coordinates.

### ScreenCapture: Exception swallows capture and returns full image
- **Files:** `src/main/java/com/ismaelSS/ScreenCapture.java:77-81`
- **Issue:** When `getSubimage()` throws an exception (e.g., due to invalid region dimensions), the code silently returns the full window image instead of handling or reporting the error.
- **Impact:** User may receive incorrect/unexpected translated text covering the entire window instead of the selected region.
- **Fix approach:** Log the error, return null, or throw a more descriptive exception instead of silently falling back to the full image.

### ScreenSelector: No minimum size validation
- **Files:** `src/main/java/com/ismaelSS/ScreenSelector.java:56-68`
- **Issue:** Selection can produce zero-width or zero-height regions if the user clicks without dragging or drags in an invalid direction.
- **Impact:** Creating regions with zero dimensions will cause issues in OCR and screen capture.
- **Fix approach:** Add minimum width/height validation before creating the Region object.

### OverlayWindow: Hardcoded position adjustments
- **Files:** `src/main/java/com/ismaelSS/layoutManagerView/OverlayWindow.java:48-51`
- **Issue:** Y offset (-10) and height adjustment (-2) are hardcoded. These will cause incorrect overlay positioning on systems with different DPI or when the target window has different title bar heights.
- **Impact:** Translated text overlays will be misaligned from the original text.
- **Fix approach:** Calculate position based on actual captured region coordinates relative to the target window.

---

## Tech Debt

### TranslateRequisitionAssembler: Hardcoded API endpoint
- **Files:** `src/main/java/com/ismaelSS/translate/TranslateRequisitionAssembler.java:18`
- **Issue:** API URL is hardcoded as `http://localhost:5000/translate` with no configuration mechanism for production or different server URLs.
- **Impact:** No flexibility for deployment with remote LibreTranslate servers.
- **Fix approach:** Add configuration via environment variable or preferences file.

### TextExtractor: Hardcoded Tesseract path
- **Files:** `src/main/java/com/ismaelSS/TextExtractor.java:16-17`
- **Issue:** OCR datapath and language are hardcoded to `"C:/Program Files/Tesseract-OCR/tessdata"` and `"eng"`. Tesseract installation path varies by system.
- **Impact:** OCR will fail on systems where Tesseract is installed in a different location or requires different languages.
- **Fix approach:** Add configuration for Tesseract path, auto-detect installation, or bundle tessdata.

### LayoutStorage: Swallows all exceptions
- **Files:** `src/main/java/com/ismaelSS/storage/LayoutStorage.java:21-23,33-36`
- **Issue:** All save/load operations catch generic `Exception` and only print stack trace. No user feedback or recovery mechanism.
- **Impact:** Silent data loss; user has no indication when layouts fail to save or load.
- **Fix approach:** Add user notification and fallback mechanisms (backup files, defaults).

### ScreenCaptureCopy: Dead code with TODO
- **Files:** `src/main/java/com/ismaelSS/ScreenCaptureCopy.java:23,39`
- **Issue:** Class appears to be abandoned (TODOs marked), but still exists in the codebase. The `list()` method is still called on every capture and outputs to console.
- **Impact:** Debug output clutters console during operation.
- **Fix approach:** Either remove the class or properly implement it.

### Main.java: Unused imports and dead code
- **Files:** `src/main/java/com/ismaelSS/Main.java:13-14,47-65`
- **Issue:** `HashMap` and `Map` imports are unused. The `processText()` method is defined but never called.
- **Impact:** Confusing code; unused code accumulates.
- **Fix approach:** Remove unused imports and the dead `processText()` method.

### LayoutManagerView: Executor services never shut down
- **Files:** `src/main/java/com/ismaelSS/layoutManagerView/LayoutManagerView.java:57-60,221-223`
- **Issue:** `workerPool` and `executor` are created but never properly shut down. No shutdown hooks or cleanup in application close.
- **Impact:** Resource leak; threads continue running after application closes.
- **Fix approach:** Add `shutdown()` calls in a close handler or explicit cleanup method.

---

## Error Handling Gaps

### TranslateService: Generic exception catch
- **Files:** `src/main/java/com/ismaelSS/TranslateService.java:27-30`
- **Issue:** Catches generic `Exception`, only prints to console, and returns original text. No distinction between network errors, API errors, or parsing errors.
- **Impact:** User gets no feedback when translation fails; may not even realize translation didn't occur.
- **Fix approach:** Add specific exception handling, user notification, and fallback options.

### TranslateRequisitionAssembler: Returns null on translation failure
- **Files:** `src/main/java/com/ismaelSS/translate/TranslateRequisitionAssembler.java:33,70`
- **Issue:** If `getTranslatedText()` returns null (API failure), null propagates up and is converted to original text silently in `TranslateService`.
- **Impact:** Failed translations appear as successful but display the original text.
- **Fix approach:** Validate response and throw descriptive exceptions instead of returning null.

### LanguageValidator: Missing resource handling
- **Files:** `src/main/java/com/ismaelSS/translate/LanguageValidator.java:16-23`
- **Issue:** If `supportedLanguages.json` is missing, prints error to stderr but continues with empty language support map. Falls back to permissive `isTargetSupported` returning true.
- **Impact:** User may not realize language validation is broken and attempt unsupported translations.
- **Fix approach:** Fail fast with clear error message if required resource is missing.

### HotkeyManager: Exception silently caught
- **Files:** `src/main/java/com/ismaelSS/HotkeyManager.java:32-35`
- **Issue:** Native hook registration failure only prints stack trace. No user notification.
- **Impact:** Global hotkeys silently fail to register; user has no idea why shortcuts don't work.
- **Fix approach:** Add user notification with instructions (e.g., "Run as administrator").

---

## Security Considerations

### TextExtractor: Writes image files to working directory
- **Files:** `src/main/java/com/ismaelSS/TextExtractor.java:32`
- **Issue:** Writes `original.png` to the current working directory on every OCR operation with no cleanup.
- **Impact:** Files accumulate in the working directory, potentially exposing captured screen content if the directory is accessible.
- **Fix approach:** Use temp directory with automatic cleanup, or remove the debug write.

### TranslateRequisitionAssembler: Hardcoded default API key
- **Files:** `src/main/java/com/ismaelSS/translate/TranslateRequisitionAssembler.java:19`
- **Issue:** API key defaults to `"unknown"` string, which could be mistaken for a valid configuration.
- **Impact:** May cause confusing errors when API requires authentication.
- **Fix approach:** Use null instead of "unknown" to make missing configuration obvious.

### WindowHandleUtil: Uses reflection without fallback
- **Files:** `src/main/java/com/ismaelSS/nativewin/WindowHandleUtil.java:12-21`
- **Issue:** Uses reflection to access JavaFX internal APIs that change between versions. No fallback for different JavaFX implementations or versions.
- **Impact:** Application will crash on newer JavaFX versions or alternative implementations (e.g., Gluon, Liberica).
- **Fix approach:** Add version detection and alternative method access, or use official JavaFX APIs if available.

---

## Performance Considerations

### LayoutManagerView: Large thread pool for each instance
- **Files:** `src/main/java/com/ismaelSS/layoutManagerView/LayoutManagerView.java:57-59`
- **Issue:** Creates thread pool sized to `availableProcessors()` (all cores) but captures and processes are I/O-bound (screen capture, network translation).
- **Impact:** Wasted resources; more threads than useful for I/O-bound work.
- **Fix approach:** Use smaller fixed pool (2-4 threads) for I/O-bound operations.

### TextExtractor: No image caching
- **Files:** `src/main/java/com/ismaelSS/TextExtractor.java:29-42`
- **Issue:** Every capture triggers full OCR processing. No result caching even when screen content hasn't changed.
- **Impact:** Unnecessary CPU usage for static content; network calls for repeated translations.
- **Fix approach:** Add dirty region detection or result caching based on screen content hash.

### LayoutManagerView: No rate limiting on translation loop
- **Files:** `src/main/java/com/ismaelSS/layoutManagerView/LayoutManagerView.java:222-233`
- **Issue:** Fixed 2-second interval with no consideration for translation latency. Slow translations queue up while new captures keep coming.
- **Impact:** Laggy UI, increasing backlog under slow network conditions.
- **Fix approach:** Use adaptive timing or completion-based scheduling instead of fixed interval.

### OverlayWindow: Font size adjustment loops inefficiently
- **Files:** `src/main/java/com/ismaelSS/layoutManagerView/OverlayWindow.java:100-125`
- **Issue:** Binary search-style font size reduction recalculates layout repeatedly. Called on every text update.
- **Impact:** CPU usage spikes when text updates frequently.
- **Fix approach:** Cache computed font size per region dimensions, or use better-fitting algorithm.

---

## Edge Cases Not Handled

### ScreenCapture: Window handle becomes invalid
- **Files:** `src/main/java/com/ismaelSS/ScreenCapture.java:17-82`
- **Issue:** No validation that the HWND is still valid before capture. Target window may close during translation.
- **Impact:** Native call fails or crashes; no graceful handling.
- **Fix approach:** Check `IsWindow()` before capture and handle gracefully.

### LayoutManagerView: No layout selected when starting
- **Files:** `src/main/java/com/ismaelSS/layoutManagerView/LayoutManagerView.java:222-233`
- **Issue:** The translation loop checks `regions.isEmpty()` but doesn't verify a layout is actually selected or has valid regions before processing.
- **Impact:** User may think translation is running when no regions are defined.
- **Fix approach:** Clear indicator showing active layout and regions.

### OverlayWindow: Text content validation
- **Files:** `src/main/java/com/ismaelSS/layoutManagerView/OverlayWindow.java:74-88`
- **Issue:** If translation returns extremely long text, it may cause performance issues in text rendering. No truncation or pagination.
- **Impact:** Application may hang or display incorrectly for very large text.
- **Fix approach:** Add text length limits or scroll support for long translations.

### ScreenSelector: No way to cancel selection
- **Files:** `src/main/java/com/ismaelSS/ScreenSelector.java:36-73`
- **Issue:** User cannot cancel the selection; pressing Escape does nothing.
- **Impact:** User stuck in selection mode if they make a mistake.
- **Fix approach:** Add Escape key handler to close stage without selecting.

### Region: Negative dimensions not validated
- **Files:** `src/main/java/com/ismaelSS/layouts/Region.java:10-15`
- **Issue:** Constructor accepts negative width/height values without validation.
- **Impact:** Downstream operations (screen capture, subimage) may fail or behave unexpectedly.
- **Fix approach:** Validate dimensions in constructor, throw IllegalArgumentException for negative values.

---

## Fragile Areas

### WinOverlayUtil: Click-through window styling is complex
- **Files:** `src/main/java/com/ismaelSS/nativewin/WinOverlayUtil.java:16-39`
- **Issue:** Multiple Windows extended style flags combined. Some flags (0x08000000, 0x00000008) are magic numbers without constants.
- **Impact:** Hard to maintain; behavior changes may break click-through.
- **Fix approach:** Use documented constants, add comments explaining each flag combination.

### LayoutManagerView: Tight coupling between UI and logic
- **Files:** `src/main/java/com/ismaelSS/layoutManagerView/LayoutManagerView.java`
- **Issue:** Single class handles UI, translation logic, hotkey handling, and data management. Over 350 lines.
- **Impact:** Changes in one area risk breaking others; hard to test individual components.
- **Fix approach:** Extract services (translation, capture) into separate classes.

---

## Test Coverage Gaps

### No test files detected
- **Files:** None in codebase
- **Issue:** No unit tests, integration tests, or test infrastructure exists.
- **Impact:** No way to verify correctness of translation logic, OCR processing, or screen capture.
- **Risk:** High - regressions go undetected; refactoring is risky.
- **Priority:** High

---

*Concerns audit: 2026-04-08*