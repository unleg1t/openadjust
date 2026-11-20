package com.adjustclient.modules.impl.player.autorewound;

import net.minecraft.init.Items;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;

public enum PitSwordTier {
    WOOD(0),
    STONE(1),
    IRON(2),
    MYSTIC(3),
    DIAMOND(4),
    ENCHANTED_MYSTIC(3);

    public final int rank;

    PitSwordTier(int rank) {
        this.rank = rank;
    }

    public static PitSwordTier fromStack(ItemStack stack) {
        if (stack == null) return null;
        Item item = stack.getItem();
        if (item == Items.wooden_sword) return WOOD;
        if (item == Items.stone_sword) return STONE;
        if (item == Items.iron_sword) return IRON;
        if (item == Items.diamond_sword) return DIAMOND;
        if (item == Items.golden_sword) {
            NBTTagCompound tag = stack.getTagCompound();
            if (tag != null && tag.hasKey("ench", 9)) {
                return ENCHANTED_MYSTIC;
            }
            return MYSTIC;
        }
        return null;
    }
}
