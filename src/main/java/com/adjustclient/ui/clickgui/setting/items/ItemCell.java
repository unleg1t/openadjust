package com.adjustclient.ui.clickgui.setting.items;

import com.adjustclient.render.font.image.NvgDraw;
import com.adjustclient.util.Vec2f;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.item.ItemStack;
import org.lwjgl.opengl.GL11;
import com.adjustclient.render.RenderUtil;
import com.adjustclient.ui.clickgui.frame.Tooltip;
import com.adjustclient.ui.screen.AdjustComponent;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;

public final class ItemCell extends AdjustComponent {

    public static final Map<Object, Object> SUBSTITUTES = new HashMap<>();

    static {
        SUBSTITUTES.put(Blocks.water, Items.water_bucket);
        SUBSTITUTES.put(Blocks.lava, Items.lava_bucket);
        SUBSTITUTES.put(Blocks.fire, Items.flint_and_steel);
        SUBSTITUTES.put(Blocks.brewing_stand, Items.brewing_stand);
        SUBSTITUTES.put(Blocks.end_portal, Items.ender_eye);
        SUBSTITUTES.put(Blocks.portal, Items.ender_pearl);
        SUBSTITUTES.put(Blocks.skull, Items.skull);   // Items.skull
        SUBSTITUTES.put(Blocks.bed, Items.bed);
        SUBSTITUTES.put(Blocks.iron_door, Items.iron_door);
        SUBSTITUTES.put(Blocks.oak_door, Items.oak_door);
        SUBSTITUTES.put(Blocks.dark_oak_door, Items.dark_oak_door);
        SUBSTITUTES.put(Blocks.spruce_door, Items.spruce_door);
        SUBSTITUTES.put(Blocks.birch_door, Items.birch_door);
        SUBSTITUTES.put(Blocks.acacia_door, Items.acacia_door);
        SUBSTITUTES.put(Blocks.jungle_door, Items.jungle_door);
        SUBSTITUTES.put(Blocks.redstone_wire, Items.redstone);
        SUBSTITUTES.put(Blocks.unpowered_repeater, Items.repeater);
        SUBSTITUTES.put(Blocks.unpowered_comparator, Items.comparator);
        SUBSTITUTES.put(Blocks.standing_sign, Items.sign);
        SUBSTITUTES.put(Blocks.standing_banner, Items.banner);
    }

    ItemListComponent owner;
    int id;
    ItemStack stack;
    Tooltip tooltip;
    boolean hovered;

    public void hover(com.adjustclient.event.Render2DEvent queue, Vec2f mouse, boolean isHovered) {
        if (isHovered) {
            RenderUtil.cursor = 221188; // cursor shape
        }
        this.hovered = isHovered;
        if (this.tooltip != null && queue.postTasks != null) {
            List<Runnable> deferred = queue.postTasks;
            deferred.add(() -> this.tooltip.drawDeferred(mouse));
        }
    }

    void queueHighlight(List<Runnable> deferred) {
        deferred.add(() -> {
            if (this.tooltip.hoverTimer == null) {
                return;
            }
            float t = (float) (System.currentTimeMillis() - this.tooltip.hoverTimer.lastMs) / (float) this.tooltip.delay;
            int color;
            if (t <= 1.0F) {
                int alpha = (int) (t * 35.0F);
                if (alpha < 25) {
                    return;
                }
                color = alpha << 24 | this.owner.getAccentColor() & 0xFFFFFF;
            } else {
                color = this.owner.getAccentColor() & 0xFFFFFF | 0x23000000;
            }
            NvgDraw.fillRect(color, this.x, this.y, this.x + this.width, this.y + this.height);
        });
    }

    public void drawIcon() {
        float x = this.x + 1.0F;
        float y = this.y + 1.0F;
        GL11.glScalef(0.7F, 0.7F, 0.7F);
        GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
        GL11.glPushMatrix();
        GL11.glRotatef(-30.0F, 0.0F, 1.0F, 0.0F);
        GL11.glRotatef(165.0F, 1.0F, 0.0F, 0.0F);
        RenderHelper.enableStandardItemLighting();
        GL11.glPopMatrix();
        GlStateManager.enableDepth();
        int alpha = (int) (200.0F * com.adjustclient.ui.clickgui.ClickGuiScreen.INSTANCE.fade.animation.value);
        Minecraft.getMinecraft().getRenderItem().renderItemIntoGUI(this.stack, alpha, 0, x * 1.4285715F, y * 1.4285715F);
        GlStateManager.disableDepth();
        RenderHelper.disableStandardItemLighting();
        GlStateManager.disableLighting();
        GL11.glScalef(1.4285715F, 1.4285715F, 1.4285715F);
        GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
    }
}
