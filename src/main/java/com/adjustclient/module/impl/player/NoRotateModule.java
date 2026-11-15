package com.adjustclient.module.impl.player;

import com.adjustclient.event.EventHandler;
import com.adjustclient.event.TeleportEvent;
import com.adjustclient.module.Category;
import com.adjustclient.module.Module;
import com.adjustclient.rotation.RotationMode;
import com.adjustclient.rotation.RotationSetting;
import com.adjustclient.rotation.handler.RenderRotationTracker;
import com.adjustclient.rotation.handler.RotationHandler;
import com.adjustclient.setting.BooleanSetting;
import com.adjustclient.setting.ExpandSetting;
import com.adjustclient.util.Vec2f;

public final class NoRotateModule extends Module {
    private final ExpandSetting rotations;
    public final RotationSetting rotation;
    public final BooleanSetting flagOnly;
    private Vec2f savedRotation;
    public static NoRotateModule INSTANCE;

    public NoRotateModule() {
        super("No Rotate", Category.PLAYER);
        INSTANCE = this;
        rotations = new ExpandSetting(this, "Rotations");
        rotation = new RotationSetting(rotations, RotationMode.INSTANT, null,
                RotationMode.LINEAR, RotationMode.DISTRIBUTED, RotationMode.STEP,
                RotationMode.ORGANIC, RotationMode.INSTANT);
        flagOnly = new BooleanSetting(this, "Flag only", true);
    }

    @EventHandler public void onTeleport(TeleportEvent event) {
        if (!isEnabled()) return;
        if (event.pre) {
            if (mc.thePlayer.addedToChunk && (!flagOnly.isEnabled()
                    || mc.thePlayer.getDistanceSq(event.x,
                    event.y, event.z) < 100.0)) {
                savedRotation = RenderRotationTracker.getRotation();
            }
            return;
        }
        if (savedRotation == null) return;
        RenderRotationTracker.INSTANCE.rotation = savedRotation;
        RotationHandler.rotate(rotation.createModel(), savedRotation.x, savedRotation.y);
        RotationHandler.INSTANCE.previous = new Vec2f(event.yaw, event.pitch);
        if (RotationHandler.INSTANCE.requested) {
            RotationHandler.INSTANCE.target = RenderRotationTracker.INSTANCE.rotation;
            RotationHandler.INSTANCE.requested = false;
        }
        savedRotation = null;
    }

    @Override public String getDescription() {
        return "Prevents the server from updating your rotations.";
    }
}
