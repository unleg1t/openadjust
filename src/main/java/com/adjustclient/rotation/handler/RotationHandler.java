package com.adjustclient.rotation.handler;

import com.adjustclient.event.EventHandler;
import com.adjustclient.event.Listener;
import com.adjustclient.event.MouseDeltaEvent;
import com.adjustclient.rotation.RotationModel;
import com.adjustclient.util.MinecraftInstance;
import com.adjustclient.util.RotationUtil;
import com.adjustclient.util.Vec2f;
import com.adjustclient.util.anticheat.ServerTracker;
import com.adjustclient.game.player.InventoryStateTracker;

public final class RotationHandler implements Listener, MinecraftInstance {
    public static final RotationHandler INSTANCE = new RotationHandler();
    public RotationModel model;
    public Vec2f target;
    public Vec2f previous;
    public boolean active;
    public boolean requested;
    public boolean updating;
    public boolean field19;
    private Object field20;

    public static void reset() {
        RotationHandler state = INSTANCE;
        if (!state.active || state.requested) return;
        state.updating = true;
        if (!InventoryStateTracker.isInServersideGui() && mc.thePlayer != null)
            mc.entityRenderer.updateMouseInput(1.0F);
        state.updating = false;
        if (ServerTracker.INSTANCE.mouseOverOverridden) return;
        float old = mc.thePlayer.rotationYawHead;
        mc.thePlayer.rotationYawHead = mc.thePlayer.rotationYaw;
        mc.entityRenderer.getMouseOver();
        mc.thePlayer.rotationYawHead = old;
    }

    @EventHandler(priority = -20) public void onMouseInput(MouseDeltaEvent event) {
        if (previous == null || target == null || mc.thePlayer == null || !active) {
            field19 = false; updating = false; return;
        }
        if (!requested) {
            target = RenderRotationTracker.INSTANCE.rotation;
            if (target == null) { field19 = false; updating = false; return; }
        }
        if (updating) updating = false;
        float sensitivity = event.sensitivity;
        Vec2f next = model.apply(previous, target, event.partialTicks);
        float dy = (next.y - mc.thePlayer.rotationPitch) / 0.15F / sensitivity;
        if (!mc.gameSettings.invertMouse) dy *= -1.0F;
        event.deltaX = (next.x - mc.thePlayer.rotationYaw) / 0.15F / sensitivity;
        event.deltaY = dy;
        event.silent = true;
        if (!requested && RotationUtil.angularDistance(next, target) < 0.001D) {
            RenderRotationTracker.INSTANCE.unwrapPlayerYaw(next);
            field19 = false; model = null; target = null; active = false;
        }
    }

    public static void beginTick() {
        if (mc.thePlayer == null) return;
        release();
        RotationHandler state = INSTANCE;
        if (state.requested) {
            state.target = RenderRotationTracker.INSTANCE.rotation;
            state.requested = false;
        }
        state.previous = new Vec2f(mc.thePlayer.rotationYaw, mc.thePlayer.rotationPitch);
    }

    public static void rotate(RotationModel model, float yaw, float pitch) {
        RotationHandler state = INSTANCE;
        state.target = new Vec2f(yaw, pitch);
        state.model = model;
        state.requested = true;
        state.active = true;
        state.updating = true;
        if (InventoryStateTracker.isInServersideGui()) { state.updating = false; return; }
        if (mc.thePlayer != null) mc.entityRenderer.updateMouseInput(1.0F);
        state.updating = false;
    }

    public static void release() {
        RotationHandler state = INSTANCE;
        if (!state.active) return;
        state.updating = true;
        if (!InventoryStateTracker.isInServersideGui() && mc.thePlayer != null)
            mc.entityRenderer.updateMouseInput(1.0F);
        state.updating = false;
    }
}
