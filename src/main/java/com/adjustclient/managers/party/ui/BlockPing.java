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
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.util.*;
import org.joml.Vector4d;

class BlockPing extends PartyPing {
    public Vec3 position;
    public AxisAlignedBB box;
    public BlockPing(Vec3 position, ClientUser user) { super(user); this.position = position; update(); }

    @Override public Vector4d project(ScaledResolution resolution, float partialTicks) {
        Minecraft.getMinecraft().entityRenderer.setupCameraTransform(partialTicks, 0);
        return RenderUtil.worldToScreen(new Vec3(position.xCoord + 0.5, position.yCoord + 2.5,
                position.zCoord + 0.5), new Vec2f(0.0F, 0.0F), resolution);
    }

    @Override public void drawLabel(int alpha, float x, float y) {
        String title = user != null ? user.name : "Ping";
        int color = new Color(233, 235, 234, alpha).getRGB();
        Fonts.TAHOMA_12.drawString(title, x - Fonts.TAHOMA_12.getStringWidth(title) * 0.5F, y, color);
        Minecraft mc = Minecraft.getMinecraft();
        double dx = mc.thePlayer.posX - position.xCoord;
        double dy = mc.thePlayer.posY - position.yCoord;
        double dz = mc.thePlayer.posZ - position.zCoord;
        String distance = Math.round(Math.sqrt(dx * dx + dy * dy + dz * dz)) + "m";
        Fonts.TAHOMA_10.drawString(distance, x - Fonts.TAHOMA_10.getStringWidth(distance) * 0.5F, y + 6.0F, color);
        String time = TimeUtil.formatDuration(5000L - (System.currentTimeMillis() - lifetime.lastMs), true);
        Fonts.TAHOMA_8.drawString(time, x - Fonts.TAHOMA_8.getStringWidth(time) * 0.5F, y + 11.0F, color);
    }

    @Override public void drawWorld(RenderManager manager, double x, double z, float opacity, float partialTicks) {
        int alpha = (int)(opacity * 135.0F);
        if (alpha < 25) return;
        RenderUtil.begin3d();
        GlStateManager.enableDepth();
        double dx = x - position.xCoord, dz = z - position.zCoord;
        double distance = dx * dx + dz * dz;
        if (distance < 48.0) alpha = (int)(alpha * (distance >= 16.0 ? distance / 48.0 : 1.0 / 3.0));
        int color = user != null ? user.getColor() : HUDModule.getHudColor().getRGB();
        RenderUtil.glColor((alpha << 24) | (color & 0xFFFFFF));
        RenderUtil.drawFilledBox(new AxisAlignedBB(position.xCoord + 0.15, 0.0, position.zCoord + 0.15,
                position.xCoord + 0.85, 255.0, position.zCoord + 0.85)
                .offset(-manager.viewerPosX, -manager.viewerPosY, -manager.viewerPosZ));
        if (box != null) RenderUtil.drawFilledBox(box.offset(-manager.viewerPosX, -manager.viewerPosY, -manager.viewerPosZ));
        RenderUtil.end3d();
    }

    @Override public void update() {
        Minecraft mc = Minecraft.getMinecraft();
        if (mc.theWorld == null) return;
        BlockPos pos = new BlockPos(position.xCoord, position.yCoord, position.zCoord);
        box = mc.theWorld.getBlockState(pos).getBlock().getSelectedBoundingBox(mc.theWorld, pos).expand(0.005, 0.005, 0.005);
    }
}
