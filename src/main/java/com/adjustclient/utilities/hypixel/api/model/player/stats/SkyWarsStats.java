package com.adjustclient.utilities.hypixel.api.model.player.stats;

import com.google.gson.annotations.SerializedName;
import net.minecraft.util.StringUtils;

public final class SkyWarsStats extends GameStats {
    @SerializedName("coins")
    public int coins;
    @SerializedName("win_streak")
    public Integer winstreak;
    @SerializedName("games_played_skywars")
    public int gamesPlayed;
    @SerializedName("wins")
    public Integer wins;
    @SerializedName("losses")
    public Integer losses;
    @SerializedName("kills")
    public int kills;
    @SerializedName("deaths")
    public int deaths;
    @SerializedName("levelFormatted")
    public String levelFormatted;
    private transient Integer level;

    public SkyWarsStats() {
    }

    public Integer getGamesPlayed() {
        return gamesPlayed;
    }

    public int getKills() {
        return kills;
    }

    public int getDeaths() {
        return deaths;
    }

    public Integer getWinstreak() {
        return winstreak;
    }

    public Integer getWins() {
        return wins;
    }

    public Integer getLosses() {
        return losses;
    }

    public Integer getWinstreakThreshold() {
        return 5;
    }

    public Integer getKdThreshold() {
        return 6;
    }

    public Integer getWlThreshold() {
        return 1;
    }

    public Integer getLevel() {
        if (level != null) return level;
        if (levelFormatted == null) return null;
        level = Integer.parseInt(StringUtils.stripControlCodes(levelFormatted).replaceAll("\\D", ""));
        return level;
    }
}
