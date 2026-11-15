package com.adjustclient.ui.clickgui.setting.color;

import com.adjustclient.render.font.image.NvgDraw;
import com.adjustclient.setting.ColorSetting;
import com.adjustclient.util.Vec2f;
import java.awt.Color;
import net.minecraft.util.MathHelper;

public final class AlphaSlider extends ColorPickerSlider {

    @Override
    public void render() {
        ColorSetting setting = this.picker.getColorSetting();
        Color color = setting.getColor();
        this.drawCheckerboard();
        float x1 = this.x;
        float x2 = x1 + this.width;
        float y1 = this.y;
        float y2 = y1 + this.height;
        int opaque = color.getRGB() & 0xFFFFFF | 0xFF000000;
        // gradient direction: horizontal, transparent on the left, the opaque colour on the right
        NvgDraw.fillGradientRect(0, opaque, x1, y1, x2, y2, x1, y1, x2, y1);
        this.drawKnob((float) color.getAlpha() / 255.0F);     // HIBYTE(value) / 255.0
    }

    @Override
    public void updateFromColor() {
    }

    @Override
    public void setFromMouse(Vec2f mouse) {
        float offset = mouse.x - this.x;
        ColorSetting setting = this.picker.getColorSetting();
        Color old = setting.getColor();
        int alpha = (int) ((offset / this.width) * 255.0F);
        alpha = MathHelper.clamp_int(alpha, 0, 255);
        Color color = alpha == old.getAlpha() ? old : new Color(old.getRed(), old.getGreen(), old.getBlue(), alpha);
        this.picker.pendingColor = color;
        Color previous = setting.getColor();
        if (previous != color) {
            setting.previousValue = previous;
            setting.value = color;
            setting.notifyListeners(previous, color);
        }
    }

    private void drawCheckerboard() {
        float x = this.x;
        float y = this.y;
        float width = this.width;
        float height = this.height + 0.5F;
        NvgDraw.pushScissor(x, y, width, height);
        int dark = new Color(130, 130, 130, 255).getRGB(); // -8224126
        int light = new Color(170, 170, 170, 255).getRGB(); // -5592406
        float square = 2.5F;
        int columns = (int) (this.width / square) + 1;
        for (int i = 0; i < columns; i++) {
            float left = (float) i * square + this.x;
            float top = this.y + 0.0F;
            NvgDraw.fillRect((i & 1) == 0 ? dark : light, left, top, left + square, top + square);
            float top2 = square + this.y;
            NvgDraw.fillRect(((i + 1) & 1) == 0 ? dark : light, left, top2, left + square, top2 + square);
        }
        NvgDraw.popScissor();
    }
}
