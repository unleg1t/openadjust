package com.adjustclient.utilities.hypixel.api.model.player.stats.bedwars;

public enum BedwarsBracketStyle {
    NONE("prestige_bracket_none", '[', ']'),
        CURLY_BRACE("prestige_bracket_curly_brace", '{', '}'),
        PARENTHESES("prestige_bracket_parentheses", '(', ')'),
        ANGLED("prestige_bracket_angled", '<', '>'),
        DOUBLE_ANGLED_QUOTATION_MARK("prestige_bracket_double_angle_quotation_mark", '\u00ab', '\u00bb');

    public final String key;
    public final char open;
    public final char close;

    BedwarsBracketStyle(String key, char open, char close) { this.key = key; this.open = open; this.close = close; }

    public static boolean isBracket(char character) {
        return java.util.Arrays.stream(values()).anyMatch(
            value -> value.open == character || value.close == character);
    }
}
