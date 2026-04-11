package com.ismaelSS.storage;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.ismaelSS.translate.LanguageExtended;

import java.io.File;

@JsonIgnoreProperties(ignoreUnknown = true)
public class PreferencesManager {

    private static final String FILE = "preferences.json";
    private static final ObjectMapper mapper = new ObjectMapper();

    private String sourceLangCode;
    private String targetLangCode;
    private long updateIntervalMs = 1000;

    public String getSourceLangCode() {
        return sourceLangCode;
    }

    public void setSourceLangCode(String sourceLangCode) {
        this.sourceLangCode = sourceLangCode;
    }

    public String getTargetLangCode() {
        return targetLangCode;
    }

    public void setTargetLangCode(String targetLangCode) {
        this.targetLangCode = targetLangCode;
    }

    public long getUpdateIntervalMs() {
        return updateIntervalMs;
    }

    public void setUpdateIntervalMs(long updateIntervalMs) {
        this.updateIntervalMs = updateIntervalMs;
    }

    public static PreferencesManager load() {
        try {
            File file = new File(FILE);
            if (!file.exists()) {
                return new PreferencesManager();
            }
            return mapper.readValue(file, PreferencesManager.class);
        } catch (Exception e) {
            e.printStackTrace();
            return new PreferencesManager();
        }
    }

    public void save() {
        try {
            mapper.writerWithDefaultPrettyPrinter()
                    .writeValue(new File(FILE), this);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public LanguageExtended getSourceLang() {
        if (sourceLangCode == null) {
            return LanguageExtended.ENGLISH;
        }
        LanguageExtended lang = LanguageExtended.fromCode(sourceLangCode);
        return lang == LanguageExtended.NONE ? LanguageExtended.ENGLISH : lang;
    }

    public LanguageExtended getTargetLang() {
        if (targetLangCode == null) {
            return LanguageExtended.PORTUGUESE_BRAZIL;
        }
        LanguageExtended lang = LanguageExtended.fromCode(targetLangCode);
        return lang == LanguageExtended.NONE ? LanguageExtended.PORTUGUESE_BRAZIL : lang;
    }
}
