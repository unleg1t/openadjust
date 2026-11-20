package com.adjustclient.core.gui.clickgui.setting.hotbar;

import com.adjustclient.core.rendering.font.Fonts;
import com.adjustclient.core.rendering.font.NvgContext;
import com.adjustclient.core.rendering.font.image.NvgDraw;
import com.adjustclient.core.gui.clickgui.ClickGuiComponent;
import com.adjustclient.core.gui.clickgui.frame.HoverEffect;
import com.adjustclient.utilities.Vec2f;
import java.awt.Color;
import java.util.List;
import java.util.function.BooleanSupplier;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.item.ItemStack;
import org.joml.Vector4f;
import org.lwjgl.opengl.GL11;
import com.adjustclient.core.rendering.animation.Animation;
import com.adjustclient.core.gui.screen.AdjustComponent;

public class HotbarSlot extends AdjustComponent {

    HoverEffect colorBehaviour;
    ItemStack stack;
    int index;
    boolean hovered;

    HotbarSlot(ClickGuiComponent owner, int index) {
        this.index = index;
        BooleanSupplier never = () -> false;
        this.colorBehaviour = new HoverEffect(owner, never, this::isHovered, 1.0F, 0.5F);
    }

    boolean isHovered() {
        return this.hovered;
    }

    public void drawBackground(com.adjustclient.core.events.Render2DEvent queue, Vec2f mouse) {
        this.colorBehaviour.render(null, queue, null, mouse, false);
        Animation anim = this.colorBehaviour.k();
        float t = anim.value;
        int alpha = (int) (25.0F * t) + 50;
        if (t > 1.0F) {
            alpha = 75;
        }
        NvgContext.pushEvenScale();
        Vector4f px = NvgContext.toPixels(this.x, this.y, this.x + this.width, this.y + this.height);
        NvgDraw.fillRect(alpha << 24 | 0x7D7D7D, px.x, px.y, px.z, px.w);
        int hud = com.adjustclient.modules.impl.render.hud.HUDModule.getHudColor().getRGB() & 0xFFFFFF; // HUDModule colour (ColorSetting.getColor)
        Float scale = NvgContext.scaleFactor;
        float strokeWidth = scale == null ? 0.5F
                : (float) (NvgContext.roundScaledPixels ? Math.round(scale) : (int) scale.floatValue()) * 0.5F;
        NvgDraw.strokeRect(false, alpha << 24 | hud, px.x, px.y, px.z, px.w, strokeWidth);
        NvgContext.popEvenScale();
        this.drawContent(queue);
    }

    public void drawContent(com.adjustclient.core.events.Render2DEvent queue) {
        if (this.stack != null) {
            List<Runnable> deferred = queue.postTasks;
            deferred.add(this::drawItem);
            return;
        }
        float t = this.colorBehaviour.k().value;
        int alpha = (int) (25.0F * t) + 50;
        if (t > 1.0F) {
            alpha = 75;
        }
        String text = Integer.toString(this.index + 1);
        float cx = this.width / 2.0F + this.x;
        float cy = this.height / 2.0F + this.y - 1.0F;
        int color = (alpha * 3) << 24 | 0xC3C3C3;
        Fonts.TAHOMA_12.drawString(text, cx - Fonts.TAHOMA_12.getStringWidth(text) * 0.5F, cy, color);
    }

    void drawItem() {
        float x = this.x + 1.0F;
        float y = this.y + 1.0F;
        GL11.glScalef(0.5F, 0.5F, 0.5F);
        GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
        GL11.glPushMatrix();
        GL11.glRotatef(-30.0F, 0.0F, 1.0F, 0.0F);
        GL11.glRotatef(165.0F, 1.0F, 0.0F, 0.0F);
        RenderHelper.enableStandardItemLighting();
        GL11.glPopMatrix();
        GlStateManager.enableDepth();
        GlStateManager.disableAlpha();
        GlStateManager.tryBlendFuncSeparate(770, 771, 1, 0);
        float fadeAlpha = (com.adjustclient.core.gui.clickgui.ClickGuiScreen.INSTANCE != null
                && com.adjustclient.core.gui.clickgui.ClickGuiScreen.INSTANCE.fade != null
                && com.adjustclient.core.gui.clickgui.ClickGuiScreen.INSTANCE.fade.animation != null)
                ? com.adjustclient.core.gui.clickgui.ClickGuiScreen.INSTANCE.fade.animation.value
                : 1.0F;
        int alpha = (int) (100.0F * fadeAlpha);
        Minecraft.getMinecraft().getRenderItem().renderItemIntoGUI(this.stack, alpha, 0, x * 2.0F, y * 2.0F);
        GlStateManager.disableDepth();
        RenderHelper.disableStandardItemLighting();
        GL11.glScalef(2.0F, 2.0F, 2.0F);
    }
}
