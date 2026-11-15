package com.adjustclient.game.render;

import com.adjustclient.module.impl.render.chams.ChamsTargetSettings;
import com.adjustclient.module.impl.render.hud.HUDModule;
import java.awt.Color;
import java.util.Map;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.entity.Entity;

public final class FrozenPlayerRenderer {
    public static final Map<Entity, ChamsTargetSettings> ENTITY_SETTINGS = new java.util.HashMap<>();
    public String name;
    public ChamsTargetSettings settings;
    public Entity entity;
    public float opacity = 0.2F;
    public FrozenPlayerRenderer(String name) { this.name = name; }

    public void render(Entity entity, double x, double y, double z, float yaw, float partialTicks) {
        this.entity = entity;
        GlStateManager.color(1.0F, 1.0F, 0.3F, 0.2F);
        RenderManager manager = Minecraft.getMinecraft().getRenderManager();
        if (settings == null) settings = new ChamsTargetSettings(null, name, true, new Color(125, 125, 125));
        Color color = HUDModule.animatedColor != null ? HUDModule.animatedColor : HUDModule.colorSetting.getColor();
        Color translucent = com.adjustclient.render.ColorUtil.withAlpha(color, 100);
        settings.color.setValue(translucent);
        color = HUDModule.animatedColor != null ? HUDModule.animatedColor : HUDModule.colorSetting.getColor();
        settings.throughWallsColor.setValue(new Color((int) (color.getRed() * 0.7D),
                (int) (color.getGreen() * 0.7D), (int) (color.getBlue() * 0.7D), 100));
        ENTITY_SETTINGS.put(entity, settings);
        manager.doRenderEntity(entity, x - manager.renderPosX, y - manager.renderPosY, z - manager.renderPosZ, yaw, partialTicks, true);
        GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
    }
}
