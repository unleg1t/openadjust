package com.adjustclient.modules.impl.render.hud.playerlist;

import net.minecraft.util.ChatColor;

final class TeamColorSwitchMap {

    static final int[] COLOR = new int[ChatColor.values().length];

    static {
        COLOR[ChatColor.RED.ordinal()] = 1;
        COLOR[ChatColor.BLUE.ordinal()] = 2;
        COLOR[ChatColor.GREEN.ordinal()] = 3;
        COLOR[ChatColor.YELLOW.ordinal()] = 4;
        COLOR[ChatColor.AQUA.ordinal()] = 5;
        COLOR[ChatColor.WHITE.ordinal()] = 6;
        COLOR[ChatColor.LIGHT_PURPLE.ordinal()] = 7;
        COLOR[ChatColor.DARK_GRAY.ordinal()] = 8;
    }
}
