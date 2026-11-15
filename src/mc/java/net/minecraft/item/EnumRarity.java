package net.minecraft.item;

import net.minecraft.util.ChatColor;

public enum EnumRarity
{
    COMMON(ChatColor.WHITE, "Common"),
    UNCOMMON(ChatColor.YELLOW, "Uncommon"),
    RARE(ChatColor.AQUA, "Rare"),
    EPIC(ChatColor.LIGHT_PURPLE, "Epic");

    public final ChatColor rarityColor;
    public final String rarityName;

    private EnumRarity(ChatColor color, String name)
    {
        this.rarityColor = color;
        this.rarityName = name;
    }
}
