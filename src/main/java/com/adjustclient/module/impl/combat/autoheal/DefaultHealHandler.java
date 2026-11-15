package com.adjustclient.module.impl.combat.autoheal;

import com.adjustclient.util.anticheat.ServerTracker;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;

class DefaultHealHandler extends HealHandler {
    @Override public boolean accepts(Item item) { return true; }
    @Override public boolean shouldHeal(HealItemSettings settings, ItemStack stack, boolean urgent) {
        float threshold = settings.getHealthThreshold();
        if (ServerTracker.INSTANCE.entityTracker.getEntities(67, null).isEmpty()) threshold *= 0.7F;
        return HealItemSettings.isHealthBelow(threshold);
    }
}
