package com.adjustclient.module.impl.render.hud.playerlist.stats;

import net.minecraft.item.EnumDyeColor;
import net.minecraft.scoreboard.ScorePlayerTeam;
import net.minecraft.util.ChatColor;
import net.minecraft.util.StringUtils;

import java.util.HashMap;
import java.util.Map;
import java.util.regex.Pattern;

public final class BedWarsTeam {

    public static final BedWarsTeam[] VALUES;
    public static final Map<EnumDyeColor, BedWarsTeam> BY_DYE_COLOR;

    public final String name;
    public final Character letter;
    public final ChatColor chatColor;
    public final EnumDyeColor dyeColor;

    static {
        VALUES = new BedWarsTeam[] {
                new BedWarsTeam("Yellow", 'Y', ChatColor.YELLOW, EnumDyeColor.YELLOW),
                new BedWarsTeam("Aqua", 'A', ChatColor.AQUA, EnumDyeColor.CYAN),
                new BedWarsTeam("White", 'W', ChatColor.WHITE, EnumDyeColor.WHITE),
                new BedWarsTeam("Pink", 'P', ChatColor.LIGHT_PURPLE, EnumDyeColor.PINK),
                new BedWarsTeam("Gray", 'S', ChatColor.DARK_GRAY, EnumDyeColor.GRAY),
                new BedWarsTeam("Red", 'R', ChatColor.RED, EnumDyeColor.RED),
                new BedWarsTeam("Blue", 'B', ChatColor.BLUE, EnumDyeColor.BLUE),
                new BedWarsTeam("Green", 'G', ChatColor.GREEN, EnumDyeColor.LIME),
        };
        BY_DYE_COLOR = new HashMap<>();
        for (BedWarsTeam team : VALUES) {
            BY_DYE_COLOR.put(team.dyeColor, team);
        }
    }

    private BedWarsTeam(String name, Character letter, ChatColor chatColor, EnumDyeColor dyeColor) {
        this.name = name;
        this.letter = letter;
        this.chatColor = chatColor;
        this.dyeColor = dyeColor;
    }

    /** Team for a wool / bed dye colour, or null. */
    public static BedWarsTeam byDyeColor(EnumDyeColor color) {
        return BY_DYE_COLOR.get(color);
    }

    /**
     * Team of a scoreboard team: the control codes ("§x" via Statics.s23a4 Pattern) are removed from the name prefix and the first remaining
     * character is looked up in {@link #VALUES} by team letter.
     */
    public static BedWarsTeam byTeamPrefix(ScorePlayerTeam team) {
        String prefix = team.getColorPrefix();
        String stripped = net.minecraft.util.StringUtils.stripControlCodes(prefix);
        if (stripped.isEmpty()) {
            return null;
        }
        char first = stripped.charAt(0);
        for (BedWarsTeam candidate : VALUES) {
            if (candidate.letter == first) {
                return candidate;
            }
        }
        return null;
    }
}
