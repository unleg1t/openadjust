package com.adjustclient.hypixel.api.model.player.stats.bedwars;

import net.minecraft.util.ChatColor;
import static net.minecraft.util.ChatColor.*;

public record TwoColorPrestigeScheme(ChatColor numbersColor, ChatColor starColor) implements PrestigeColorScheme {

    @Override public String format(String text) {
        StringBuilder result = new StringBuilder();
        for (char character : text.toCharArray()) {
            result.append(BedwarsBracketStyle.isBracket(character) ? GRAY
                    : BedwarsStarSymbol.isStar(character) ? starColor : numbersColor);
            result.append(character);
        }
        return result.toString();
    }

    @Override public String toString() {
        return "g[numbersColor=" + numbersColor + ", starColor=" + starColor + "]";
    }
}
