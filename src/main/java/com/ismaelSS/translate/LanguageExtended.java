package com.ismaelSS.translate;


import java.util.Arrays;

public enum LanguageExtended {

    AUTO("auto"),

    ALBANIAN("sq"),
    ARABIC("ar"),
    AZERBAIJANI("az"),
    BASQUE("eu"),
    BENGALI("bn"),
    BULGARIAN("bg"),
    CATALAN("ca"),
    CHINESE("zh"),
    CHINESE_TRADITIONAL("zt"),
    CZECH("cs"),
    DANISH("da"),
    DUTCH("nl"),
    ENGLISH("en"),
    ESPERANTO("eo"),
    ESTONIAN("et"),
    FINNISH("fi"),
    FRENCH("fr"),
    GALICIAN("gl"),
    GERMAN("de"),
    GREEK("el"),
    HEBREW("he"),
    HINDI("hi"),
    HUNGARIAN("hu"),
    INDONESIAN("id"),
    IRISH("ga"),
    ITALIAN("it"),
    JAPANESE("ja"),
    KOREAN("ko"),
    KYRGYZ("ky"),
    LATVIAN("lv"),
    LITHUANIAN("lt"),
    MALAY("ms"),
    NORWEGIAN("nb"),
    PERSIAN("fa"),
    POLISH("pl"),
    PORTUGUESE("pt"),
    PORTUGUESE_BRAZIL("pt-BR"),
    ROMANIAN("ro"),
    RUSSIAN("ru"),
    SLOVAK("sk"),
    SLOVENIAN("sl"),
    SPANISH("es"),
    SWEDISH("sv"),
    TAGALOG("tl"),
    THAI("th"),
    TURKISH("tr"),
    UKRAINIAN("uk"),
    URDU("ur"),
    VIETNAMESE("vi"),

    NONE("none");

    private final String code;

    LanguageExtended(String code) {
        this.code = code;
    }

    public String getCode() {
        return code;
    }

    public static LanguageExtended fromCode(String code) {
        return Arrays.stream(values())
                .filter(l -> l.code.equalsIgnoreCase(code))
                .findFirst()
                .orElse(NONE);
    }
}
