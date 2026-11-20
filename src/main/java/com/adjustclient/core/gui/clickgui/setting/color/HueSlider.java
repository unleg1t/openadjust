package com.adjustclient.core.gui.clickgui.setting.color;

import com.adjustclient.core.rendering.font.image.NvgDraw;
import com.adjustclient.core.settings.ColorSetting;
import com.adjustclient.utilities.Vec2f;
import java.awt.Color;

public final class HueSlider extends ColorPickerSlider {

    private static int[] RAINBOW;

    private float hue;

    float getHue() {
        return this.hue;
    }

    static {
        RAINBOW = new int[] {
                Color.RED.getRGB(), Color.YELLOW.getRGB(), Color.GREEN.getRGB(), Color.CYAN.getRGB(),
                Color.BLUE.getRGB(), Color.MAGENTA.getRGB(), Color.RED.getRGB()
        };
    }

    @Override
    public void render() {
        float y = this.y;
        int[] colors = RAINBOW;
        float width = this.width;
        if (colors.length >= 2) {
            float segment = width / (float) (colors.length - 1);
            for (int i = 0; i < colors.length - 1; i++) {
                float x1 = (float) i * segment + this.x;
                float x2 = x1 + segment;
                // gradient from stop i (left) to stop i + 1 (right): horizontal, along the top edge of the bar
                NvgDraw.fillGradientRect(colors[i], colors[i + 1], x1, y, x2, y + this.height, x1, y, x2, y);
            }
        }
        this.drawKnob(this.hue);
    }

    @Override
    public void updateFromColor() {
        Color color = this.picker.getColorSetting().getColor();
        this.hue = Color.RGBtoHSB(color.getRed(), color.getGreen(), color.getBlue(), null)[0];
    }

    @Override
    public void setFromMouse(Vec2f mouse) {
        float hue = (mouse.x - this.x) / this.width;
        ColorSetting setting = this.picker.getColorSetting();
        Color old = setting.getColor();
        SaturationBrightnessSquare square = this.picker.getSquare();
        float saturation = square.getSaturation();
        float brightness = square.getBrightness();
        int rgb = Color.HSBtoRGB(hue, saturation, brightness);
        int alpha = old.getAlpha();
        int argb = alpha == 255 ? rgb : (alpha << 24 | (rgb >> 16 & 0xFF) << 16 | (rgb >> 8 & 0xFF) << 8 | rgb & 0xFF);
        Color color = new Color(argb, true);
        this.picker.pendingColor = color;
        setting.setValue(color);
        this.hue = hue;
    }
}
