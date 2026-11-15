package com.adjustclient.hypixel.api.model.player.stats.bedwars;

import static net.minecraft.util.ChatColor.*;

public final class RainbowPrestigeScheme implements PrestigeColorScheme {
    @Override public String format(String text) {
        StringBuilder result = new StringBuilder();
        boolean first = true;
        for (char character : text.toCharArray()) {
            result.append(first || BedwarsStarSymbol.isStar(character) ? RED : WHITE).append(character);
            first = false;
        }
        return result.toString();
    }
}
