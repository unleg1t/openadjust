package com.adjustclient.modules.impl.movement.inventorymove;

import com.adjustclient.Adjust;
import com.adjustclient.core.events.*;
import com.adjustclient.utilities.game.packet.PacketHold;
import com.adjustclient.core.settings.NumberSetting;
import net.minecraft.client.gui.inventory.GuiInventory;
import net.minecraft.network.play.client.*;

public final class LegitInventoryMoveMode extends ListenerInventoryMoveMode {
    public final NumberSetting stopTime;
    public PacketHold hold;
    public int lastClickTick;
    public boolean inventoryAction;
    public boolean sendingOpen;

    public LegitInventoryMoveMode(InventoryMoveModule module) {
        super(module, "Legit");
        stopTime = new NumberSetting(module, "Stop time", 500L, 50L, 1000L, 50L,
                () -> module.mode.getValue() != this);
        stopTime.setDescription("How long to wait for before closing inventory.");
        stopTime.formatter(NumberSetting.MILLISECONDS);
    }

    @EventHandler public void onInputTick(UserInputVerificationEvent event) {
        if (inventoryAction) {
            inventoryAction = false;
            return;
        }
        if (hold != null) {
            releaseHold();
            event.inputRequested = false;
        }
        if (!isActive() || !Adjust.inventoryStateTracker.open) return;
        int tick = mc.thePlayer.ticksExisted;
        if (mc.thePlayer.inventory.getItemStack() != null) {
            lastClickTick = tick;
            return;
        }
        if (mc.currentScreen != null && mc.currentScreen.getClass() == GuiInventory.class) {
            net.minecraft.inventory.ContainerPlayer container = (net.minecraft.inventory.ContainerPlayer) mc.thePlayer.inventoryContainer;
            for (int slot = 0; slot < container.craftMatrix.getSizeInventory(); slot++) {
                if (container.craftMatrix.getStackInSlot(slot) != null) {
                    lastClickTick = tick;
                    return;
                }
            }
        }
        if (tick - lastClickTick < stopTime.getValue().intValue() / 50) return;
        event.inputRequested = false;
        mc.thePlayer.sendQueue.addToSendQueue(new C0DPacketCloseWindow(mc.thePlayer.inventoryContainer.windowId));
    }

    public void releaseHold() {
        if (hold != null) {
            Adjust.outboundPacketQueue.removeHold(hold);
            hold = null;
        }
    }

    @EventHandler public void onPacketSend(DirectionalPacketEvent event) {
        if (event.isCancelled() || !isActive() || !event.outgoing) return;
        net.minecraft.network.Packet<?> packet = event.packet;
        if (packet instanceof C16PacketClientStatus status) {
            if (status.getStatus() == C16PacketClientStatus.EnumState.OPEN_INVENTORY_ACHIEVEMENT && !sendingOpen) {
                event.cancelled = true;
            }
        } else if (packet instanceof C0EPacketClickWindow click) {
            if (click.getWindowId() != mc.thePlayer.inventoryContainer.windowId) return;
            lastClickTick = mc.thePlayer.ticksExisted;
            if (!Adjust.inventoryStateTracker.open) {
                acquireHold();
                inventoryAction = true;
                sendingOpen = true;
                mc.thePlayer.sendQueue.addToSendQueue(new C16PacketClientStatus(C16PacketClientStatus.EnumState.OPEN_INVENTORY_ACHIEVEMENT));
                sendingOpen = false;
            }
        } else if (packet instanceof C0DPacketCloseWindow close) {
            if (close.getWindowId() != mc.thePlayer.inventoryContainer.windowId) return;
            if (!Adjust.inventoryStateTracker.open) event.cancelled = true;
            else if (hold != null) inventoryAction = true;
        }
    }

    public void acquireHold() {
        if (hold != null) return;
        hold = Adjust.outboundPacketQueue.addHold(packet -> packet != null
                && (packet.getClass() == C0DPacketCloseWindow.class || packet.getClass() == C0EPacketClickWindow.class), false);
    }
}
