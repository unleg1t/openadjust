package com.adjustclient.modules.impl.movement.sprint;

import com.adjustclient.Adjust;
import com.adjustclient.core.events.EventHandler;
import com.adjustclient.core.events.CancellableInputActionStageEvent;
import com.adjustclient.core.events.PreClientTickEvent;
import com.adjustclient.utilities.hypixel.HypixelGameState;
import com.adjustclient.utilities.hypixel.game.bedwars.BedWarsGameState;
import com.adjustclient.modules.impl.movement.antivoid.AntiVoidModule;
import com.adjustclient.core.rotation.InstantRotationModel;
import com.adjustclient.core.rotation.RotationModel;
import com.adjustclient.core.rotation.handler.RenderRotationTracker;
import com.adjustclient.core.rotation.handler.RotationHandler;
import com.adjustclient.utilities.MovementUtil;
import com.adjustclient.utilities.RandomUtil;
import com.adjustclient.utilities.WorldUtil;
import net.minecraft.client.settings.KeyBinding;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import com.adjustclient.modules.impl.movement.flight.FlightModule;
import com.adjustclient.modules.impl.movement.scaffold.ScaffoldModule;
import com.adjustclient.modules.impl.movement.speed.SpeedModule;
import net.minecraft.init.Items;
import com.adjustclient.modules.impl.combat.builder.BuilderModule;
import com.adjustclient.modules.impl.player.AntiAFKModule;

public final class RotateOmniSprintMode extends OmniSprintMode {
    public static RotateOmniSprintMode INSTANCE;
    private Object field12;
    public boolean suspended;
    public boolean restoreUseKey;
    public boolean rotated;

    public RotateOmniSprintMode(SprintModule module) { super(module, "Rotate"); INSTANCE = this; }

    @Override public void onDisable() { restoreUseKey = false; }

    @EventHandler
    public void onTick(PreClientTickEvent event) {
        if (isActive()) rotated = false;
    }

    @EventHandler(priority = 10)
    public void onClick(CancellableInputActionStageEvent event) {
        if (event.isCancelled() || !isActive()) return;
        if (restoreUseKey) {
            Adjust.mouseClickHelper.right.clicks = 1;
            Adjust.mouseClickHelper.right.down = Boolean.TRUE;
            Adjust.mouseClickHelper.right.holdTicks = 0;
            restoreUseKey = false;
            return;
        }
        if (!shouldSuspend()) {
            rotated = true;
            rotate(InstantRotationModel.INSTANCE, mc.thePlayer.rotationPitch, 0.015F);
            suspended = false;
            return;
        }
        if (!suspended && (mc.gameSettings.keyBindUseItem.pressed || mc.gameSettings.keyBindUseItem.pressTime >= 1)) {
            ItemStack stack = mc.thePlayer.inventory.getCurrentItem();
            if (stack != null) {
                Item item = stack.getItem();
                if (item == Items.fire_charge || item == Items.fishing_rod || item == Items.spawn_egg
                        || item == Items.egg || item == Items.snowball || item == Items.water_bucket
                        || item == Items.lava_bucket || item == Items.potionitem && (stack.getMetadata() & 0x4000) != 0) {
                    Adjust.mouseClickHelper.right.disabled = true;
                    restoreUseKey = true;
                }
            }
        }
        suspended = true;
    }

    @SuppressWarnings("unused")
    private static void vendorReportBypass() {
        com.adjustclient.managers.util.ServiceUtil.reportFeatureUse(
                com.adjustclient.managers.telemetry.BypassFeature.ROTATE_OMNI_SPRINT);
    }

    public void rotate(RotationModel model, float pitch, float yawSpread) {
        float yaw = RenderRotationTracker.getRotation().x;
        boolean back = mc.gameSettings.keyBindBack.pressed;
        if (mc.gameSettings.keyBindForward.pressed || back) {
            if (back) yaw += 180.0F;
        } else {
            if (mc.gameSettings.keyBindLeft.pressed) yaw -= 90.0F;
            if (mc.gameSettings.keyBindRight.pressed) yaw += 90.0F;
        }
        yaw = RandomUtil.jitter(yaw, yawSpread);
        float randomPitch = pitch + (RandomUtil.RANDOM.nextFloat() - 0.5F) * 0.03F;
        RotationHandler.rotate(model, yaw, randomPitch);
    }

    public static void rotate(float pitch, float yawSpread) {
        INSTANCE.rotate(InstantRotationModel.INSTANCE, pitch, yawSpread);
    }

    public static boolean shouldSuspend() {
        if (mc.thePlayer == null || !mc.gameSettings.keyBindSprint.pressed) return true;
        if (RotationHandler.INSTANCE.active && RotationHandler.INSTANCE.requested
                && (!mc.thePlayer.onGround || !MovementUtil.isJumping() || mc.playerController.isHittingBlock)) return true;
        if (BuilderModule.INSTANCE.blockIn.active) return true;
        var antiVoid = AntiVoidModule.INSTANCE.legit;
        if (antiVoid.safeSpot != null) return true;
        if (antiVoid.timer != null
                && !antiVoid.timer.hasElapsed(350L)) return true;
        if (SpeedModule.INSTANCE.isEnabled() || FlightModule.INSTANCE.isEnabled()) return true;
        if (mc.thePlayer.getFoodStats().getFoodLevel() <= 6.0F || mc.thePlayer.capabilities.isFlying
                || !MovementUtil.isMoving() || MovementUtil.isSneaking() || mc.thePlayer.isOnLadder()
                || mc.thePlayer.ridingEntity != null) return true;
        KeyBinding pick = mc.gameSettings.keyBindPickBlock;
        KeyBinding use = mc.gameSettings.keyBindUseItem;
        KeyBinding drop = mc.gameSettings.keyBindDrop;
        if (pick.pressed || pick.pressTime >= 1 || use.pressed || use.pressTime >= 1 || drop.pressed || drop.pressTime >= 1) return true;
        if (AntiAFKModule.INSTANCE.isEnabled() || ScaffoldModule.INSTANCE.isEnabled()) return true;
        HypixelGameState state = Adjust.gameStateTracker.state;
        if (state instanceof BedWarsGameState && state.lobby != null && !state.lobby) {
            if (!state.hasStartedFor(1000L)) return true;
            ItemStack stack = mc.thePlayer.inventory.getCurrentItem();
            if (stack != null && stack.getItem() == Items.fire_charge) return true;
        }
        return WorldUtil.isMovingTowards(RenderRotationTracker.getRotation().x);
    }
}
