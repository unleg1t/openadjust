package com.adjustclient.modules.impl.player;

import com.adjustclient.core.events.CancellablePlayerUpdateEvent;
import com.adjustclient.core.events.DirectionalPacketEvent;
import com.adjustclient.core.events.EventHandler;
import com.adjustclient.core.events.HotbarItemRenderEvent;
import com.adjustclient.modules.Category;
import com.adjustclient.modules.UtilityModule;
import com.adjustclient.modules.impl.player.inventory.InventoryModule;
import com.adjustclient.modules.impl.player.inventory.click.InventoryClicker;
import com.adjustclient.modules.impl.render.hud.HUDModule;
import com.adjustclient.core.rendering.animation.Animation;
import com.adjustclient.core.rendering.animation.Easing;
import com.adjustclient.utilities.MinecraftInstance;
import com.adjustclient.utilities.Timer;
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
            var snapshot = new com.adjustclient.modules.impl.player.inventory.plan.InventorySnapshot(
                    inventory.hotbarLayout, settings);
            InventoryClicker clicker = new InventoryClicker(snapshot);
            clicker.clickDelay.timer.reset();
            clicker.clickDelay.durationMs = settings.resolveClickingDelay()
                    + com.adjustclient.utilities.RandomUtil.nextInt(settings.resolveClickingDelay() / 3);
            clicker.openingDelay.timer.reset();
            clicker.openingDelay.durationMs = 10L;
            clicker.resetOpeningDelay();
            snapshot.clicker = clicker;
            snapshot.keepChecker = new com.adjustclient.modules.impl.player.inventory.plan.ItemKeepChecker(snapshot);
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
