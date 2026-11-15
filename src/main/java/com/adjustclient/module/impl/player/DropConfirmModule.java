package com.adjustclient.module.impl.player;

import com.adjustclient.event.CancellablePlayerUpdateEvent;
import com.adjustclient.event.DirectionalPacketEvent;
import com.adjustclient.event.EventHandler;
import com.adjustclient.event.HotbarItemRenderEvent;
import com.adjustclient.module.Category;
import com.adjustclient.module.UtilityModule;
import com.adjustclient.module.impl.player.inventory.InventoryModule;
import com.adjustclient.module.impl.player.inventory.click.InventoryClicker;
import com.adjustclient.module.impl.render.hud.HUDModule;
import com.adjustclient.render.animation.Animation;
import com.adjustclient.render.animation.Easing;
import com.adjustclient.util.MinecraftInstance;
import com.adjustclient.util.Timer;
import java.awt.Color;
import net.minecraft.client.gui.Gui;
import net.minecraft.item.ItemStack;
import net.minecraft.network.play.client.C07PacketPlayerDigging;

public final class DropConfirmModule extends UtilityModule implements MinecraftInstance {
    public static DropConfirmModule INSTANCE;
    private Integer pendingSlot;
    private Timer pendingTimer;
    private Animation animation;

    public DropConfirmModule() {
        super("Drop Confirm", Category.PLAYER);
        INSTANCE = this;
        flags &= ~4;
        savedFlags &= ~4;
    }

    @EventHandler public void onTick(CancellablePlayerUpdateEvent event) {
        if (event.isCancelled() || !isEnabled()) return;
        if (pendingTimer != null && pendingTimer.hasElapsed(2000L)) {
            pendingTimer = null;
            pendingSlot = null;
            animation = null;
        }
    }

    @EventHandler public void onHotbarRender(HotbarItemRenderEvent event) {
        if (!isEnabled() || pendingSlot == null || pendingSlot != event.slot) return;
        int x = event.x;
        int y = event.y;
        float alpha = animation.update(120.0F);
        float progress = (float) (System.currentTimeMillis() - pendingTimer.lastMs) * 16.0F / 2000.0F;
        Color color = HUDModule.animatedColor != null ? HUDModule.animatedColor : HUDModule.colorSetting.getColor();
        int argb = (Math.max(0, Math.min(255, (int) alpha)) << 24) | (color.getRGB() & 0xFFFFFF);
        Gui.drawRect(x, y + (int) progress, x + 16, y + 16, argb);
    }

    @Override public void onDisable() { pendingTimer = null; pendingSlot = null; }

    @Override public String getDescription() { return "Double checks whether you actually want to drop an item that is considered useful."; }

    @EventHandler public void onPacket(DirectionalPacketEvent event) {
        if (event.isCancelled() || !isEnabled() || !event.outgoing || !(event.packet instanceof C07PacketPlayerDigging)) return;
        C07PacketPlayerDigging packet = (C07PacketPlayerDigging) event.packet;
        switch (packet.getStatus()) {
            case DROP_ALL_ITEMS:
            case DROP_ITEM:
                break;
            default: return;
        }
        int slot = mc.thePlayer.inventory.m();
        ItemStack stack = mc.thePlayer.inventory.getCurrentItem();
        boolean useful = false;
        if (stack != null) {
            InventoryModule inventory = InventoryModule.INSTANCE;
            var settings = inventory.inventorySettings;
            var snapshot = new com.adjustclient.module.impl.player.inventory.plan.InventorySnapshot(
                    inventory.hotbarLayout, settings);
            InventoryClicker clicker = new InventoryClicker(snapshot);
            clicker.clickDelay.timer.reset();
            clicker.clickDelay.durationMs = settings.resolveClickingDelay()
                    + com.adjustclient.util.RandomUtil.nextInt(settings.resolveClickingDelay() / 3);
            clicker.openingDelay.timer.reset();
            clicker.openingDelay.durationMs = 10L;
            clicker.resetOpeningDelay();
            snapshot.clicker = clicker;
            snapshot.keepChecker = new com.adjustclient.module.impl.player.inventory.plan.ItemKeepChecker(snapshot);
            inventory.updateSnapshot(snapshot, true);
            useful = snapshot.keepChecker.isUseful(stack);
        }
        if (useful && (pendingSlot == null || pendingSlot != slot)) {
            event.cancelled = true;
        } else if (packet.getStatus() == C07PacketPlayerDigging.Action.DROP_ALL_ITEMS || stack == null || stack.stackSize < 2) {
            pendingTimer = null;
            pendingSlot = null;
            animation = null;
            return;
        }
        pendingTimer = new Timer();
        pendingSlot = slot;
        animation = new Animation(Easing.LINEAR, 110L);
    }
}
