package com.adjustclient.core.gui.dialog;

import com.adjustclient.core.rendering.animation.Animation;
import com.adjustclient.core.rendering.animation.Easing;
import com.adjustclient.core.rendering.font.Fonts;
import com.adjustclient.core.rendering.font.NvgContext;
import com.adjustclient.core.rendering.font.NvgFontRenderer;
import com.adjustclient.core.rendering.font.image.NvgDraw;
import java.awt.Color;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiButton;
import org.joml.Vector4f;

public final class AdjustButton extends GuiButton {
    public AlphaModifier alphaModifier;
    public Animation hoverAnimation;
    public NvgFontRenderer font;
    public float x;
    public float y;
    public float labelOffsetX;
    public float labelOffsetY;
    public int base = 26;
    public Object field68;

    public AdjustButton(int id, float x, float y, int width, int height, String label, AlphaModifier alphaModifier) {
        super(id, 0, 0, width, height, label);
        this.x = x;
        this.y = y;
        this.alphaModifier = alphaModifier;
        this.font = Fonts.TAHOMA_12;
        this.hoverAnimation = new Animation(Easing.LINEAR, 111L);
    }
    public AdjustButton(int id, String label, int width, int height, int base, AlphaModifier alphaModifier, NvgFontRenderer font) {
        super(id, 0, 0, width, height, label);
        this.base = base;
        this.alphaModifier = alphaModifier;
        this.font = font;
        this.hoverAnimation = new Animation(Easing.LINEAR, 111L);
    }
    public void draw(float mouseX, float mouseY) {
        if (!visible) return;
        hovered = mouseX >= x && mouseY >= y && x + width > mouseX && y + height > mouseY;
        hoverAnimation.update(enabled && hovered ? 1.0F : 0.0F);
        float hover = hoverAnimation.value;
        int textGrey = (int) (hover * 25.0F + 201.0F);
        if ("Quit Playing".equals(displayString)) textGrey -= 40;
        if (!enabled) textGrey -= 40;
        int opacity = (int) (hover * 30.0F);
        NvgContext.pushEvenScale();
        Vector4f rectangle = NvgContext.toPixels(x, y, x + width, y + height);
        NvgDraw.fillRect(new Color(base, base, base, alphaModifier.multiplyAlpha(opacity + 82)).getRGB(),
                rectangle.x, rectangle.y, rectangle.z, rectangle.w);
        float scale = NvgContext.scaleFactor == null ? 1.0F
                : NvgContext.roundScaledPixels ? Math.round(NvgContext.scaleFactor) : NvgContext.scaleFactor.intValue();
        NvgDraw.strokeRect(true, new Color(base, base, base, alphaModifier.multiplyAlpha(opacity + 122)).getRGB(),
                rectangle.x, rectangle.y, rectangle.z, rectangle.w, scale * 0.5F);
        NvgContext.popEvenScale();
        float center = width / 2.0F + x;
        if (displayString.length() == 1 && !font.symbolFont) center += 0.25F;
        font.drawCenteredString(displayString, center + labelOffsetX, height / 2.0F + y - 1.5F + labelOffsetY,
                new Color(textGrey, textGrey, textGrey, alphaModifier.multiplyAlpha(2 * opacity + 190)).getRGB());
    }
    @Override
    public boolean mousePressed(Minecraft minecraft, int mouseX, int mouseY) {
        return enabled && visible && mouseX >= x && mouseY >= y && x + width > mouseX && y + height > mouseY;
    }
    public boolean isHovered(float mouseX, float mouseY) {
        return enabled && visible && mouseX >= x && mouseY >= y && x + width > mouseX && y + height > mouseY;
    }
    @Override
    public void drawButton(Minecraft minecraft, int mouseX, int mouseY) {
        draw(mouseX, mouseY);
    }
}
