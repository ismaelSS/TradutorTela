# Tradutor de Tela - Roadmap

**Project:** Tradutor de Tela
**Created:** 2026-03-24

## Overview

Desktop screen translation application using LibreTranslate and OCR.

## Phase 1: LibreTranslate Integration

**Status:** Planning
**Goal:** Integrate LibreTranslate as embedded service with automatic lifecycle management

**Requirements:**
- [LIBRE-01] LibreTranslate starts automatically when application starts
- [LIBRE-02] LibreTranslate stops cleanly when application exits
- [LIBRE-03] Health check verifies LibreTranslate is operational before use
- [LIBRE-04] Graceful error handling when LibreTranslate fails to start
- [LIBRE-05] Status indicator shows LibreTranslate connection state

### Dependencies
None - foundational phase

---

## Future Phases

### Phase 2: Screen Capture Integration
- Region selection overlay
- Screen capture using Robot class
- Image preprocessing for OCR

### Phase 3: OCR Integration  
- Tesseract text extraction
- Language detection
- Text cleanup and formatting

### Phase 4: Translation UI
- Overlay window for translated text
- Copy to clipboard functionality
- Language selection UI