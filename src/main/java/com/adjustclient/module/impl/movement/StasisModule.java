package com.adjustclient.module.impl.movement;

import com.adjustclient.Adjust;
import com.adjustclient.event.*;
import com.adjustclient.module.Category;
import com.adjustclient.module.Module;
import com.adjustclient.module.util.MouseClick;
import com.adjustclient.server.ServerType;
import com.adjustclient.service.util.ServiceUtil;
import com.adjustclient.setting.BooleanSetting;
import com.adjustclient.setting.ExpandSetting;
import java.util.Map;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;
import net.minecraft.util.MovingObjectPosition;
import net.minecraft.init.Blocks;
import com.adjustclient.game.player.InventoryStateTracker;

public final class StasisModule extends Module {
    public static StasisModule INSTANCE;
    public final BooleanSetting update;
    public final BooleanSetting disableOnWorldJoin;
    public final BooleanSetting disableOnTeleport;
    private int phaseTicks;
    private boolean longPhase;
    private double accumulatedTime;
    private double updateScale = 1.0;
    private boolean updating;

    public StasisModule() {
        super("Stasis", Category.MOVEMENT);
        INSTANCE = this;
        update = new BooleanSetting(this, "Update", true);
        update.setDescription("Updates your position to prevent flags.");
        ExpandSetting disableOn = new ExpandSetting(this, "Disable on");
        disableOnWorldJoin = new BooleanSetting(disableOn, "World join", true);
        disableOnTeleport = new BooleanSetting(disableOn, "Teleport", true);
    }

    @EventHandler(priority = 12) public void onIngameAction(IngameActionEvent event) {
        if (isEnabled() && !event.suppressActions) updateMouseButtons();
    }

    public void updateMouseButtons() {
        if (InventoryStateTracker.inServerGui || InventoryStateTracker.inServerInventory
                || !ServiceUtil.isActiveModel(ServerType.HYPIXEL) || !update.isEnabled()) return;
        ItemStack stack = mc.thePlayer.inventory.getCurrentItem();
        Map<?, MouseClick> buttons = Adjust.mouseClickHelper.buttons;
        boolean anyPressed = false;
        for (MouseClick button : buttons.values()) {
            if (button.isPressed(false) || button.isClicked(false)) {
                anyPressed = true;
                break;
            }
        }
        boolean active = false;
        if (anyPressed) {
            MouseClick use = Adjust.mouseClickHelper.right;
            if (!use.isPressed(false) && !use.isClicked(false)) {
                active = true;
            } else if (mc.thePlayer.isUsingItem() || stack == null || !(stack.getItem() instanceof ItemBlock)) {
                active = true;
            } else {
                MovingObjectPosition hit = mc.objectMouseOver;
                active = hit != null && hit.typeOfHit == MovingObjectPosition.MovingObjectType.BLOCK
                        && ((ItemBlock) stack.getItem()).canPlaceBlockOnSide(mc.theWorld, hit.getBlockPos(), hit.sideHit,
                                mc.thePlayer, stack);
            }
        }
        if (com.adjustclient.util.anticheat.ServerTracker.INSTANCE.ticksSinceVelocity < 2) updateScale = 0.5;
        else if (!com.adjustclient.util.anticheat.ServerTracker.INSTANCE.field204 && !active) updateScale = 0.5;
        else {
            updateScale = 1.0;
            if (!com.adjustclient.util.anticheat.ServerTracker.INSTANCE.field204 && Adjust.mouseClickHelper.right.isClicked(false)
                    && !Adjust.mouseClickHelper.left.isPressed(false)
                    && !Adjust.mouseClickHelper.left.isClicked(false)
                    && stack != null && stack.getItem() instanceof ItemBlock) updateScale = 0.7;
        }
        if (updateScale * 50.0 + accumulatedTime >= 50.0) return;
        for (MouseClick button : buttons.values()) {
            if (button == Adjust.mouseClickHelper.right && mc.thePlayer.isUsingItem()) continue;
            button.disabled = true;
        }
    }

