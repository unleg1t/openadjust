package com.adjustclient.utilities.hypixel.api.model.player.stats;

import com.google.gson.annotations.SerializedName;
import net.minecraft.util.StringUtils;

public final class DuelsStats extends GameStats {
    @SerializedName("coins")
    public int coins;
    @SerializedName("best_overall_winstreak")
    public Integer bestWinstreak;
    @SerializedName("current_winstreak")
    public Integer winstreak;
    @SerializedName("games_played_duels")
    public int gamesPlayed;
    @SerializedName("wins")
    public Integer wins;
    @SerializedName("losses")
    public Integer losses;
    @SerializedName("kills")
    public int kills;
    @SerializedName("deaths")
    public int deaths;

    public DuelsStats() {
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
        return 15;
    }

    public Integer getKdThreshold() {
        return 5;
    }

    public Integer getWlThreshold() {
        return 5;
    }
}
