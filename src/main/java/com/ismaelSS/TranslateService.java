package com.ismaelSS;

import com.ismaelSS.translate.LanguageExtended;
import com.ismaelSS.translate.TranslateRequisitionAssembler;
import space.dynomake.libretranslate.Language;
import space.dynomake.libretranslate.Translator;

public class TranslateService {

    static {
        Translator.setUrlApi("http://localhost:5000/translate");
    }

    public String translate(String text, LanguageExtended sourceLang, LanguageExtended targetLang) {
        try {

            if (text == null || text.isBlank()) return "";

            String result = TranslateRequisitionAssembler.translate(
                    text,
                    sourceLang,
                    targetLang
            );

            return result != null ? result : text;

        } catch (Exception e) {
            System.out.println("ERRO TRADUÇÃO: " + e.getMessage());
            return text;
        }
    }

}