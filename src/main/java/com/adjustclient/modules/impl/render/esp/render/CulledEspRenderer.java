package com.adjustclient.modules.impl.render.esp.render;

import com.adjustclient.core.rendering.RenderUtil;
import com.adjustclient.utilities.anticheat.entity.TrackedEntity;
import net.minecraft.client.gui.ScaledResolution;
import org.joml.Vector4d;

public abstract class CulledEspRenderer extends EspRenderer {
    public TrackedEntity<?> target;

    @Override public Vector4d project(ScaledResolution resolution, float partialTicks) {
        if (target.entity.ignoreFrustumCheck) return target.getRenderPosition(resolution, partialTicks);
        net.minecraft.util.AxisAlignedBB bounds = target.entity.getEntityBoundingBox();
        var tracker = com.adjustclient.utilities.game.entity.tracker.ServerPositionTracker.INSTANCE;
        if (tracker != null) {
            var entry = tracker.findTrackedPosition(target.entity);
            if (entry != null) bounds = entry.boundingBox;
        }
        if (!RenderUtil.isInFrustum(bounds)) return null;
        return target.getRenderPosition(resolution, partialTicks);
    }
}
