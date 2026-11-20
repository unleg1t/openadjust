package com.adjustclient.utilities.hypixel.api.model.player;

import com.adjustclient.Adjust;
import com.adjustclient.utilities.hypixel.api.model.player.stats.*;
import com.adjustclient.utilities.hypixel.api.model.player.stats.bedwars.BedwarsStats;
import com.google.gson.annotations.SerializedName;

public final class HypixelPlayerStats {
    @SerializedName("SkyWars")
    public SkyWarsStats skyWars;
    @SerializedName("Bedwars")
    public BedwarsStats bedwars;
    @SerializedName("Duels")
    public DuelsStats duels;
    @SerializedName("HungerGames")
    public BlitzStats hungerGames;
    @SerializedName("Pit")
    public PitStats pit;
    @SerializedName("UHC")
    public UhcStats uhc;
    @SerializedName("WoolGames")
    public WoolGamesStats woolGames;

    public HypixelPlayerStats() {}

    public GameStats getCurrentGameStats() {
        return switch (Adjust.gameStateTracker.state.game) {
            case UHC_CHAMPIONS -> uhc;
            case BLITZ_SG -> hungerGames;
            case DUELS -> duels;
            case BED_WARS_PRACTICE, BED_WARS -> bedwars;
            case SKYWARS -> skyWars;
            case WOOL_WARS -> woolGames == null ? null : woolGames.woolWars;
            case SHEEP_WARS -> woolGames == null ? null : woolGames.sheepWars;
            case CAPTURE_THE_WOOL -> woolGames == null ? null : woolGames.captureTheWool;
            default -> null;
        };
    }
}
