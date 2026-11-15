package com.adjustclient.hypixel.api.model.player;

import com.adjustclient.hypixel.api.model.ApiResponse;
import com.adjustclient.hypixel.api.model.player.stats.bedwars.*;
import com.adjustclient.ui.hud.notification.NotificationBuilder;
import com.google.gson.annotations.SerializedName;
import net.minecraft.util.ChatColor;
import java.util.Objects;

public final class HypixelPlayer extends ApiResponse {
    @SerializedName("displayname") public String displayName;
    @SerializedName("firstLogin") public Long firstLogin;
    @SerializedName("lastLogin") public Long lastLogin;
    @SerializedName("lastLogout") public Long lastLogout;
    @SerializedName("newPackageRank") public String newPackageRank;
    @SerializedName("rank") public String rank;
    @SerializedName("monthlyPackageRank") public String monthlyPackageRank;
    @SerializedName("packageRank") public String packageRank;
    @SerializedName("prefix") public String prefix;
    @SerializedName("rankPlusColor") public String rankPlusColor;
    @SerializedName("monthlyRankColor") public String monthlyRankColor;
    @SerializedName("networkExp") public Integer networkExp;
    @SerializedName("networkLevel") public Double networkLevel;
    @SerializedName("stats") public HypixelPlayerStats stats;
    @SerializedName("achievements") public HypixelAchievements achievements;
    @SerializedName("socialMedia") public HypixelSocialMedia socialMedia;
    @SerializedName("userLanguage") public String userLanguage;
    private transient HypixelLanguage languageCache;
    @SerializedName("channel") private String channel;
    private transient HypixelChannel channelCache;
    @SerializedName("karma") public int karma;
    public final transient long fetchedAt;
    @SerializedName("achievementPoints") public int achievementPoints;
    public transient boolean languageWarned;

    public HypixelPlayer() {
        fetchedAt = System.currentTimeMillis();
    }

    public HypixelLanguage getUserLanguage() {
        if (languageCache != null) return languageCache;
        if (userLanguage == null) return languageCache = HypixelLanguage.ENGLISH;
        try {
            return languageCache = HypixelLanguage.valueOf(userLanguage);
        } catch (IllegalArgumentException exception) {
            if (!languageWarned) {
                NotificationBuilder warning = new NotificationBuilder();
                warning.title = "Warning";
                warning.parts = new java.util.ArrayList<>();
                warning.actions = new java.util.ArrayList<>();
                warning.lifetimeSteps = 400;
                warning.addText("An unknown language ", 0xFFBEBCBD).append(userLanguage)
                        .addText(" was just received, if you got this notification please report it to Adjust developers.", 0xFFBEBCBD).show();
                languageWarned = true;
            }
            return null;
        }
    }

    private static boolean isMeaningfulRank(String value) {
        return value != null && !value.equals("NONE") && !value.equals("NORMAL");
    }

    public HypixelChannel getChannel() {
        if (channelCache != null) return channelCache;
        if (channel == null) return channelCache = HypixelChannel.ALL;
        try {
            return channelCache = HypixelChannel.valueOf(channel);
        } catch (IllegalArgumentException exception) {
            NotificationBuilder warning = new NotificationBuilder();
            warning.title = "Warning";
            warning.parts = new java.util.ArrayList<>();
            warning.actions = new java.util.ArrayList<>();
            warning.lifetimeSteps = 400;
            warning.addText("An unknown channel ", 0xFFBEBCBD).append(channel)
                    .addText(" was just received, if you got this notification please report it to Adjust developers.", 0xFFBEBCBD).show();
            return null;
        }
    }

    public String getRankPrefix() {
        if (prefix != null) return prefix;
        try {
            ChatColor plus = Objects.requireNonNullElse(ChatColor.getValueByName(rankPlusColor), ChatColor.RED);
            String effectiveRank = getEffectiveRank();
            return switch (effectiveRank) {
                case null -> "§7None";
                case "STAFF" -> "§c[§6ዞ§c]";
                case "YOUTUBER" -> "§c[§fYOUTUBE§c]";
                case "SUPERSTAR" -> {
                    ChatColor monthly = Objects.requireNonNullElse(ChatColor.getValueByName(monthlyRankColor), ChatColor.GOLD);
                    yield monthly + "[MVP" + plus + "++" + monthly + "]";
                }
                case "MVP_PLUS" -> "§b[MVP" + plus + "+§b]";
                case "MVP" -> "§b[MVP]";
                case "VIP_PLUS" -> "§a[VIP§6+§a]";
                case "VIP" -> "§a[VIP]";
                default -> "§7None";
            };
        } catch (NullPointerException exception) {
            return "§7None";
        }
    }

    public String getBedwarsStarText() {
        if (achievements == null || achievements.bedwarsLevel == null || stats == null || stats.bedwars == null) {
            return ChatColor.GRAY + "[0✫]";
        }
        int level = achievements.bedwarsLevel;
        BedwarsPrestige prestige = stats.bedwars.getPrestige(level);
        BedwarsStarSymbol star = stats.bedwars.getStarSymbol();
        BedwarsBracketStyle bracket = stats.bedwars.getBracketStyle();
        return prestige.colorScheme.format("" + bracket.open + level + star.symbol + bracket.close);
    }

    public String getBedwarsStar() {
        return getBedwarsStarText().replaceAll("[^0-9a-f§]", "");
    }

    public String getEffectiveRank() {
        if (isMeaningfulRank(rank)) return rank;
        if (isMeaningfulRank(monthlyPackageRank)) return monthlyPackageRank;
        if (isMeaningfulRank(newPackageRank)) return newPackageRank;
        return packageRank;
    }
}
