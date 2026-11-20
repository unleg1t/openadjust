package com.adjustclient.utilities.hypixel.api.model.player.stats.bedwars;

public enum BedwarsStarSymbol {
    DEFAULT("star_black_open", '\u272b'),
        PRESTIGE_1000("star_white_circled", '\u272a'),
        PRESTIGE_2000("star_white_outlined", '\u269d'),
        PRESTIGE_3000("star_four_clubs", '\u2725'),
        PRESTIGE_4000("star_black_outlined", '\u272d'),
        FOUR_POINTED("star_four_pointed", '\u2726'),
        PINWHEEL("star_pinwheel", '\u2735'),
        HOLLOW("star_hollow", '\u2730'),
        NAUTICAL("star_nautical", '\u272f');

    public final String key;
    public final char symbol;

    BedwarsStarSymbol(String key, char symbol) { this.key = key; this.symbol = symbol; }

    public static boolean isStar(char character) {
        return java.util.Arrays.stream(values()).anyMatch(
            value -> value.symbol == character);
    }

    @Override public String toString() { return String.valueOf(symbol); }
}
