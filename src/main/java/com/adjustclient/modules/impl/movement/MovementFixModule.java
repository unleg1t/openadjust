package com.adjustclient.modules.impl.movement;

import com.adjustclient.modules.impl.combat.displacement.DisplacementModule;
import com.adjustclient.modules.impl.movement.scaffold.ScaffoldModule;
import com.adjustclient.core.rotation.handler.RenderRotationTracker;
import com.adjustclient.utilities.RotationUtil;
import com.adjustclient.utilities.Vec2f;
import net.minecraft.util.Vec3;
import com.adjustclient.core.events.EventHandler;
import com.adjustclient.core.events.MovementInputEvent;
import com.adjustclient.modules.Category;
import com.adjustclient.modules.Module;
import com.adjustclient.core.settings.BooleanSetting;
import com.adjustclient.utilities.MovementUtil;
import net.minecraft.util.MathHelper;

public final class MovementFixModule extends Module {
    public static MovementFixModule INSTANCE;
    public final BooleanSetting optimizeMovement;
    public final BooleanSetting optimizeScaffold;

    public MovementFixModule() {
        super("Movement Fix", Category.MOVEMENT);
        INSTANCE = this;
        optimizeMovement = new BooleanSetting(this, "Optimize movement", true);
        optimizeMovement.setDescription("Optimizes keys to move closer to your client rotations.");
        optimizeScaffold = new BooleanSetting(this, "Optimize scaffold", true);
        optimizeScaffold.setDescription("Moves you closer to the center of the block you are scaffolding on.");
    }

    @EventHandler public void onMovementInput(MovementInputEvent event) {
        if (!isEnabled() || !optimizeMovement.isEnabled() || DisplacementModule.INSTANCE.displaced) return;
        float[] movement = event.getMovement();
        if (movement[0] == 0.0F && movement[1] == 0.0F) return;
        Vec2f rotation = RenderRotationTracker.INSTANCE.rotation;
        float sourceYaw = rotation == null ? mc.thePlayer.rotationYaw : rotation.x;
        float desired = MovementUtil.getDirection(sourceYaw, movement[0], movement[1]);
        ScaffoldModule scaffold = ScaffoldModule.INSTANCE;
        if (optimizeScaffold.isEnabled() && scaffold.isEnabled()
                && !(scaffold.settings.isStableYaw() && mc.thePlayer.onGround && !MovementUtil.isJumping())
                && scaffold.lastPlacement != null && scaffold.lastPlacement.placedPos != null) {
            Vec3 position = new Vec3(mc.thePlayer.posX, mc.thePlayer.posY, mc.thePlayer.posZ);
            net.minecraft.util.BlockPos pos = scaffold.lastPlacement.placedPos;
            Vec3 center = new Vec3(pos.getX() + 0.5D, pos.getY() + 0.5D, pos.getZ() + 0.5D);
            double dx = position.xCoord - center.xCoord;
            double dz = position.zCoord - center.zCoord;
            if (Math.sqrt(dx * dx + dz * dz) > 0.001D) {
                float towardCenter = RotationUtil.getYawBetween(center, position).x;
                float difference = Math.abs(MathHelper.wrapAngleTo180_float(towardCenter - desired));
                if (difference > 10.0F && difference <= 55.0F) desired = towardCenter;
            }
        }
        Float closest = null;
        for (float forward = -1.0F; forward <= 1.0F; forward += 1.0F) {
            for (float strafe = -1.0F; strafe <= 1.0F; strafe += 1.0F) {
                if (forward == 0.0F && strafe == 0.0F) continue;
                float direction = MovementUtil.getDirection(mc.thePlayer.rotationYaw, forward, strafe);
                float difference = Math.abs(MathHelper.wrapAngleTo180_float(desired - direction));
                if (closest == null || closest > difference) {
                    movement[0] = forward;
                    movement[1] = strafe;
                    closest = difference;
                }
            }
        }
        event.back = movement[0] < 0.0F;
        event.forward = movement[0] > 0.0F;
        event.left = movement[1] > 0.0F;
        event.right = movement[1] < 0.0F;
    }

    @Override public void onDisable() {
        if (!SafeWalkModule.INSTANCE.eagleEnabled.isEnabled()) {
            SafeWalkModule.INSTANCE.sneakTicks = 0;
            SafeWalkModule.INSTANCE.sneaking = false;
        }
    }

    @Override public String getDescription() { return "Updates your movement based on your server rotations."; }

    @Override public String getAdditionalDescription() { return "Required for predictive anti-cheats."; }
}
