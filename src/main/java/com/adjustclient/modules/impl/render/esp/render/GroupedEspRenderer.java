package com.adjustclient.modules.impl.render.esp.render;

import com.adjustclient.core.rendering.RenderUtil;
import java.util.Collection;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.entity.item.EntityItem;
import org.joml.Vector4d;
import net.minecraft.util.Vec3;

public abstract class GroupedEspRenderer extends EspRenderer {
    public java.util.function.Supplier<Vec3> positionSupplier;
    public Vec3 center;

    public Vector4d projectGroup(ScaledResolution resolution, float partialTicks) {
        if (!RenderUtil.isInFrustum(new net.minecraft.util.AxisAlignedBB(
                center.xCoord - 0.300000011920929, center.yCoord, center.zCoord - 0.300000011920929,
                center.xCoord + 0.300000011920929, center.yCoord + 1.799999952316284,
                center.zCoord + 0.300000011920929))) return null;
        net.minecraft.client.Minecraft.getMinecraft().entityRenderer.setupCameraTransform(partialTicks, 0);
        Vec3 interpolated = positionSupplier.get();
        return RenderUtil.worldToScreen(interpolated, new com.adjustclient.utilities.Vec2f(0.4F, 0.4F), resolution);
    }
}
