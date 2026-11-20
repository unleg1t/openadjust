package com.adjustclient.modules.impl.combat.autoheal;

import com.adjustclient.utilities.MinecraftInstance;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;

abstract class HealHandler implements MinecraftInstance {
    public abstract boolean accepts(Item item);
    public abstract boolean shouldHeal(HealItemSettings settings, ItemStack stack, boolean urgent);
}
