package com.adjustclient.module.impl.movement.noslowdown;

import com.adjustclient.anticheat.feature.Features;
import com.adjustclient.event.EventHandler;
import com.adjustclient.event.ItemSlowdownEvent;
import com.adjustclient.event.MovementInputEvent;
import com.adjustclient.event.SprintEvent;
import com.adjustclient.event.SprintState;
import com.adjustclient.setting.BooleanSetting;
import com.adjustclient.setting.NumberSetting;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemSword;

public final class CustomNoSlowMode extends NoSlowMode {
    public static CustomNoSlowMode INSTANCE;
    public final NumberSetting sword;
    public final NumberSetting consumables;
    public final NumberSetting sneak;
    public final BooleanSetting allowSprinting;
    public final BooleanSetting slowness;

    public CustomNoSlowMode(NoSlowdownModule module) {
        super(module, "Custom", Features.NO_SLOW_CUSTOM);
        INSTANCE = this;
        sword = new NumberSetting(module, "Sword", 0.2, 0.2, 1.0, 0.01,
                () -> module.mode.getValue() != this);
        sword.setDisplayName("customSword");
        consumables = new NumberSetting(module, "Consumables", 0.2, 0.2, 1.0, 0.01,
                () -> module.mode.getValue() != this);
        consumables.setDisplayName("customConsumables");
        sneak = new NumberSetting(module, "Sneak", 0.3, 0.3, 1.0, 0.01,
                () -> module.mode.getValue() != this);
        sneak.setDisplayName("customSneak");
        allowSprinting = new BooleanSetting(module, "Allow sprinting", false,
                () -> module.mode.getValue() != this);
        allowSprinting.setDisplayName("customAllowSprinting");
        slowness = new BooleanSetting(module, "Slowness", true,
                () -> module.mode.getValue() != this);
        slowness.setDisplayName("customSlowness");
    }

    @EventHandler
    public void onSprint(SprintEvent event) {
        if (isActive() && event.state == SprintState.SLOWNESS && slowness.isEnabled()) event.cancelled = true;
    }

    @EventHandler
    public void onItemSlowdown(ItemSlowdownEvent event) {
        if (!isActive() || !event.apply) return;
        ItemStack held = mc.thePlayer.inventory.getCurrentItem();
        float multiplier = held != null && held.getItem() instanceof ItemSword ? sword.getFloat() : consumables.getFloat();
        mc.thePlayer.movementInput.moveStrafe *= multiplier;
        mc.thePlayer.movementInput.moveForward *= multiplier;
        event.apply = false;
        if (allowSprinting.isEnabled()) event.allowSprinting = true;
    }

    @EventHandler
    public void onSneakSlowdown(MovementInputEvent event) {
        if (!isActive()) return;
        event.sneakMultiplier = sneak.getDouble();
    }

    @Override
    public boolean removesSlowdown() {
        return true;
    }

    @Override
    public boolean isBlocking() {
        return false;
    }
}
