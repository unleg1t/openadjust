package com.adjustclient.module.impl.movement;

import com.adjustclient.Adjust;
import com.adjustclient.event.CancellablePlayerUpdateEvent;
import com.adjustclient.event.EventHandler;
import com.adjustclient.event.IngameActionEvent;
import com.adjustclient.event.LowLevelPacketReceiveEvent;
import com.adjustclient.event.Render3DEvent;
import com.adjustclient.event.WorldResetEvent;
import com.adjustclient.game.player.InventoryStateTracker;
import com.adjustclient.game.render.FrozenPlayerRenderer;
import com.adjustclient.module.Category;
import com.adjustclient.module.Module;
import com.adjustclient.module.impl.player.GameSpeedModule;
import com.adjustclient.module.util.MouseClick;
import com.adjustclient.server.ServerType;
import com.adjustclient.service.util.ServiceUtil;
import com.adjustclient.setting.BooleanSetting;
import com.adjustclient.setting.ExpandSetting;
import com.adjustclient.util.Simulation;
import com.adjustclient.util.SimulationPlayer;
import com.adjustclient.util.WorldUtil;
import com.adjustclient.util.anticheat.ServerTracker;
import java.util.Map;
import net.minecraft.client.entity.EntityOtherPlayerMP;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;
import net.minecraft.network.play.server.S08PacketPlayerPosLook;
import net.minecraft.util.MovingObjectPosition;

public final class StasisModule extends Module {
    public static StasisModule INSTANCE;
    public final BooleanSetting update;
    public final BooleanSetting disableOnWorldJoin;
    public final BooleanSetting disableOnTeleport;
    public final ExpandSetting render;
    public final BooleanSetting landPrediction;
    private EntityOtherPlayerMP predictedLanding;
    private EntityOtherPlayerMP prevPredictedLanding;
    private final FrozenPlayerRenderer renderer = new FrozenPlayerRenderer("Stasis");
    private int phaseTicks;
    private double accumulatedTime;
    private double updateScale = 1.0;

    public StasisModule() {
        super("Stasis", Category.MOVEMENT);
        INSTANCE = this;
        update = new BooleanSetting(this, "Update", true);
        update.setDescription("Updates your position to prevent flags.");
        ExpandSetting disableOn = new ExpandSetting(this, "Disable on");
        disableOnWorldJoin = new BooleanSetting(disableOn, "World join", true);
        disableOnTeleport = new BooleanSetting(disableOn, "Teleport", true);
        render = new ExpandSetting(this, "Render");
        landPrediction = new BooleanSetting(render, "Land prediction", true);
        landPrediction.setDescription("Renders where you are predicted to land.");
    }

    @EventHandler(priority = 12)
    public void onIngameAction(IngameActionEvent event) {
        if (isEnabled() && !event.suppressActions) updateMouseButtons();
    }

