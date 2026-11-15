package com.adjustclient.hypixel.game.zombies;

import net.minecraft.item.ItemStack;
import net.minecraft.util.ChatColor;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;

public enum ZombiesPowerUp {
    INSTA_KILL("Insta Kill", ChatColor.RED, new ItemStack(Items.diamond_sword), null),
    MAX_AMMO("Max Ammo", ChatColor.BLUE, new ItemStack(Items.arrow), null),
    DOUBLE_GOLD("Double Gold", ChatColor.GOLD, new ItemStack(Items.gold_ingot), null),
    CARPENTER("Carpenter", ChatColor.BLUE, new ItemStack(net.minecraft.item.Item.getItemFromBlock(Blocks.planks)), ZombiesDifficulty.ALIEN_ARCADIUM),
    SHOPPING_SPREE("Shopping Spree", ChatColor.DARK_PURPLE, new ItemStack(net.minecraft.item.Item.getItemFromBlock(Blocks.ender_chest)), ZombiesDifficulty.ALIEN_ARCADIUM),
    BONUS_GOLD("Bonus Gold", ChatColor.GOLD, new ItemStack(net.minecraft.item.Item.getItemFromBlock(Blocks.gold_block)), ZombiesDifficulty.ALIEN_ARCADIUM);
    public final String displayName;
    public final ChatColor color;
    public final ItemStack icon;
    public final ZombiesDifficulty map;
    public Object field32;
    ZombiesPowerUp(String displayName, ChatColor color, ItemStack icon, ZombiesDifficulty map) {
        this.displayName = displayName; this.color = color; this.icon = icon; this.map = map;
    }
    public int getColor() {
        return switch (color) {
            case RED -> 16733525;
            case BLUE -> 5592575;
            case GOLD -> 16755200;
            case DARK_PURPLE -> 11141290;
            default -> 0xFFFFFF;
        };
    }
}
