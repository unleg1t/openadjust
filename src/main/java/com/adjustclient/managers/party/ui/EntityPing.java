package com.adjustclient.managers.party.ui;

import com.adjustclient.modules.impl.render.hud.HUDModule;
import com.adjustclient.core.rendering.RenderUtil;
import com.adjustclient.core.rendering.font.*;
import com.adjustclient.managers.user.ClientUser;
import com.adjustclient.utilities.TimeUtil;
import com.adjustclient.utilities.Vec2f;
import java.awt.Color;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.entity.Entity;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.util.*;
import org.joml.Vector4d;

final class EntityPing extends BlockPing {
    public Entity entity;
    public int entityId;
    public EntityPing(int entityId, Vec3 position, ClientUser user) {
        super(position, user);
        this.entityId = entityId;
        update();
    }

    public AxisAlignedBB getMarkerBox(float partialTicks) {
        if (entity == null) return new AxisAlignedBB(position.xCoord + 0.15, 0.0, position.zCoord + 0.15,
                position.xCoord + 0.85, 255.0, position.zCoord + 0.85);
        double x = entity.lastTickPosX + (entity.posX - entity.lastTickPosX) * partialTicks;
        double y = entity.lastTickPosY + (entity.posY - entity.lastTickPosY) * partialTicks;
        double z = entity.lastTickPosZ + (entity.posZ - entity.lastTickPosZ) * partialTicks;
        AxisAlignedBB bounds = entity.getEntityBoundingBox();
        double half = (bounds.maxX - bounds.minX) * 0.5;
        double top = y + bounds.maxY - bounds.minY;
        return new AxisAlignedBB(x - half - 0.05, top + 0.1, z - half - 0.05,
                x + half + 0.05, top + 0.3, z + half + 0.05);
    }

    @Override public Vector4d project(ScaledResolution resolution, float partialTicks) {
        Minecraft.getMinecraft().entityRenderer.setupCameraTransform(partialTicks, 0);
        if (entity == null) return super.project(resolution, partialTicks);
        double x = entity.lastTickPosX + (entity.posX - entity.lastTickPosX) * partialTicks;
        double y = entity.lastTickPosY + (entity.posY - entity.lastTickPosY) * partialTicks;
        double z = entity.lastTickPosZ + (entity.posZ - entity.lastTickPosZ) * partialTicks;
        if (entity instanceof EntityItem) {
            EntityItem item = (EntityItem) entity;
            y += MathHelper.sin(item.getAge() / 10.0F + item.hoverStart) * 0.1F + 0.1F;
        }
        return RenderUtil.worldToScreen(new Vec3(x, y + entity.height + 0.75, z), new Vec2f(0.0F, 0.0F), resolution);
    }

    @Override public void drawLabel(int alpha, float x, float y) {
        int color = new Color(233, 235, 234, alpha).getRGB();
        String title = entity != null ? entity.getName() : "Entity";
        Fonts.TAHOMA_12.drawString(title, x - Fonts.TAHOMA_12.getStringWidth(title) * 0.5F, y, color);
        Fonts.TAHOMA_10.drawCenteredString(user != null ? user.name : "Ping", x, y + 6.0F, color);
        Fonts.TAHOMA_8.drawCenteredString(
                TimeUtil.formatDuration(5000L - (System.currentTimeMillis() - lifetime.lastMs), true), x, y + 11.0F, color);
    }

    @Override public void drawWorld(RenderManager manager, double x, double z, float opacity, float partialTicks) {
        if (entity == null) { super.drawWorld(manager, x, z, opacity, partialTicks); return; }
        int alpha = (int)(opacity * 190.0F);
        if (alpha < 25) return;
        RenderUtil.begin3d();
        AxisAlignedBB bounds = getMarkerBox(partialTicks);
        double dx = x - bounds.minX, dz = z - bounds.minZ;
        double distance = dx * dx + dz * dz;
        if (distance < 48.0) alpha = (int)(alpha * (distance >= 16.0 ? distance / 48.0 : 1.0 / 3.0));
        int color = user != null ? user.getColor() : HUDModule.getHudColor().getRGB();
        RenderUtil.glColor((alpha << 24) | (color & 0xFFFFFF));
        RenderUtil.drawFilledBox(bounds.offset(-manager.viewerPosX, -manager.viewerPosY, -manager.viewerPosZ));
        RenderUtil.end3d();
    }

    @Override public void update() {
        Minecraft mc = Minecraft.getMinecraft();
        if (mc.theWorld == null) return;
        if (entity == null) entity = mc.theWorld.getEntityByID(entityId);
        else if (!mc.theWorld.loadedEntityList.contains(entity)) entity = null;
    }
}