    public void updateMouseButtons() {
        if (InventoryStateTracker.isInServersideGui()
                || !ServiceUtil.isActiveModel(ServerType.HYPIXEL) || !update.isEnabled()
                || mc.thePlayer.onGround) return;
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
        if (ServerTracker.INSTANCE.ticksSinceVelocity < 2) updateScale = 0.5;
        else if (!ServerTracker.INSTANCE.field204 && !active) updateScale = 0.5;
        else {
            updateScale = 1.0;
            if (!ServerTracker.INSTANCE.field204 && Adjust.mouseClickHelper.right.isClicked(false)
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
        double scale = !InventoryStateTracker.isInServersideGui()
                && ServiceUtil.isActiveModel(ServerType.HYPIXEL) && update.isEnabled() ? updateScale : 1.0;
        double elapsed = scale * 50.0;
        updateScale = 1.0;
        if (mc.thePlayer.onGround || mc.thePlayer.isOnLadder()) {
            setEnabled(false, true);
            return;
        }
        simulateLanding();
        if (!update.isEnabled()) {
            phaseTicks = 0;
            accumulatedTime = 0.0;
            WorldUtil.cancelMovementTick(event, true);
            return;
        }
        int velocityTicks = ServerTracker.INSTANCE.ticksSinceVelocity;
        if (velocityTicks < 2) {
            phaseTicks = 0;
        }
        double total = accumulatedTime + elapsed;
        accumulatedTime = total;
        if (total >= 50.0) {
            accumulatedTime = scale == 1.0 ? 0.0 : total - 50.0;
            if (velocityTicks < 2) return;
            if (++phaseTicks >= 20) {
                phaseTicks = 0;
                return;
            }
        }
        WorldUtil.cancelMovementTick(event, total >= 50.0);
    }

    private void simulateLanding() {
        prevPredictedLanding = predictedLanding;
        predictedLanding = null;
        if (!landPrediction.isEnabled()) {
            prevPredictedLanding = null;
            return;
        }
        Simulation simulation = new Simulation(mc.thePlayer);
        SimulationPlayer player = simulation.player;
        float forward = (mc.gameSettings.keyBindForward.pressed ? 1.0F : 0.0F)
                - (mc.gameSettings.keyBindBack.pressed ? 1.0F : 0.0F);
        float strafe = (mc.gameSettings.keyBindLeft.pressed ? 1.0F : 0.0F)
                - (mc.gameSettings.keyBindRight.pressed ? 1.0F : 0.0F);
        boolean sneak = mc.gameSettings.keyBindSneak.pressed;
        boolean jump = mc.gameSettings.keyBindJump.pressed;
        if (sneak) {
            player.movementInput.moveForward = forward * 0.3F;
            player.movementInput.moveStrafe = strafe * 0.3F;
            player.movementInput.jump = jump;
            player.setSneaking(true);
        } else {
            player.movementInput.moveForward = forward;
            player.movementInput.moveStrafe = strafe;
            player.movementInput.jump = jump;
            player.setSneaking(false);
        }
        for (int tick = 0; tick < 100; tick++) {
            player.tick();
            if (player.onGround || player.isOnLadder()) {
                recordLanding(player);
                break;
            }
            if (player.posY < 0.0D) break;
        }
        if (predictedLanding == null) {
            prevPredictedLanding = null;
        }
    }

    private void recordLanding(SimulationPlayer player) {
        EntityOtherPlayerMP ghost = new EntityOtherPlayerMP(player.worldObj, mc.thePlayer.getGameProfile());
        predictedLanding = ghost;
        ghost.setPositionAndRotation(player.posX, player.posY, player.posZ, player.rotationYaw, player.rotationPitch);
        ghost.prevRotationYaw = player.prevRotationYaw;
        ghost.rotationYawHead = player.rotationYawHead;
        ghost.prevRotationYawHead = player.prevRotationYawHead;
        ghost.prevRotationPitch = player.prevRotationPitch;
    }

    @EventHandler
    public void onRender3D(Render3DEvent event) {
        if (!isEnabled() || !landPrediction.isEnabled() || predictedLanding == null) return;
        double x = predictedLanding.posX;
        double y = predictedLanding.posY;
        double z = predictedLanding.posZ;
        float yaw = predictedLanding.rotationYaw;
        float pt = event.partialTicks;
        if (prevPredictedLanding != null) {
            x = prevPredictedLanding.posX + (x - prevPredictedLanding.posX) * pt;
            y = prevPredictedLanding.posY + (y - prevPredictedLanding.posY) * pt;
            z = prevPredictedLanding.posZ + (z - prevPredictedLanding.posZ) * pt;
            yaw = prevPredictedLanding.rotationYaw
                    + (((yaw - prevPredictedLanding.rotationYaw) + 180.0F) % 360.0F - 180.0F) * pt;
        }
        renderer.render(predictedLanding, x, y, z, yaw, pt);
    }

    @Override
    public void onEnable() {
        phaseTicks = 0;
        accumulatedTime = 0.0;
        updateScale = 1.0;
        predictedLanding = null;
        prevPredictedLanding = null;
    }

    @Override
    public void onDisable() {
        predictedLanding = null;
        prevPredictedLanding = null;
    }

    @EventHandler
    public void onWorldChange(WorldResetEvent event) {
        if (!isEnabled()) return;
        phaseTicks = 0;
        accumulatedTime = 0.0;
        updateScale = 1.0;
        predictedLanding = null;
        prevPredictedLanding = null;
        if (disableOnWorldJoin.isEnabled()) {
            toggle(false);
            disableConflictingModules(GameSpeedModule.class);
        }
    }

    @EventHandler
    public void onPacketReceive(LowLevelPacketReceiveEvent event) {
        if (!isEnabled()) return;
        if (event.packet instanceof S08PacketPlayerPosLook && disableOnTeleport.isEnabled()) {
            toggle(false);
            disableConflictingModules(GameSpeedModule.class);
        }
    }
}
