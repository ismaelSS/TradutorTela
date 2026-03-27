package com.ismaelSS.translate;

import space.dynomake.libretranslate.Language;
import space.dynomake.libretranslate.Translator;

public class TranslateService {

    // 🔥 configura API UMA vez
    static {
        Translator.setUrlApi("http://localhost:5000/translate");
    }

    public String translate(String text, Language sourceLang, Language targetLang) {
        try {

            if (text == null || text.isBlank()) return "";

            // 🔥 chamada da lib (simples e limpa)
            String result = Translator.translate(
                    sourceLang,
                    targetLang,
                    text
            );

            return result != null ? result : text;

        } catch (Exception e) {
            System.out.println("ERRO TRADUÇÃO: " + e.getMessage());
            return text; // fallback
        }
    }

}