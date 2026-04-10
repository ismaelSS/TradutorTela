# Tradutor de Tela - Project Context

**Version:** 1.0
**Started:** 2026-03-24
**Status:** Active

<domain>

## Project Type

Desktop application (Windows) - Java/JavaFX with LibreTranslate integration

## What It Does

Captures screen regions, extracts text via OCR (Tesseract), translates using LibreTranslate, displays results in overlay window

## Tech Stack

- **Language:** Java 21
- **UI:** JavaFX 21
- **Translation:** LibreTranslate (local Python)
- **OCR:** Tesseract 5.3.0
- **Build:** Maven
- **Dependencies:** 
  - libretranslate-java (dynomake)
  - tess4j
  - jnativehook (global hotkeys)

</domain>

<decisions>

## Technical Decisions

### Architecture
- Single-window application with overlay for translation display
- Global hotkey (Ctrl+Shift+T) to capture screen regions
- Local LibreTranslate instance for privacy/offline capability

### Libraries
- Using dynomake's libretranslate-java client
- Using tess4j for OCR
- Using jnativehook for global keyboard hooks

### Platform
- Windows-only (uses Windows-specific APIs for window management and screen capture)

</decisions>

<concerns>

## Known Concerns

- LibreTranslate requires Python installation
- Some users run LibreTranslate via Docker instead of local install
- No graceful error handling when LibreTranslate fails to start

</concerns>

<requirements>

## Current Requirements

No formal requirements documented yet - this is a new project setup

</requirements>

---

*Generated: 2026-04-08*