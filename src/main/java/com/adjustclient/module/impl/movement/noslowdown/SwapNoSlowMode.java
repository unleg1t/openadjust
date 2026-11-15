package com.adjustclient.module.impl.movement.noslowdown;

import com.adjustclient.Adjust;
import com.adjustclient.anticheat.feature.Features;
import com.adjustclient.event.*;
import com.adjustclient.game.packet.PacketHold;
import com.adjustclient.setting.BooleanSetting;
import com.adjustclient.setting.ModeSetting;
import com.adjustclient.util.RandomUtil;
import com.adjustclient.util.WorldUtil;
import com.adjustclient.game.player.InventoryStateTracker;

public final class SwapNoSlowMode extends NoSlowMode {
    public static SwapNoSlowMode INSTANCE;
    public final ModeSetting<SwapTiming> swapMode;
    public final BooleanSetting sneaking;
    public final BooleanSetting slowness;
    public final BooleanSetting swordsOnly;
    public PacketHold hold;
    public int blockingTicks;
    public boolean blocking;
    public boolean updatePending;
    public boolean active;

    public SwapNoSlowMode(NoSlowdownModule module) {
        super(module, "Swap", Features.NO_SLOW_SWAP);
        INSTANCE = this;
        swapMode = new ModeSetting<>(module, "Swap Mode", SwapTiming.ONCE,
                () -> module.mode.getValue() != this, SwapTiming.values());
        module.getSettings().remove(swapMode);
        module.getSettings().add(1, swapMode);
        swordsOnly = new BooleanSetting(module, "Swords only", true,
                () -> module.mode.getValue() != this);
        swordsOnly.setDisplayName("swapSwordsOnly");
        sneaking = new BooleanSetting(module, "Sneaking", true,
                () -> module.mode.getValue() != this);
        sneaking.setDisplayName("swapSneaking");
        slowness = new BooleanSetting(module, "Slowness", true,
                () -> module.mode.getValue() != this);
        slowness.setDisplayName("swapSlowness");
    }

    @EventHandler(priority = 71)
    public void onControllerUpdate(IngameActionEvent event) {
        if (!isActive()) return;
        blocking = false;
        if (!event.suppressActions && mc.thePlayer.ridingEntity == null
                && !InventoryStateTracker.inServerGui && !InventoryStateTracker.inServerInventory) {
            NoSlowItemType itemType = module.getItemType(true);
            if (itemType != null && (!swordsOnly.isEnabled() || itemType == NoSlowItemType.SWORD)
                    && Adjust.mouseClickHelper.right.isClicked(false)) {
                updateBlocking();
                return;
            }
        }
        updatePending = false;
        blocking = false;
        blockingTicks = 0;
        active = false;
        releaseHold();
        updatePending = false;
        blockingTicks = 0;
    }

    @EventHandler
    public void onSprint(SprintEvent event) {
        if (isActive() && event.state == SprintState.SLOWNESS && slowness.isEnabled()) event.cancelled = true;
    }

    @EventHandler
    public void onItemSlowdown(ItemSlowdownEvent event) {
        if (isActive() && (module.getItemType(true) == NoSlowItemType.SWORD || !swordsOnly.isEnabled()))
            event.apply = false;
    }

    @EventHandler
    public void onMotion(MotionEvent event) {
        if (!isActive() || !updatePending || !event.pre) return;
        releaseHold();
        beginHold();
    }

    @EventHandler
    public void onUpdate(ControllerUpdatePhaseEvent event) {
        if (!isActive()) return;
        if (updatePending && mc.thePlayer != null) {
            if (event.pre) {
                boolean swapped = swapSlot();
                if (swapped && swapMode.getValue() == SwapTiming.BLINK) {
                    Adjust.mouseClickHelper.field4ClickOverride.disabled = true;
                    Adjust.mouseClickHelper.left.disabled = true;
                }
                if (swapMode.getValue() == SwapTiming.BLINK) updatePending = false;
            } else {
                Adjust.slotSpoofer.field17 = false;
                Adjust.slotSpoofer.active = false;
                if (swapMode.getValue() == SwapTiming.CONSTANT) {
                    mc.thePlayer.clearItemInUse();
                    blockingTicks = 0;
                } else {
                    mc.playerController.syncCurrentPlayItem();
                }
                updatePending = false;
            }
        } else if (event.pre && !active) {
            updatePending = false;
            blocking = false;
            blockingTicks = 0;
            active = false;
            releaseHold();
        }
        if (event.pre) active = false;
    }

    @EventHandler
    public void onSneakSlowdown(MovementInputEvent event) {
        if (isActive() && sneaking.isEnabled()) event.sneakMultiplier = 1.0;
    }

    @EventHandler
    public void onItemInUse(ItemUseDurationEvent event) {
        if (isActive() && blocking) event.useDuration = 72000;
    }

    @EventHandler
    public void onWorldReset(WorldResetEvent event) {
        if (!isActive()) return;
        updatePending = false;
        blocking = false;
        blockingTicks = 0;
        active = false;
        releaseHold();
    }

    @Override
    public void onDisable() {
        updatePending = false;
        blocking = false;
        blockingTicks = 0;
        active = false;
        releaseHold();
    }

    @Override
    public boolean removesSlowdown() {
        return swapMode.getValue() != SwapTiming.ONCE;
    }

    @Override
    public boolean isBlocking() {
        return blocking;
    }

    public void beginHold() {
        if (hold == null) hold = Adjust.outboundPacketQueue.addHold(null, false);
    }

    public void updateBlocking() {
        active = true;
        if (mc.thePlayer == null || !WorldUtil.canInteract()) {
            if (!mc.gameSettings.keyBindUseItem.isKeyDown())
                Adjust.mouseClickHelper.right.disabled = true;
            releaseHold();
            updatePending = false;
            blockingTicks = 0;
            return;
        }
        int previousTicks = blockingTicks++;
        var use = Adjust.mouseClickHelper.right;
        blocking = use.isClicked(false);
        if (previousTicks != 0) {
            if (Adjust.slotSpoofer.active) {
                mc.rightClickDelayTimer = 1;
                blockingTicks = 0;
            }
            if (swapMode.getValue() == SwapTiming.BLINK) beginHold();
            else releaseHold();
        } else {
            if (blocking) {
                if (!use.previousDown) use.clicks = Integer.valueOf(1);
                use.down = Boolean.TRUE;
            }
            Adjust.slotSpoofer.field17 = false;
            Adjust.slotSpoofer.active = false;
            updatePending = true;
        }
    }

    public boolean swapSlot() {
        if (Adjust.slotSpoofer.active) return false;
        int start = RandomUtil.nextInt(RandomUtil.RANDOM, 0, 9);
        Integer slot = WorldUtil.findHotbarSlot(start,
                candidate -> candidate != mc.thePlayer.inventory.m());
        if (slot == null) return false;
        Adjust.slotSpoofer.slot = slot;
        Adjust.slotSpoofer.active = true;
        Adjust.slotSpoofer.field8 = Adjust.slotSpoofer.field12;
        Adjust.slotSpoofer.field17 = true;
        Adjust.slotSpoofer.renderSpoofed = false;
        return true;
    }

    public void releaseHold() {
        if (hold != null) {
            Adjust.outboundPacketQueue.removeHold(hold);
            hold = null;
        }
    }
}
