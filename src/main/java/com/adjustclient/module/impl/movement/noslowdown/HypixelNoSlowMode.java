package com.adjustclient.module.impl.movement.noslowdown;

import com.adjustclient.Adjust;
import com.adjustclient.anticheat.feature.Feature;
import com.adjustclient.event.*;
import com.adjustclient.game.packet.PacketBatch;
import com.adjustclient.game.packet.PacketHoldHandle;
import com.adjustclient.server.ServerType;
import com.adjustclient.service.util.ServiceUtil;
import com.adjustclient.setting.BooleanSetting;
import com.adjustclient.util.RandomUtil;
import com.adjustclient.util.Timer;
import com.adjustclient.util.WorldUtil;
import net.minecraft.network.play.client.C03PacketPlayer;
import net.minecraft.util.MovingObjectPosition;
import com.adjustclient.module.impl.player.nofall.NoFallModule;
import com.adjustclient.game.player.InventoryStateTracker;

public final class HypixelNoSlowMode extends NoSlowMode {
    public static HypixelNoSlowMode INSTANCE;
    public final BooleanSetting delayJump;
    public final Timer movementTimer;
    public final PacketHoldHandle outboundHold;
    public final PacketBatch outboundBatch;
    public int blockingTicks;
    public int swapCountdown;
    public int specialCountdown;
    public boolean blocking;
    public boolean releasePending;
    public boolean updatePending;
    public boolean specialSwap;
    public boolean active;
    public boolean delayedJump;

    public HypixelNoSlowMode(NoSlowdownModule module, Feature feature) {
        super(module, "Hypixel", feature);
        INSTANCE = this;
        movementTimer = new Timer(0L);
        outboundHold = new PacketHoldHandle(Adjust.outboundPacketQueue);
        outboundBatch = new PacketBatch(Adjust.outboundPacketQueue);
        delayJump = new BooleanSetting(module, "Delay jump", true,
                () -> module.mode.getValue() != this);
        delayJump.setDescription("Delays your jumps while blocking so that you always sprint jump.");
    }

    @EventHandler(priority = 74)
    public void onControllerUpdate(IngameActionEvent event) {
        if (!isActive()) return;
        blocking = false;
        if (!event.suppressActions && mc.thePlayer.ridingEntity == null
                && !InventoryStateTracker.inServerGui && !InventoryStateTracker.inServerInventory
                && module.getItemType(true) == NoSlowItemType.SWORD
                && (NoFallModule.INSTANCE.water.placement == null || !NoFallModule.INSTANCE.water.isActive())
                && Adjust.mouseClickHelper.right.isClicked(false)) {
            updateBlocking();
            return;
        }
        updatePending = false;
        blocking = false;
        blockingTicks = 0;
        active = false;
        specialSwap = false;
        clearOrphanedNoSlowSpoof(false);
        outboundHold.release();
        synchronized (outboundBatch.holds) {
            while (!outboundBatch.holds.isEmpty()) outboundBatch.queue.removeHold(outboundBatch.holds.removeFirst());
        }
    }

    private void clearOrphanedNoSlowSpoof(boolean force) {
        if (Adjust.slotSpoofer == null || !Adjust.slotSpoofer.active || !Adjust.slotSpoofer.field17 || Adjust.slotSpoofer.renderSpoofed) return;
        if (NoFallModule.INSTANCE != null && NoFallModule.INSTANCE.water != null && NoFallModule.INSTANCE.water.isActive()) return;
        if (force || mc.thePlayer == null || module.getItemType(true) != NoSlowItemType.SWORD) {
            Adjust.slotSpoofer.field17 = false;
            Adjust.slotSpoofer.active = false;
        }
    }

    @EventHandler
    public void onUserInput(UserInputVerificationEvent event) {
        if (isActive()) active = false;
    }

    @EventHandler
    public void onMotion(MotionEvent event) {
        if (isActive() && !event.pre) releasePending = false;
    }

    @EventHandler
    public void onPostMotion(PostInputEvent event) {
        if (!isActive() || !releasePending) return;
        outboundHold.release();
        synchronized (outboundBatch.holds) {
            while (!outboundBatch.holds.isEmpty()) outboundBatch.queue.removeHold(outboundBatch.holds.removeFirst());
        }
    }

    @EventHandler
    public void onPostUpdate(GameLoopEvent event) {
        if (!isActive() || event.preScheduled || !active || !movementTimer.hasElapsed(50L)) return;
        synchronized (outboundBatch.holds) {
            if (!outboundBatch.holds.isEmpty()) outboundBatch.queue.removeHold(outboundBatch.holds.removeFirst());
        }
    }

    @EventHandler
    public void onUpdate(ControllerUpdatePhaseEvent event) {
        if (!isActive() || !event.pre) return;
        if (updatePending) {
            if (mc.thePlayer == null) {
                if (!active) resetState();
                return;
            }
            boolean swap = --swapCountdown < 1;
            if (swap) swapCountdown = RandomUtil.nextInt(RandomUtil.RANDOM, 3, 6);
            if (--specialCountdown < 1) {
                specialSwap = true;
                specialCountdown = RandomUtil.nextInt(RandomUtil.RANDOM, 5, 9);
            } else if (!swap) {
                updatePending = false;
                return;
            }
            if (ServiceUtil.isActiveModel(ServerType.HYPIXEL) && swapSlot()) {
                Adjust.mouseClickHelper.left.disabled = true;
                Adjust.mouseClickHelper.field4ClickOverride.disabled = true;
                outboundHold.acquire(outboundHold.field16);
            }
            updatePending = false;
        } else if (!active) {
            resetState();
        }
    }

