package com.ismaelSS;

import space.dynomake.libretranslate.Language;
import space.dynomake.libretranslate.Translator;

public class TranslateService {

    static {
        Translator.setUrlApi("http://localhost:5000/translate");
    }

    public String translate(String text, Language sourceLang, Language targetLang) {
        try {

            if (text == null || text.isBlank()) return "";

            String result = Translator.translate(
                    sourceLang,
                    targetLang,
                    text
            );

            return result != null ? result : text;

        } catch (Exception e) {
            System.out.println("ERRO TRADUÇÃO: " + e.getMessage());
            return text;
        }
    }

}