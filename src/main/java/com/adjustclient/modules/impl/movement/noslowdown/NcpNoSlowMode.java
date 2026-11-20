package com.adjustclient.modules.impl.movement.noslowdown;

import com.adjustclient.Adjust;
import com.adjustclient.utilities.anticheat.feature.Feature;
import com.adjustclient.core.events.EventHandler;
import com.adjustclient.core.events.IngameActionEvent;
import com.adjustclient.core.events.ItemSlowdownEvent;
import com.adjustclient.core.events.MotionEvent;
import com.adjustclient.utilities.game.packet.PacketHold;
import com.adjustclient.utilities.game.packet.HeldPacketQueue;
import com.adjustclient.utilities.game.player.InventoryStateTracker;

public final class NcpNoSlowMode extends NoSlowMode {
    public static NcpNoSlowMode INSTANCE;
    public PacketHold hold;

    public NcpNoSlowMode(NoSlowdownModule module, Feature feature) {
        super(module, "NCP", feature);
        INSTANCE = this;
    }

    @EventHandler(priority = 72)
    public void onControllerUpdate(IngameActionEvent event) {
        if (!isActive() || event.suppressActions || mc.thePlayer.ridingEntity != null) return;
        if (InventoryStateTracker.isInServersideGui()) return;
        if (module.getItemType(false) != NoSlowItemType.SWORD) return;
        if (!Adjust.mouseClickHelper.right.isClicked(false)) return;
        beginHold();
        if (mc.thePlayer.isUsingItem()) mc.playerController.onStoppedUsingItem(mc.thePlayer);
        mc.rightClickDelayTimer = 0;
    }

    @EventHandler
    public void onMotion(MotionEvent event) {
        if (isActive() && !event.pre) releaseHold();
    }

    @EventHandler
    public void onItemSlowdown(ItemSlowdownEvent event) {
        if (!isActive()) return;
        event.apply = false;
        event.allowSprinting = true;
        event.ignoreSneakForSprint = true;
    }

    @Override
    public void onDisable() {
        releaseHold();
    }

    @Override
    public boolean removesSlowdown() {
        return false;
    }

    @Override
    public boolean isBlocking() {
        return false;
    }

    public void beginHold() {
        if (hold != null) return;
        HeldPacketQueue<?> queue = Adjust.outboundPacketQueue;
        synchronized (queue.lock) {
            long sequence = queue.updateOldestHoldTime();
            hold = new PacketHold(packet -> packet != null
                    && (packet.getClass() == net.minecraft.network.play.client.C0APacketAnimation.class
                    || packet.getClass() == net.minecraft.network.play.client.C08PacketPlayerBlockPlacement.class
                    || packet.getClass() == net.minecraft.network.play.client.C02PacketUseEntity.class), sequence, false);
            queue.holds.add(hold);
        }
    }

    public void releaseHold() {
        if (hold != null) {
            Adjust.outboundPacketQueue.removeHold(hold);
            hold = null;
        }
    }
}
