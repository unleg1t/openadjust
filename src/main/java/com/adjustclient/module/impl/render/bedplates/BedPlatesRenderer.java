package com.adjustclient.module.impl.render.bedplates;

import com.adjustclient.render.RenderUtil;
import com.adjustclient.render.font.NvgContext;
import com.adjustclient.util.MinecraftInstance;
import com.adjustclient.util.Vec2f;
import java.util.Map;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.item.ItemStack;
import org.lwjgl.opengl.GL11;

public final class BedPlatesRenderer implements MinecraftInstance, com.adjustclient.event.Listener {
    private static final ItemStack EMPTY_STACK = new ItemStack(net.minecraft.init.Items.bed);
    private final BedPlatesModule module;

    static { }
    public BedPlatesRenderer(BedPlatesModule module) { this.module = module; }

    @com.adjustclient.event.EventHandler(priority = 50)
    public void renderAll(com.adjustclient.event.Render2DEvent event) {
        if (!module.isEnabled() || module.tracker == null) return;
        int configured = com.adjustclient.service.config.user.UserConfiguration.scale.getValue().intValue();
        double scale = configured == 16 ? 1000.0 : (double) (configured - 1) * 0.25 + 2.0;
        ScaledResolution resolution = event.scaledResolution;
        if (scale != 2.0) {
            resolution = new ScaledResolution(mc, 2.0);
            mc.entityRenderer.setupOverlayRendering(resolution);
            NvgContext.setScale((float) resolution.getScaleFactor_double());
        }
        Map<net.minecraft.util.BlockPos, BedPlate> plates = module.tracker.beds;
        GL11.glPushMatrix();
        synchronized (plates) {
            org.joml.Vector4d[] projected = new org.joml.Vector4d[plates.size()];
            int index = 0;
            boolean visible = false;
            for (BedPlate plate : plates.values()) {
                org.joml.Vector4d point = RenderUtil.worldToScreen(plate.center, new Vec2f(0.0F, 0.0F), resolution);
                projected[index++] = point;
                visible |= point != null;
            }
            if (mc.entityRenderer.worldProjectionActive) mc.entityRenderer.setupOverlayRendering(resolution);
            if (visible) {
                GlStateManager.disableLighting();
                GL11.glDisable(GL11.GL_LIGHTING);
                GlStateManager.disableBlend();
                renderPlate(projected, plates.values());
            }
        }
        GL11.glPopMatrix();
        if (scale != 2.0) {
            mc.entityRenderer.setupOverlayRendering(event.scaledResolution);
            NvgContext.setScale((float) event.scaledResolution.getScaleFactor_double());
        }
    }

    public Vec2f getPlateSize(BedPlate plate) {
        if (module.settings.mode.getValue() == BedPlatesMode.BLOCKS) {
            return new Vec2f(plate.blocks.size() * 8.0F, 8.0F);
        }
        float width = 0.0F;
        float height = 8.0F;
        for (BedLayer layer : plate.layers) {
            height = Math.max(height, layer.blocks.size() * 8.0F);
            width += 8.0F;
        }
        return new Vec2f(width, height);
    }

    private void renderPlate(org.joml.Vector4d[] projected, java.util.Collection<BedPlate> plates) {
        int index = 0;
        for (BedPlate plate : plates) {
            if (index >= projected.length) return;
            org.joml.Vector4d screen = projected[index++];
            if (screen == null) continue;
            int alpha = module.settings.getAlpha();
            Vec2f size = getPlateSize(plate);
            float x = (float) screen.x - size.x * 0.5F;
            float y = (float) screen.y - size.y * 0.5F;
            float left = x - 1.5F, top = y - 1.5F;
            float right = size.x + x + 1.5F, bottom = size.y + y + 1.5F;
            GL11.glClear(256);
            int background = plate.incomplete ? 0xAA5555 : 0;
            net.minecraft.client.gui.Gui.drawRect((Math.max(0, Math.min(255, alpha - 127)) << 24) | background,
                    left, top, right, bottom);
            int color = plate.team == null
                    ? com.adjustclient.module.impl.render.hud.HUDModule.getHudColor().getRGB()
                    : plate.team.dyeColor.getMapColor().colorValue;
            RenderUtil.drawBorderedRect(((alpha - 87) << 24) | (color & 0xFFFFFF),
                    left, top, right, bottom, 0.5F);
            if (module.settings.mode.getValue() == BedPlatesMode.BLOCKS) renderBlocks(plate, alpha, x, y);
            else renderLayers(plate, alpha, x, y);
        }
    }

    private void renderBlocks(BedPlate plate, int alpha, float x, float y) {
        for (Map.Entry<net.minecraft.block.Block, Integer> entry : plate.blocks.entrySet()) {
            net.minecraft.block.Block block = entry.getKey();
            ItemStack stack = block == net.minecraft.init.Blocks.bed ? EMPTY_STACK
                    : new ItemStack(net.minecraft.item.Item.getItemFromBlock(block), 1, Math.max(0, entry.getValue()));
            if (stack.getItem() == null) continue;
            renderItem(stack, alpha, x, y);
            x += 8.0F;
        }
    }

    private void renderItem(ItemStack stack, int alpha, float x, float y) {
        RenderHelper.enableGUIStandardItemLighting();
        GL11.glScalef(0.5F, 0.5F, 0.5F);
        mc.getRenderItem().renderItemIntoGUI(stack, alpha, true, x * 2.0F, y * 2.0F);
        GL11.glScalef(2.0F, 2.0F, 2.0F);
        GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
        RenderHelper.disableStandardItemLighting();
    }

    private void renderLayers(BedPlate plate, int alpha, float x, float y) {
        for (BedLayer layer : plate.layers) {
            float rowY = y;
            for (net.minecraft.block.Block block : layer.blocks.keySet()) {
                int metadata = layer.getMetadata(block);
                ItemStack stack = block == net.minecraft.init.Blocks.bed ? EMPTY_STACK
                        : new ItemStack(net.minecraft.item.Item.getItemFromBlock(block), 1, Math.max(0, metadata));
                if (stack.getItem() == null) continue;
                renderItem(stack, alpha, x, rowY);
                rowY += 8.0F;
            }
            x += 8.0F;
        }
    }
}
