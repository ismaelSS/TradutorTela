<objective>
Research how to implement Phase 1: LibreTranslate Integration
Answer: "What do I need to know to PLAN this phase well?"
</objective>

<files_to_read>
- .planning/phases/01-libretranslate-integration/01-CONTEXT.md (USER DECISIONS)
- .planning/PROJECT.md (Project context)
- .planning/ROADMAP.md (Roadmap)
- src/main/java/com/ismaelSS/LibreTranslateStarter.java (Existing implementation)
- src/main/java/com/ismaelSS/TranslateService.java (How translation is used)
- src/main/java/com/ismaelSS/Main.java (Application entry point)
- pom.xml (Project dependencies)
</files_to_read>

<additional_context>
**Phase description:** Integrate LibreTranslate as embedded service with automatic lifecycle management

**Phase requirement IDs:** None explicitly mapped yet

**Current state:**
- LibreTranslateStarter.java exists but needs enhancement
- Currently runs via Docker (user wants embedded)
- TranslationService uses Translator.setUrlApi() to connect

**Key requirements:**
1. LibreTranslate starts automatically when application starts
2. LibreTranslate stops cleanly when application exits  
3. Health check verifies LibreTranslate is operational before use
4. Graceful error handling when LibreTranslate fails to start
5. Status indicator shows LibreTranslate connection state

**Project instructions:** This is a Java 21 / JavaFX desktop application with Maven build
</additional_context>

<research_areas>

## Technical Research Required

### 1. JavaFX Process Lifecycle Management
- How to properly start a subprocess when JavaFX application starts
- How to ensure subprocess is terminated when JavaFX application exits
- Best practices for Runtime.addShutdownHook vs JavaFX lifecycle
- Handling unexpected application crashes (ensuring subprocess cleanup)

### 2. Health Check Patterns for Embedded Services
- How to verify LibreTranslate is ready before first translation
- Polling strategies with timeouts
- Endpoint patterns (/languages, /translate health verification)
- Retry mechanisms and fallback handling

### 3. Status Feedback in JavaFX
- UI indicators for connection state (label, icon, color)
- Updating UI from background thread (Platform.runLater)
- Progress indication during startup wait
- Error state display and retry options

### 4. Python Process Management from Java
- Detecting Python installation location
- Managing Python subprocess with proper environment
- Capturing subprocess output/logs
- Handling different OS environments (Windows focus)

</research_areas>

<output>
Write to: .planning/phases/01-libretranslate-integration/01-RESEARCH.md
</output>