package com.adjustclient.hypixel.api.model.player.stats.bedwars;

import net.minecraft.util.ChatColor;
import static net.minecraft.util.ChatColor.*;

public record SingleColorPrestigeScheme(ChatColor color) implements PrestigeColorScheme {

    @Override public String format(String text) {
        return color + text;
    }

    @Override public String toString() {
        return "i[color=" + color + "]";
    }
}
