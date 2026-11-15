package com.adjustclient.hypixel.api.model.player.stats;

import com.google.gson.annotations.SerializedName;

public abstract class GameStats {
    public abstract int getKills();
    public abstract int getDeaths();

    public Integer getGamesPlayed() {
        return null;
    }

    public Integer getWinstreak() {
        return null;
    }

    public Integer getWins() {
        return null;
    }

    public Integer getLosses() {
        return null;
    }

    public Integer getWinstreakThreshold() {
        return null;
    }

    public Integer getKdThreshold() {
        return null;
    }

    public Integer getWlThreshold() {
        return null;
    }

    public Float getWinLossRatio() {
        Integer wins = getWins();
        int numerator = wins == null ? 0 : wins;
        Integer losses = getLosses();
        if (losses == null || losses == 0) {
            return numerator == 0 ? null : (float) numerator;
        }
        return (float) numerator / losses;
    }

    public Float getKillDeathRatio() {
        int kills = getKills();
        int deaths = getDeaths();
        if (deaths != 0) return (float) kills / deaths;
        if (kills == 0) return null;
        return (float) kills;
    }
}
