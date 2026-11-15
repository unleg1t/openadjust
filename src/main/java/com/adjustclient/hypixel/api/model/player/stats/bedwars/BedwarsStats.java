package com.adjustclient.hypixel.api.model.player.stats.bedwars;

import com.adjustclient.hypixel.api.model.player.stats.GameStats;
import com.google.gson.annotations.SerializedName;

public final class BedwarsStats extends GameStats {
    @SerializedName("coins")
    public int coins;
    @SerializedName("winstreak")
    public Integer winstreak;
    @SerializedName("games_played_bedwars_1")
    public int gamesPlayed;
    @SerializedName("wins_bedwars")
    public Integer wins;
    @SerializedName("losses_bedwars")
    public Integer losses;
    @SerializedName("kills_bedwars")
    public int kills;
    @SerializedName("deaths_bedwars")
    public int deaths;
    @SerializedName("final_kills_bedwars")
    public int finalKills;
    @SerializedName("final_deaths_bedwars")
    public int finalDeaths;
    @SerializedName("beds_broken_bedwars")
    public int bedsBroken;
    @SerializedName("beds_lost_bedwars")
    public int bedsLost;
    @SerializedName("slumber")
    public BedwarsSlumber slumber;
    @SerializedName("active_star")
    public String activeStar;
    @SerializedName("active_prestige_scheme")
    public String activePrestigeScheme;
    @SerializedName("active_prestige_bracket")
    public String activePrestigeBracket;
    @SerializedName("favourites_2")
    public String favourites;

    public BedwarsStats() {
        slumber = new BedwarsSlumber();
    }

    public Integer getGamesPlayed() {
        return gamesPlayed;
    }

    public BedwarsPrestige getPrestige(int level) {
        BedwarsPrestige[] prestiges = BedwarsPrestige.values();
                if (activePrestigeScheme != null) {
                    for (BedwarsPrestige prestige : prestiges) {
                        if (("prestige_scheme_" + prestige.displayName.replace(' ', '_')).equalsIgnoreCase(activePrestigeScheme)) {
                            return prestige;
                        }
                    }
                }
                return prestiges[Math.min(prestiges.length - 1, Math.max(0, level / 100))];
    }

    public int getKills() {
        return finalKills;
    }

    public int getDeaths() {
        return finalDeaths;
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
        return 12;
    }

    public Integer getWlThreshold() {
        return 9;
    }

    public BedwarsStarSymbol getStarSymbol() {
        if (activeStar != null) {
                    for (BedwarsStarSymbol value : BedwarsStarSymbol.values()) {
                        if (value.key.equalsIgnoreCase(activeStar)) return value;
                    }
                }
                return BedwarsStarSymbol.DEFAULT;
    }

    public BedwarsBracketStyle getBracketStyle() {
        if (activePrestigeBracket != null) {
                    for (BedwarsBracketStyle value : BedwarsBracketStyle.values()) {
                        if (value.key.equalsIgnoreCase(activePrestigeBracket)) return value;
                    }
                }
                return BedwarsBracketStyle.NONE;
    }
}
