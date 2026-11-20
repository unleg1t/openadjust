package com.adjustclient.modules.impl.combat.autoheal;

import com.adjustclient.Adjust;
import com.adjustclient.utilities.hypixel.HypixelGame;
import com.adjustclient.core.settings.*;
import com.adjustclient.utilities.MinecraftInstance;
import com.adjustclient.utilities.Timer;
import java.util.function.BooleanSupplier;
import java.util.function.Supplier;
import net.minecraft.init.Items;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;

final class HealItemSettings implements MinecraftInstance {
    public final Item item;
    public final NumberSetting lowHealth;
    private final BooleanSupplier hidden;
    public final Timer useTimer = new Timer(0L);
    public Timer cooldown;
    public HealItemSettings(AutoHealModule module, Item item, BooleanSupplier hidden) {
        this.item = item;
        this.hidden = hidden;
        ExpandSetting group = new ExpandSetting(module, item.getUnlocalizedName(), hidden);
        ItemStack displayStack = new ItemStack(item);
        Supplier<String> displayName = () -> getItemName(item, displayStack);
        group.field36 = displayName;
        lowHealth = new NumberSetting(group, "Low Health", 0.75F, 0.05F, 1.0F, 0.05F);
        lowHealth.formatter(com.adjustclient.core.settings.NumberSetting.MULTIPLIER);
    }
    public static boolean isHealthBelow(float ratio) {
        return mc.thePlayer.getMaxHealth() * ratio >= mc.thePlayer.getHealth() + mc.thePlayer.getAbsorptionAmount();
    }
    public static String getItemName(Item item, ItemStack stack) {
        return item == Items.skull ? "Head" : item.getItemStackDisplayName(stack);
    }
    public boolean tryHeal(ItemStack stack, boolean urgent) {
        if (cooldown != null && !cooldown.hasElapsed(1500L)) return false;
        return getHandler().shouldHeal(this, stack, urgent);
    }
    public float getHealthThreshold() { return lowHealth.getFloat(); }
    public HealHandler getHandler() {
        HealHandler[] handlers = AutoHealModule.HEAL_HANDLERS;
        for (HealHandler handler : handlers) if (handler.accepts(item)) return handler;
        throw new RuntimeException("Default predicate unavailable? This shouldn't happen.");
    }
}
