package com.adjustclient.rotation.handler;

import com.adjustclient.event.EventHandler;
import com.adjustclient.event.Listener;
import com.adjustclient.event.MouseDeltaEvent;
import com.adjustclient.event.MoveFlyingEvent;
import com.adjustclient.event.WorldResetEvent;
import com.adjustclient.util.MinecraftInstance;
import com.adjustclient.util.Vec2f;
import net.minecraft.util.MathHelper;
import com.adjustclient.module.impl.movement.MovementFixModule;
import com.adjustclient.module.impl.render.ThirdPersonModule;

public final class RenderRotationTracker implements Listener, MinecraftInstance {
    public static final RenderRotationTracker INSTANCE = new RenderRotationTracker();
    public Vec2f rotation;
    public boolean disabled;
    private Object field12;

    @EventHandler(priority = -10) public void onMouseInput(MouseDeltaEvent event) {
        boolean synthetic = mc.cameraOverride != null && event.silent;
        if (mc.thePlayer == null) return;
        if (mc.cameraOverride != null && !synthetic) return;
        if (rotation == null) rotation = new Vec2f(mc.thePlayer.rotationYaw, mc.thePlayer.rotationPitch);
        float dx = event.sensitivity * event.deltaX;
        float dy = event.sensitivity * event.deltaY;
        if (mc.gameSettings.smoothCamera && !synthetic) {
            mc.entityRenderer.smoothCamYaw += dx;
            mc.entityRenderer.smoothCamPitch += dy;
            float elapsed = event.partialTicks - mc.entityRenderer.smoothCamPartialTicks;
            dx = mc.entityRenderer.smoothCamFilterX * elapsed;
            dy = mc.entityRenderer.smoothCamFilterY * elapsed;
        }
        int invert = mc.gameSettings.invertMouse ? -1 : 1;
        rotation = new Vec2f(rotation.x + dx * 0.15F,
                MathHelper.clamp_float(rotation.y - invert * dy * 0.15F, -90.0F, 90.0F));
    }

    @EventHandler public void onStrafe(MoveFlyingEvent event) {
        if (event.pre && RotationHandler.INSTANCE.active
                && !MovementFixModule.INSTANCE.isEnabled() && !disabled && rotation != null) {
            event.rotationYaw = rotation.x;
        }
    }

    public void unwrapPlayerYaw(Vec2f angles) {
        if (ThirdPersonModule.INSTANCE.freeLooking) return;
        if (mc.cameraOverride != null) return;
        mc.thePlayer.prevRenderArmYaw = angles.x
                + MathHelper.wrapAngleTo180_float(mc.thePlayer.prevRenderArmYaw - angles.x);
        mc.thePlayer.renderArmYaw = angles.x
                + MathHelper.wrapAngleTo180_float(mc.thePlayer.renderArmYaw - angles.x);
        rotation = null;
    }

    @EventHandler public void onWorldReset(WorldResetEvent event) {
        rotation = null;
        disabled = false;
        RotationHandler state = RotationHandler.INSTANCE;
        state.active = false;
        state.requested = false;
        state.updating = false;
        state.field19 = false;
        state.model = null;
        state.target = null;
        state.previous = null;
    }

    public static Vec2f getRotation() {
        Vec2f value = INSTANCE.rotation;
        return value != null ? new Vec2f(value.x, value.y) : new Vec2f(mc.thePlayer.rotationYaw, mc.thePlayer.rotationPitch);
    }
}
