package com.adjustclient.hypixel.game.pit;
import com.adjustclient.util.StringUtil;
import net.minecraft.util.ChatColor;
public final class PitEnchantEntry {
    public final PitEnchant enchant;
    public final String level;
    public final ChatColor color;
    public Object field16;
    public PitEnchantEntry(PitEnchant enchant, int level, ChatColor color) {
        this.enchant = enchant;
        this.level = StringUtil.toRoman(level);
        this.color = color;
    }
}