    @EventHandler(priority = -20)
    public void onPlayerUpdate(CancellablePlayerUpdateEvent event) {
        if (event.isCancelled() || !isEnabled()) return;
        updating = false;
        double scale = !InventoryStateTracker.inServerGui && !InventoryStateTracker.inServerInventory
                && ServiceUtil.isActiveModel(ServerType.HYPIXEL) && update.isEnabled() ? updateScale : 1.0;
        double elapsed = scale * 50.0;
        updateScale = 1.0;
        if (mc.thePlayer.onGround) {
            setEnabled(false, true);
            return;
        }
        net.minecraft.block.Block block = mc.theWorld.getBlockState(new net.minecraft.util.BlockPos(
                mc.thePlayer.posX, mc.thePlayer.getEntityBoundingBox().minY, mc.thePlayer.posZ)).getBlock();
        if ((block == Blocks.ladder || block == Blocks.vine) && !mc.thePlayer.isSpectator()) {
            setEnabled(false, true);
            return;
        }
        if (!update.isEnabled()) {
            phaseTicks = 0;
            accumulatedTime = 0.0;
            updateScale = 1.0;
            updating = false;
            longPhase = false;
            com.adjustclient.util.WorldUtil.cancelMovementTick(event, true);
            return;
        }
        int velocityTicks = com.adjustclient.util.anticheat.ServerTracker.INSTANCE.ticksSinceVelocity;
        if (velocityTicks < 2) {
            phaseTicks = 0;
            longPhase = false;
        }
        double total = accumulatedTime + elapsed;
        accumulatedTime = total;
        if (total >= 50.0) {
            accumulatedTime = scale == 1.0 ? 0.0 : total - 50.0;
            if (velocityTicks < 2) return;
        }
        if (longPhase) {
            if (++phaseTicks >= 280) {
                phaseTicks = 0;
                longPhase = false;
                accumulatedTime = 0.0;
                return;
            }
        } else if (total >= 50.0 && ++phaseTicks >= 50) {
            phaseTicks = 0;
            longPhase = true;
            return;
        }
        updating = true;
        com.adjustclient.util.WorldUtil.cancelMovementTick(event, total >= 50.0);
    }

    @Override public void onEnable() {
        phaseTicks = 0;
        accumulatedTime = 0.0;
        updateScale = 1.0;
        updating = false;
        longPhase = false;
    }

    @Override public void onDisable() {
        phaseTicks = 0;
        accumulatedTime = 0.0;
        updateScale = 1.0;
        updating = false;
        longPhase = false;
    }

    @EventHandler public void onWorldChange(WorldResetEvent event) {
        if (!isEnabled()) return;
        phaseTicks = 0;
        accumulatedTime = 0.0;
        updateScale = 1.0;
        updating = false;
        longPhase = false;
        if (disableOnWorldJoin.isEnabled()) disableConflictingModules(com.adjustclient.module.impl.player.GameSpeedModule.class);
    }

    @EventHandler public void onPacketReceive(LowLevelPacketReceiveEvent event) {
        if (!isEnabled()) return;
        if (event.packet instanceof net.minecraft.network.play.server.S08PacketPlayerPosLook
                && disableOnTeleport.isEnabled()) disableConflictingModules(com.adjustclient.module.impl.player.GameSpeedModule.class);
    }

    @EventHandler public void onPacketSend(DirectionalPacketEvent event) {
        if (event.isCancelled() || !isEnabled() || mc.thePlayer == null || !event.outgoing || !updating
                || !ServiceUtil.isActiveModel(ServerType.HYPIXEL) || !update.isEnabled()) return;
        if (event.packet instanceof net.minecraft.network.play.client.C03PacketPlayer packet
                && !(packet instanceof net.minecraft.network.play.client.C03PacketPlayer.C05PacketPlayerLook)
                && !packet.forkField39) {
            event.packet = new net.minecraft.network.play.client.C03PacketPlayer.C05PacketPlayerLook(
                    mc.thePlayer.rotationYaw, mc.thePlayer.rotationPitch, packet.isOnGround());
        }
    }
}
