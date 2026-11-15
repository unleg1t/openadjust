package com.adjustclient.module.impl.render.hud.eventtimer;

import net.minecraft.item.ItemStack;
import com.adjustclient.render.font.Fonts;

import java.awt.Color;
import java.util.List;

public final class TimerEntry {

    public String name;
    public ItemStack icon;
    public String time;
    public String extra;

    /** Deferred item draws are queued as Runnables; see {@link #render}. */

    void drawIcon(int xOffset, float x, float y) {
        net.minecraft.client.renderer.GlStateManager.tryBlendFuncSeparate(770, 771, 1, 0);
        net.minecraft.client.renderer.GlStateManager.disableLighting();
        org.lwjgl.opengl.GL11.glPushMatrix();
        org.lwjgl.opengl.GL11.glRotatef(-30.0F, 0.0F, 1.0F, 0.0F);
        org.lwjgl.opengl.GL11.glRotatef(165.0F, 1.0F, 0.0F, 0.0F);
        net.minecraft.client.renderer.RenderHelper.enableStandardItemLighting();            // RenderHelper.b
        org.lwjgl.opengl.GL11.glPopMatrix();
        net.minecraft.client.Minecraft.getMinecraft().getRenderItem().renderItemIntoGUI(this.icon, xOffset + 10, false, x + 2.0F, y);
        net.minecraft.client.renderer.RenderHelper.disableStandardItemLighting();           // RenderHelper.a
        net.minecraft.client.renderer.GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
    }

    /**
     * Draws the row at (x, y): the icon (queued into {@code deferred}, or drawn at once when no queue is present), the name in
     * Color(233, 235, 234, alpha) at (x + 2 + 18, y + 3.5), the extra line in Color(170, 170, 170, alpha) at (x + 1 + .., y + 5) and the time in
     * Color(213, 215, 214, alpha) at (.., y + 10.5), where alpha = (int) (fade * 190).
     */
    void render(float x, float y, float fade, List<Runnable> deferred) {
        int alpha = (int) (fade * 190.0F);
        float textX = x + 2.0F;
        float textY = y + 3.5F;
        if (this.icon != null) {
            // queue the icon draw behind the text
            deferred.add(() -> this.drawIcon(alpha, x, y));
            textX += 18.0F;
        }
        float nameEndX = Fonts.TAHOMA_10.drawString(this.name, textX, textY, new Color(233, 235, 234, alpha).getRGB());
        if (this.extra != null) {
            Fonts.TAHOMA_8.drawString(this.extra, nameEndX + 1.0F, y + 5.0F, new Color(170, 170, 170, alpha).getRGB());
        }
        if (this.time != null) {
            Fonts.TAHOMA_10.drawString(this.time, textX, y + 10.5F, new Color(213, 215, 214, alpha).getRGB());
        }
    }
}
