package com.ismaelSS.translate;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.InputStream;
import java.util.*;

public class LanguageValidator {

    private static final Map<String, Set<String>> sourceToTargets = new HashMap<>();

    static {
        loadSupportedLanguages();
    }

    private static void loadSupportedLanguages() {
        try {
            ObjectMapper mapper = new ObjectMapper();
            InputStream is = LanguageValidator.class.getResourceAsStream("/supportedLanguages.json");
            if (is == null) {
                System.err.println("supportedLanguages.json not found");
                return;
            }
            List<Map<String, Object>> languages = mapper.readValue(is, new TypeReference<List<Map<String, Object>>>() {});
            for (Map<String, Object> lang : languages) {
                String code = (String) lang.get("code");
                @SuppressWarnings("unchecked")
                List<String> targets = (List<String>) lang.get("targets");
                sourceToTargets.put(code, new HashSet<>(targets));
            }
            is.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static boolean isTargetSupported(LanguageExtended source, LanguageExtended target) {
        if (source == LanguageExtended.AUTO || source == LanguageExtended.NONE) {
            return true;
        }
        String sourceCode = source.getCode();
        Set<String> targets = sourceToTargets.get(sourceCode);
        if (targets == null) {
            return true;
        }
        return targets.contains(target.getCode());
    }

    public static List<LanguageExtended> getSupportedTargets(LanguageExtended source) {
        if (source == LanguageExtended.AUTO || source == LanguageExtended.NONE) {
            return Arrays.asList(LanguageExtended.values());
        }
        Set<String> targets = sourceToTargets.get(source.getCode());
        if (targets == null) {
            return Arrays.asList(LanguageExtended.values());
        }
        List<LanguageExtended> result = new ArrayList<>();
        for (LanguageExtended lang : LanguageExtended.values()) {
            if (targets.contains(lang.getCode())) {
                result.add(lang);
            }
        }
        return result;
    }
}