    @EventHandler
    public void onPacketSend(LowLevelPacketSendEvent event) {
        if (isActive() && event.packet instanceof C03PacketPlayer) movementTimer.reset();
    }

    @EventHandler(priority = 2)
    public void onMovementInput(MovementInputEvent event) {
        if (!isActive()) return;
        if (delayedJump) {
            mc.thePlayer.jumpTicks = 0;
            event.jump = true;
            delayedJump = false;
        } else if (event.jump && blocking && mc.thePlayer.isUsingItem()
                && delayJump.isEnabled() && !mc.thePlayer.capabilities.isFlying) {
            event.jump = false;
            delayedJump = true;
        }
    }

    @EventHandler
    public void applyUseDuration(ItemUseDurationEvent event) {
        if (isActive() && blocking && module.getItemType(true) == NoSlowItemType.SWORD) event.useDuration = 72000;
    }

    public static Boolean isDifferentSlot(int slot) {
        return slot != mc.thePlayer.inventory.m();
    }

    @Override
    public void onDisable() {
        updatePending = false;
        blocking = false;
        blockingTicks = 0;
        active = false;
        specialSwap = false;
        clearOrphanedNoSlowSpoof(true);
        outboundHold.release();
        synchronized (outboundBatch.holds) {
            while (!outboundBatch.holds.isEmpty()) outboundBatch.queue.removeHold(outboundBatch.holds.removeFirst());
        }
        delayedJump = false;
    }

    @EventHandler(priority = 11)
    public void onUseItem(IngameActionEvent event) {
        if (isActive() && active && module.getItemType(true) == NoSlowItemType.SWORD && mc.objectMouseOver != null
                && mc.objectMouseOver.typeOfHit == MovingObjectPosition.MovingObjectType.BLOCK) WorldUtil.clearMouseOver();
    }

    @Override
    public void onEnable() {
        swapCountdown = RandomUtil.nextInt(RandomUtil.RANDOM, 3, 6);
        specialCountdown = RandomUtil.nextInt(RandomUtil.RANDOM, 5, 9);
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
        active = true;
        if (mc.thePlayer != null && !mc.playerController.isHittingBlock && WorldUtil.canInteract()) {
            blockingTicks++;
            var use = Adjust.mouseClickHelper.right;
            blocking = use.isClicked(false);
            if (blockingTicks == 1) {
                if (blocking) {
                    use.clicks = Integer.valueOf(1);
                    use.down = Boolean.TRUE;
                }
                Adjust.slotSpoofer.field17 = false;
                Adjust.slotSpoofer.active = false;
                updatePending = true;
                releasePending = true;
            } else {
                if (Adjust.slotSpoofer.active) {
                    mc.rightClickDelayTimer = 1;
                    if (specialSwap) use.disabled = true;
                } else {
                    use.disabled = true;
                }
                outboundHold.acquire(outboundHold.field16);
                blockingTicks = 0;
                specialSwap = false;
            }
            return;
        }
        updatePending = false;
        blocking = false;
        blockingTicks = 0;
        active = false;
        specialSwap = false;
        clearOrphanedNoSlowSpoof(false);
        outboundHold.release();
        synchronized (outboundBatch.holds) {
            while (!outboundBatch.holds.isEmpty()) outboundBatch.queue.removeHold(outboundBatch.holds.removeFirst());
        }
    }

    public boolean swapSlot() {
        if (Adjust.slotSpoofer.active) return false;
        int start = RandomUtil.nextInt(RandomUtil.RANDOM, 0, 9);
        Integer slot = WorldUtil.findHotbarSlot(start,
                candidate -> isDifferentSlot(candidate));
        if (slot == null) return false;
        Adjust.slotSpoofer.slot = slot;
        Adjust.slotSpoofer.active = true;
        Adjust.slotSpoofer.field8 = Adjust.slotSpoofer.field12;
        Adjust.slotSpoofer.field17 = true;
        Adjust.slotSpoofer.renderSpoofed = false;
        return true;
    }

    public void resetState() {
        updatePending = false;
        blocking = false;
        blockingTicks = 0;
        active = false;
        specialSwap = false;
        clearOrphanedNoSlowSpoof(false);
        outboundHold.release();
        synchronized (outboundBatch.holds) {
            while (!outboundBatch.holds.isEmpty()) outboundBatch.queue.removeHold(outboundBatch.holds.removeFirst());
        }
    }

    @EventHandler(priority = -1)
    public void onPacketReceive(DirectionalPacketEvent event) {
        if (!event.isCancelled() && isActive() && event.outgoing && blocking && event.packet instanceof C03PacketPlayer
                && ServiceUtil.isActiveModel(ServerType.HYPIXEL)) {
            outboundBatch.addHold();
        }
    }
}
