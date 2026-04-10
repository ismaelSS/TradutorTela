# Coding Conventions

**Analysis Date:** 2026-04-08

## Naming Patterns

**Files:**
- PascalCase: `LayoutManagerView.java`, `TranslateService.java`, `LanguageValidator.java`
- Single-word class names for core utilities: `Main.java`, `TextExtractor.java`

**Packages:**
- Lowercase: `com.ismaelSS.layouts`, `com.ismaelSS.translate`, `com.ismaelSS.storage`
- Functional grouping: `layouts/`, `translate/`, `nativewin/`, `storage/`

**Functions:**
- camelCase: `loadData()`, `addLayout()`, `processText()`, `isTargetSupported()`
- Action verbs: `refreshWindowList()`, `clearOverlays()`, `toggleTranslation()`

**Variables:**
- camelCase: `layouts`, `regions`, `selectedHwnd`, `isRunning`
- Instance fields: no prefix (e.g., `translateService`, `textExtractor`)
- Local variables: descriptive (e.g., `result`, `selectedLayout`)

**Constants:**
- UPPER_SNAKE_CASE: `FILE = "layouts.json"`
- Static final with descriptive names

**Types/Classes:**
- PascalCase: `Layout`, `Region`, `LanguageExtended`, `WindowItem`

## Code Style

**Formatting:**
- Not detected: No `.editorconfig`, Checkstyle, or formatter config
- 4-space indentation in sample files
- Braces: K&R style (Egyptian)
- Line length: Up to ~120 characters observed

**Linting:**
- Not detected: No ESLint, Checkstyle, or SpotBugs configuration
- Lombok used via `@NonNull` annotation in `TranslateRequisitionAssembler`

**Language Level:**
- Java 21 (detected in `pom.xml`: `maven.compiler.source>21</maven.compiler.source`)
- JavaFX 21 for UI components

## Import Organization

**Order (observed):**
1. Java standard library: `java.util.*`, `java.io.*`
2. Third-party (JavaFX): `javafx.application.*`, `javafx.scene.*`
3. Third-party (Tesseract, JNA, Jackson): `net.sourceforge.tess4j.*`, `com.sun.jna.*`
4. Project-specific: `com.ismaelSS.*`

**Path Aliases:**
- Not applicable for Java

## Error Handling

**Patterns:**

1. **Try-catch with printing** (most common):
   ```java
   } catch (Exception e) {
       e.printStackTrace();
   }
   ```
   Found in: `LayoutStorage.java`, `TextExtractor.java`, `LanguageValidator.java`

2. **Try-catch returning default values**:
   ```java
   } catch (Exception e) {
       e.printStackTrace();
       return new ArrayList<>();
   }
   ```
   Found in: `LayoutStorage.load()`

3. **Try-catch returning original input** (fail-safe translation):
   ```java
   } catch (Exception e) {
       System.out.println("ERRO TRADUÇÃO: " + e.getMessage());
       return text;
   }
   ```
   Found in: `TranslateService.translate()`

4. **RuntimeException wrapping**:
   ```java
   } catch (Exception e) {
       if (e instanceof RuntimeException) {
           throw (RuntimeException) e;
       } else {
           e.printStackTrace();
           throw new RuntimeException(e);
       }
   }
   ```
   Found in: `TranslateRequisitionAssembler`

5. **Null checks with early returns**:
   ```java
   if (text == null || text.isBlank()) return "";
   ```
   Found in: `TranslateService.translate()`, `TranslateRequisitionAssembler.translate()`

**Logging:**
- Console output via `System.out.println("ERRO TRADUÇÃO: " + e.getMessage())`
- `System.err.println()` for missing resource warnings
- No logging framework detected (no SLF4J, Log4j, etc.)

## Comments

**When to Comment:**
- Observed: Minimal inline comments
- Some explanatory comments for non-obvious logic (e.g., in `TextExtractor.java` RGB processing)
- Code structure uses whitespace and method naming for clarity

**JSDoc/TSDoc:**
- Not applicable (Java project)

**Portuguese in Code:**
- User-facing strings in Portuguese: `"Selecione a janela alvo..."`, `"ERRO TRADUÇÃO"`
- Variable names in Portuguese: `linhas`, `resultado`, `conteudo` in `Main.java`

## Function Design

**Size:**
- Varies: Short helpers (5-10 lines) to large controllers (356 lines in `LayoutManagerView`)
- No strict method length enforcement

**Parameters:**
- Validation with `@NonNull` (Lombok) in `TranslateRequisitionAssembler`
- Null checks inline for others

**Return Values:**
- Early returns for edge cases (null/blank input)
- Default values on exceptions
- Original input preserved on failure (translation service)

## Module Design

**Exports:**
- Public classes: `Main`, `LayoutManagerView`, `TranslateService`, `TextExtractor`
- Package-private utility classes: `LayoutStorage`, `LanguageValidator`, `TranslateRequisitionAssembler`
- Package-private models: `Layout`, `Region`, `LanguageExtended`, `LanguageExtended`

**Static Methods:**
- Heavy use of static methods for utilities: `LayoutStorage.save()`, `LayoutStorage.load()`, `LanguageValidator.isTargetSupported()`
- Static initialization blocks: `static { }` in `TranslateService`, `TextExtractor`, `LanguageValidator`

**Utility Classes Pattern:**
```java
private TranslateRequisitionAssembler() {
    throw new UnsupportedOperationException("This is a utility class and cannot be instantiated");
}
```
Found in: `TranslateRequisitionAssembler`

**Data Classes:**
- Simple POJOs with getters/setters: `Layout`, `Region`
- No builder pattern detected
- Jackson annotations for JSON serialization (auto-detected via `ObjectMapper`)

---

*Convention analysis: 2026-04-08*