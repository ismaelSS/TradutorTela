# Phase 1: LibreTranslate Integration - Context

**Gathered:** 2026-04-08
**Status:** Ready for planning
**Source:** User requirements via /gsd-plan-phase

<domain>

## Phase Boundary

This phase delivers embedded LibreTranslate service with automatic lifecycle management integrated into the Java application.

**Current state:** Existing `LibreTranslateStarter.java` handles manual start/stop, but needs to be integrated into application lifecycle and enhanced with health checks and status feedback.

**Deliverables:**
- Embedded LibreTranslate with auto-start on app launch
- Auto-stop on app exit (proper cleanup)
- Health verification before translation requests
- Status indicator for connection state
- Graceful error handling

</domain>

<decisions>

## Implementation Decisions

### D-01: Installation Method
- **Decision:** Install LibreTranslate as embedded Python package within the application (not Docker)
- **Rationale:** User wants it "installed in the project" - embedded solution works without external dependencies
- **Implementation:** Use Python/pip to install libretranslate package, start as subprocess

### D-02: Lifecycle Management
- **Decision:** Start LibreTranslate when Main.start() is called, stop on application exit
- **Rationale:** User requirement - "inicia quando o projeto iniciar e encerre quando o projeto for encerrado"
- **Implementation:** Use JavaFX lifecycle hooks (start/stop) or Runtime.addShutdownHook

### D-03: Health Verification
- **Decision:** Verify LibreTranslate is operational before any translation request
- **Rationale:** User requirement - "todas as verificacoes necessarias para o uso"
- **Implementation:** Poll /languages endpoint with timeout before first translation

### D-04: Status Feedback
- **Decision:** Show connection status in UI (indicator or status bar)
- **Rationale:** User needs to know when LibreTranslate is ready
- **Implementation:** Add status label in LayoutManagerView, update on health check

### D-05: Error Handling
- **Decision:** Graceful degradation - show error message but don't crash
- **Rationale:** LibreTranslate might fail to start, app should remain usable
- **Implementation:** Try-catch in start, show user-friendly error, allow retry

### D-06: Port Configuration
- **Decision:** Use fixed port 5000 for localhost
- **Rationale:** Existing code uses port 5000, change would break compatibility
- **Implementation:** Keep as-is in LibreTranslateStarter

### D-07: Timeout Settings
- **Decision:** 120 second max wait for server startup, 5 second for health checks
- **Rationale:** Balance between user experience and reliability
- **Implementation:** Keep existing MAX_WAIT_SECONDS = 120

</decisions>

<canonical_refs>

## Canonical References

**Downstream agents MUST read these before planning or implementing.**

### Existing Code
- `src/main/java/com/ismaelSS/LibreTranslateStarter.java` — Current implementation (needs enhancement)
- `src/main/java/com/ismaelSS/TranslateService.java` — Uses Translator.setUrlApi()
- `src/main/java/com/ismaelSS/Main.java` — Application entry point

### Project Config
- `pom.xml` — Project dependencies (libretranslate-java, javafx, tess4j)

</canonical_refs>

<specifics>

## Specific Ideas

1. **Embedded Installation:** Currently LibreTranslateStarter downloads/installs on first run. Could improve by checking if already installed before attempting pip install.

2. **Startup Progress:** Show progress indicator while waiting for LibreTranslate to start.

3. **Retry Mechanism:** If LibreTranslate fails initially, provide "Retry" button in UI.

4. **Docker Note:** User currently runs LibreTranslate via Docker for development. The embedded solution should work standalone, Docker remains optional for advanced users.

</specifics>

<deferred>

## Deferred Ideas

- **Docker Compose:** Could support Docker as alternative installation method (defer - embedded is preferred)
- **Multi-language models:** Could add argument to download specific language models (defer - use default)
- **Custom port:** Could make port configurable (defer - fixed 5000 is fine for now)

</deferred>

---

*Phase: 01-libretranslate-integration*
*Context gathered: 2026-04-08 via /gsd-plan-phase*