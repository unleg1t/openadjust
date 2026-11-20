package com.adjustclient.utilities.hypixel.api.model.player.stats.bedwars;

import net.minecraft.util.ChatColor;
import static net.minecraft.util.ChatColor.*;

public record GradientPrestigeScheme(ChatColor col1, ChatColor col2, ChatColor col3, ChatColor col4, ChatColor col5, ChatColor col6, ChatColor col7) implements PrestigeColorScheme {

    @Override public String format(String text) {
        StringBuilder result = new StringBuilder();
        ChatColor[] colors = {col1, col2, col3, col4, col5, col6, col7};
        for (int i = 0; i < text.length(); i++) {
            if (i < colors.length) result.append(colors[i]);
            result.append(text.charAt(i));
        }
        return result.toString();
    }

    @Override public String toString() {
        return "f[col1=" + col1 + ", col2=" + col2 + ", col3=" + col3 + ", col4=" + col4 + ", col5=" + col5 + ", col6=" + col6 + ", col7=" + col7 + "]";
    }
}
