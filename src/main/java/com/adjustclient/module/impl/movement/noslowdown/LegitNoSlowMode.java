package com.adjustclient.module.impl.movement.noslowdown;

import com.adjustclient.Adjust;
import com.adjustclient.anticheat.feature.Feature;
import com.adjustclient.event.*;
import com.adjustclient.game.packet.PacketHold;
import com.adjustclient.game.packet.PacketHoldHandle;
import com.adjustclient.module.impl.combat.backtrack.LagBreakModule;
import com.adjustclient.setting.NumberSetting;
import com.adjustclient.util.DistanceUtil;
import com.adjustclient.util.WorldUtil;
import net.minecraft.util.Vec3;
import com.adjustclient.module.impl.player.nofall.NoFallModule;
import com.adjustclient.game.player.InventoryStateTracker;

public final class LegitNoSlowMode extends NoSlowMode {
    public static LegitNoSlowMode INSTANCE;
    public final NumberSetting blinkTicks;
    public PacketHold outboundHold;
    public final PacketHoldHandle inboundHold;
    public int blockingTicks;
    public boolean blocking;
    public boolean startBlink;
    public boolean skipNextUpdate;

    public LegitNoSlowMode(NoSlowdownModule module, Feature feature) {
        super(module, "Legit", feature);
        INSTANCE = this;
        inboundHold = new PacketHoldHandle(Adjust.inboundPacketQueue);
        blinkTicks = new NumberSetting(module, "Blink Ticks", 1, 1, 20, 1,
                () -> module.mode.getValue() != this);
    }

    @EventHandler(priority = 73)
    public void onControllerUpdate(IngameActionEvent event) {
        if (!isActive()) return;
        blocking = false;
        if (event.suppressActions || mc.thePlayer.ridingEntity != null
                || InventoryStateTracker.inServerGui || InventoryStateTracker.inServerInventory
                || module.getItemType(true) != NoSlowItemType.SWORD
                || (NoFallModule.INSTANCE.water.placement != null && NoFallModule.INSTANCE.water.isActive())
                || !Adjust.mouseClickHelper.right.isClicked(false)) {
            startBlink = false;
            blocking = false;
            blockingTicks = 0;
            skipNextUpdate = false;
            releaseHolds();
            return;
        }
        updateBlocking();
    }

    @EventHandler
    public void onMotion(MotionEvent event) {
        if (!isActive()) return;
        if (!event.pre && startBlink) {
            releaseHolds();
            if (!LagBreakModule.INSTANCE.hurtAtStart) LagBreakModule.INSTANCE.advanceHold();
            if (outboundHold == null) outboundHold = Adjust.outboundPacketQueue.addHold(null, false);
            startBlink = false;
        }
    }

    @EventHandler
    public void onUpdate(ControllerUpdatePhaseEvent event) {
        if (!isActive() || !event.pre) return;
        if (skipNextUpdate) {
            skipNextUpdate = false;
            return;
        }
        startBlink = false;
        blocking = false;
        blockingTicks = 0;
        skipNextUpdate = false;
        if (outboundHold != null) {
            Adjust.outboundPacketQueue.removeHold(outboundHold);
            outboundHold = null;
        }
        if (LagBreakModule.INSTANCE.target != null) {
            Vec3 eyes = mc.thePlayer.getPositionEyes(1.0F);
            if (DistanceUtil.distanceToEntity(eyes, LagBreakModule.INSTANCE.target.getEntity()) <= 3.0F)
                LagBreakModule.INSTANCE.advanceHold();
        }
        inboundHold.release();
    }

    @EventHandler
    public void onItemInUse(ItemUseDurationEvent event) {
        if (isActive() && blocking && module.getItemType(true) == NoSlowItemType.SWORD) event.useDuration = 72000;
    }

    @EventHandler
    public void onWorldReset(WorldResetEvent event) {
        if (!isActive()) return;
        startBlink = false;
        blocking = false;
        blockingTicks = 0;
        skipNextUpdate = false;
        releaseHolds();
    }

    @Override
    public void onDisable() {
        startBlink = false;
        blocking = false;
        blockingTicks = 0;
        skipNextUpdate = false;
        if (outboundHold != null) {
            Adjust.outboundPacketQueue.removeHold(outboundHold);
            outboundHold = null;
        }
        if (LagBreakModule.INSTANCE.target != null) {
            Vec3 eyes = mc.thePlayer.getPositionEyes(1.0F);
            if (DistanceUtil.distanceToEntity(eyes, LagBreakModule.INSTANCE.target.getEntity()) <= 3.0F)
                LagBreakModule.INSTANCE.advanceHold();
        }
        inboundHold.release();
    }

    @Override
    public boolean removesSlowdown() {
        return false;
    }

    @Override
    public boolean isBlocking() {
        return blocking;
    }

    public void updateBlocking() {
        skipNextUpdate = true;
        if (mc.thePlayer != null && !mc.playerController.isHittingBlock && WorldUtil.canInteract()) {
            blockingTicks++;
            var use = Adjust.mouseClickHelper.right;
            blocking = use.isClicked(false);
            if (blockingTicks == 1) {
                if (!use.previousDown) use.clicks = Integer.valueOf(1);
                use.down = Boolean.TRUE;
                Adjust.slotSpoofer.field17 = false;
                Adjust.slotSpoofer.active = false;
                startBlink = true;
            } else {
                acquireInboundHold();
                use.disabled = true;
                if (blockingTicks > blinkTicks.getInt()) blockingTicks = 0;
            }
            return;
        }
        startBlink = false;
        blocking = false;
        blockingTicks = 0;
        skipNextUpdate = false;
        if (outboundHold != null) {
            Adjust.outboundPacketQueue.removeHold(outboundHold);
            outboundHold = null;
        }
        if (LagBreakModule.INSTANCE.target != null) {
            Vec3 eyes = mc.thePlayer.getPositionEyes(1.0F);
            if (DistanceUtil.distanceToEntity(eyes, LagBreakModule.INSTANCE.target.getEntity()) <= 3.0F)
                LagBreakModule.INSTANCE.advanceHold();
        }
        inboundHold.release();
    }

    public void acquireInboundHold() {
        if (!LagBreakModule.INSTANCE.hurtAtStart) LagBreakModule.INSTANCE.advanceHold();
        if (outboundHold != null) return;
        var queue = Adjust.outboundPacketQueue;
        synchronized (queue.lock) {
            long sequence = queue.updateOldestHoldTime();
            PacketHold next = new PacketHold(null, sequence, false);
            queue.holds.add(next);
            outboundHold = next;
        }
    }

    public void releaseHolds() {
        if (outboundHold != null) {
            Adjust.outboundPacketQueue.removeHold(outboundHold);
            outboundHold = null;
        }
        if (LagBreakModule.INSTANCE.target != null) {
            Vec3 eyes = mc.thePlayer.getPositionEyes(1.0F);
            if (DistanceUtil.distanceToEntity(eyes, LagBreakModule.INSTANCE.target.getEntity()) <= 3.0F)
                LagBreakModule.INSTANCE.advanceHold();
        }
        inboundHold.release();
    }
}
