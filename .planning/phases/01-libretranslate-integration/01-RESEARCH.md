# Phase 1: LibreTranslate Integration - Research

**Researched:** 2026-04-08
**Phase:** 01-libretranslate-integration
**Goal:** Integrate LibreTranslate as embedded service with automatic lifecycle management

---

## Research Summary

### 1. JavaFX Process Lifecycle Management

**Finding: Use Application.stop() over Runtime.addShutdownHook()**

- **Best practice:** Override `Application.stop()` method - this is the proper JavaFX lifecycle hook
- **Why:** Runtime.addShutdownHook() runs after JVM termination begins, may be too late for JavaFX cleanup
- **Implementation:** Override `stop()` in Main class, call LibreTranslateStarter.stop() there
- **Additional safety:** Also register a Runtime shutdown hook as backup for unexpected crashes

**Key pattern:**
```java
@Override
public void stop() {
    // This is called when JavaFX application exits
    if (libreTranslateStarter != null) {
        libreTranslateStarter.stop();
    }
}
```

---

### 2. Health Check Patterns for Embedded Services

**Finding: Poll endpoint with exponential backoff**

- **Recommended approach:** Poll `/languages` endpoint (lighter than `/translate`)
- **Timeout:** 120 seconds max (existing code already has this)
- **Polling interval:** Start at 1 second, increase to 2-5 seconds
- **Health check timing:** Run on background thread at startup, don't block UI

**Endpoint check:**
```java
HttpRequest request = HttpRequest.newBuilder()
    .uri(URI.create("http://localhost:5000/languages"))
    .timeout(Duration.ofSeconds(5))
    .build();
// Returns 200 if server is ready
```

**From existing code:** LibreTranslateStarter.java already has `isServerRunning()` method - can be reused/enhanced

---

### 3. Status Feedback in JavaFX

**Finding: Use Platform.runLater() for thread-safe UI updates**

- **Pattern:** All UI updates from background threads must use `Platform.runLater()`
- **UI components:** Use Label or specialized indicator (colored circle)
- **States:** 
  - "Starting..." (yellow/orange) - during startup wait
  - "Ready" (green) - when health check passes
  - "Error" (red) - when health check fails with retry option

**Thread-safe update pattern:**
```java
Platform.runLater(() -> {
    statusLabel.setText("Ready");
    statusLabel.setStyle("-fx-text-fill: green");
});
```

---

### 4. Python Process Management from Java

**Finding: Existing implementation is well-structured**

- **Current code:** LibreTranslateStarter already handles:
  - Python detection (python, python3, py, explicit paths)
  - LibreTranslate installation check (`pip show libretranslate`)
  - Auto-install if missing
  - Port configuration (5000)
  - Process start with proper command construction

**Recommendations for enhancement:**
1. Add `--check-installed` flag to avoid reinstall on every startup
2. Add environment variable configuration for Python path
3. Add logging to capture LibreTranslate stdout/stderr

---

## Technical Recommendations

### Implementation Approach

| Area | Recommendation | Rationale |
|------|-----------------|------------|
| Lifecycle | Use Application.stop() + shutdown hook | Proper cleanup + crash safety |
| Health check | Reuse existing isServerRunning() | Already implemented, works |
| Status UI | Add Label to LayoutManagerView | Simple, shows connection state |
| Threading | Background task for startup | Don't block JavaFX thread |

### Architecture Pattern

```
Main.java
  ├── LibreTranslateStarter (lifecycle management)
  │     ├── start() - launch subprocess
  │     ├── stop() - terminate subprocess  
  │     └── isServerRunning() - health check
  └── LayoutManagerView (UI with status)
        └── StatusLabel - shows connection state
```

### Edge Cases to Handle

1. **Python not installed** - Show clear error, suggest installation steps
2. **LibreTranslate install fails** - Show error, offer retry
3. **Server timeout** - Show timeout message after 120s
4. **App crash** - Shutdown hook ensures subprocess cleanup
5. **Port already in use** - Check if existing server, reuse or error

---

## Validation Architecture

**Dimension: Process Lifecycle (8.1)**
- Verify LibreTranslate process starts with application
- Verify subprocess terminates on application exit
- Verify no orphan processes after crash

**Dimension: Health Verification (8.2)**
- Verify health check runs before first translation
- Verify timeout behavior after 120 seconds
- Verify retry mechanism works

**Dimension: Status Feedback (8.3)**
- Verify UI updates on health check completion
- Verify error state displays correctly
- Verify thread-safety of UI updates

---

## References

- Stack Overflow: "Using JavaFX Application.stop() method over Shutdownhook"
- Oracle JavaFX Docs: Platform class
- CodingTechRoom: "Update JavaFX UI from Different Threads"
- Existing code: LibreTranslateStarter.java, TranslateService.java

---

*Research completed: 2026-04-08*
*Phase: 01-libretranslate-integration*