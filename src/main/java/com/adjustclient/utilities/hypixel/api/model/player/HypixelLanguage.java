package com.adjustclient.utilities.hypixel.api.model.player;

public enum HypixelLanguage {
    ENGLISH("English"),
        GERMAN("German"),
        FRENCH("French"),
        DUTCH("Dutch"),
        SPANISH("Spanish"),
        ITALIAN("Italian"),
        CHINESE_SIMPLIFIED("Chinese (Simplified)"),
        CHINESE_TRADITIONAL("Chinese (Traditional)"),
        PORTUGUESE_BR("Portuguese (Brazil)"),
        RUSSIAN("Russian"),
        KOREAN("Korean"),
        POLISH("Polish"),
        JAPANESE("Japanese"),
        PIRATE("Pirate"),
        NORWEGIAN("Norwegian"),
        PORTUGUESE_PT("Portuguese (Portugal)"),
        SWEDISH("Swedish"),
        TURKISH("Turkish"),
        DANISH("Danish"),
        CZECH("Czech"),
        FINNISH("Finnish"),
        GREEK("Greek"),
        UKRAINIAN("Ukrainian"),
        ROMANIAN("Romanian"),
        HUNGARIAN("Hungarian"),
        SERBIAN_CYRILLIC("Serbian");

    public final String displayName;

    HypixelLanguage(String displayName) { this.displayName = displayName; }
}
