package com.adjustclient.module.impl.combat.autoheal;

import net.minecraft.init.Items;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.potion.Potion;
import net.minecraft.util.MathHelper;

class RegenHealHandler extends DefaultHealHandler {
    @Override public boolean accepts(Item item) { return item == Items.skull; }
    public static boolean hasStrongRegeneration() {
        return mc.thePlayer.isPotionActive(Potion.regeneration)
                && mc.thePlayer.getActivePotionEffect(Potion.regeneration).getAmplifier() >= 1
                && mc.thePlayer.getAbsorptionAmount() > 0.0F;
    }
    @Override public boolean shouldHeal(HealItemSettings settings, ItemStack stack, boolean urgent) {
        if (!hasStrongRegeneration()) return super.shouldHeal(settings, stack, urgent);
        float elapsed = (float) (System.currentTimeMillis() - settings.useTimer.lastMs) / 4000.0F;
        float multiplier = MathHelper.clamp_float(elapsed, 0.0F, 1.0F) * 0.7F + 0.3F;
        float max = mc.thePlayer.getMaxHealth();
        float ratio = Math.min(max, mc.thePlayer.getHealth()) / max;
        if (!HealItemSettings.isHealthBelow(settings.getHealthThreshold() * multiplier)) return false;
        return settings.useTimer.lastMs + (long) (ratio * 3500.0F) <= System.currentTimeMillis();
    }
}
