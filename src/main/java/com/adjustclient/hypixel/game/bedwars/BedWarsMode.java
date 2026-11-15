package com.adjustclient.hypixel.game.bedwars;

import java.util.List;

public enum BedWarsMode {
    EIGHT_ONE(List.of("Solo"), "Solo", false),
    EIGHT_TWO(List.of("Doubles", "Lucky V2 Doubles"), "Doubles", true),
    FOUR_THREE(List.of("3v3v3v3"), "3v3v3v3", true),
    FOUR_FOUR(List.of("4v4v4v4", "Lucky V2 4v4v4v4"), "4v4v4v4", true),
    TWO_FOUR(List.of("4v4"), "4v4", true),
    EIGHT_TWO_VOIDLESS(List.of("Voidless Doubles"), "Voidless Doubles", true),
    FOUR_FOUR_VOIDLESS(List.of("Voidless 4v4v4v4"), "Voidless 4v4v4v4", true),
    BED_WARS_DUEL(List.of("Bed Wars Duel"), "Bed Wars Duel", false),
    BED_RUSH_DUEL(List.of("Bed Rush Duel"), "Bed Rush Duel", false),
    CASTLE(List.of("Castle"), "Castle", true);
    public final List<String> aliases;
    public final String displaySubstring;
    public final boolean largeGame;
    public Object field28;
    BedWarsMode(List<String> aliases, String displaySubstring, boolean largeGame) {
        this.aliases = aliases;
        this.displaySubstring = displaySubstring;
        this.largeGame = largeGame;
    }
}
