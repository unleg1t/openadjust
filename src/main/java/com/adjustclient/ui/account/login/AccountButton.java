package com.adjustclient.ui.account.login;

import java.awt.Color;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.texture.TextureManager;
import net.minecraft.util.ResourceLocation;
import org.lwjgl.opengl.GL11;

import com.adjustclient.render.RenderUtil;
import com.adjustclient.render.animation.Animation;
import com.adjustclient.render.animation.Easing;
import com.adjustclient.ui.account.AccountEntry;
import com.adjustclient.ui.account.HoverTooltip;

public abstract class AccountButton extends GuiButton {

    public HoverTooltip hoverTooltip;
    protected Animation hoverAnim;
    protected AccountEntry account;
    protected ResourceLocation icon;
    public float x;
    public float y;

    public AccountButton(AccountEntry account, ResourceLocation icon) {
        super(-1, 0, 0, 8, 8, null);
        this.account = account;
        this.icon = icon;
    }

    public String getTooltip() {
        return null;
    }

    protected void drawIcon(float px, float py) {
        Gui.drawTexturedModalRect(px, py, 0.0F, 0.0F);
    }

    public int getAlpha(int alpha, int mouseX, int mouseY) {
        if (this.hoverAnim == null) {
            this.hoverAnim = new Animation(Easing.EASE_OUT_CUBIC, 185L);
            this.hoverAnim.value = 1.0F;
        }
        if (!this.enabled) {
            this.hoverAnim.value = 1.0F;
            return Math.max(0, Math.min(255, alpha));
        }
        boolean hovered = (float) mouseX > this.x
                && (float) (this.width + this.x) > (float) mouseX
                && (float) mouseY > this.y
                && (float) (this.height + this.y) > (float) mouseY;
        float target = hovered ? 1.6F : 1.0F;
        float factor = this.hoverAnim.update(target);
        int v = (int) ((float) alpha * factor);
        if (v < 0) {
            return 0;
        }
        return Math.min(v, 255);
    }

    /**
     * Draws the button: positions it, computes the hover alpha, draws the rounded background and
     * the icon, then registers a hover tooltip. Mirrors GuiButton.drawButton(mc, mouseX, mouseY)
     * with float coordinates.
     */
    public void drawButton(int mouseX, int mouseY, int alpha, float px, float py) {
        this.x = px;
        this.y = py;
        int buttonAlpha = getAlpha(alpha, mouseX, mouseY);
        drawBackground(buttonAlpha, px, py, this.width, this.height);
        drawTexturedIcon(buttonAlpha);
        // hover tooltip: when hovered, create/update this button's tooltip renderer (delay 500 ms)
        String tooltip = getTooltip();
        int tooltipAlpha = Math.max(0, Math.min(255, alpha * 2));
        if (tooltip != null) {
            if (this.hoverTooltip == null && (float) mouseX > px && (float) (px + this.width) > (float) mouseX
                    && (float) mouseY > py && (float) (py + this.width) > (float) mouseY) {
                this.hoverTooltip = new HoverTooltip((float) this.width, (float) this.height);
            }
            if (this.hoverTooltip != null) {
                this.hoverTooltip.draw(tooltip, mouseX, mouseY, tooltipAlpha, px, py, 500);
            }
        }
    }

    /**
     * Binds the icon texture and draws the icon glyph with a grey-scale colour whose alpha is
     * {@code alpha * 1.3 / 255}, delegating the glyph itself to {@link #drawIcon}.
     */
    public void drawTexturedIcon(int alpha) {
        TextureManager tm = Minecraft.getMinecraft().getTextureManager();
        tm.bindTexture(this.icon);
        GlStateManager.enableBlend();
        float a = (float) alpha * 1.3F / 255.0F;
        GlStateManager.color(1.0F, 1.0F, 1.0F, a);
        drawIcon(this.x, this.y);
        GlStateManager.disableBlend();
        GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
    }

    /**
     * Draws the rounded button panel: a dark fill (50,50,50) with a lighter border (110,110,115),
     * using the client's render helper. Alpha scales the panel colour.
     */
    public static void drawBackground(int alpha, float left, float top, float width, float height) {
        float right = left + width;
        float bottom = top + height;
        GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
        Gui.drawRect(new java.awt.Color(50, 50, 50, alpha).getRGB(), left, top, right, bottom);
        new java.awt.Color(110, 110, 115, alpha); // retain the constructor's range validation
        RenderUtil.enableBlend();                                        // begin border pass
        GlStateManager.color(0.43137255F, 0.43137255F, 0.4509804F, (float) (alpha) / 255.0F);
        RenderUtil.drawRectOutline(left, top, right, bottom, 0.5F);
        GlStateManager.enableTexture2D();
        GlStateManager.disableBlend();
    }

    public boolean mousePressed(Minecraft mc, int mouseX, int mouseY) {
        boolean within = (float) (this.height + this.y) > (float) mouseY
                && (float) (this.width + this.x) > (float) mouseX
                && (float) mouseY >= this.y
                && (float) mouseX >= this.x;
        return this.enabled && this.visible && within;
    }
}
