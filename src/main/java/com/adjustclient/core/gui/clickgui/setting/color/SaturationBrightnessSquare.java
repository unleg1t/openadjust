package com.adjustclient.core.gui.clickgui.setting.color;

import com.adjustclient.core.rendering.font.NvgContext;
import com.adjustclient.core.rendering.font.image.NvgDraw;
import com.adjustclient.core.settings.ColorSetting;
import com.adjustclient.utilities.Vec2f;
import java.awt.Color;
import net.minecraft.util.MathHelper;
import org.joml.Vector4f;

public final class SaturationBrightnessSquare extends ColorPickerElement {

    private float saturation = 0.0F;
    private float brightness = 0.0F;

    float getSaturation() {
        return this.saturation;
    }

    float getBrightness() {
        return this.brightness;
    }

    @Override
    public void render() {
        HueSlider hueSlider = this.picker.getHueSlider();
        int pure = Color.HSBtoRGB(hueSlider.getHue(), 1.0F, 1.0F);
        float x1 = this.x;
        float x2 = x1 + this.width;
        float y1 = this.y;
        float y2 = y1 + this.height;
        NvgDraw.fillGradientRect(0xFFFFFFFF, pure, x1, y1, x2, y2, x1, y1, x2, y1);
        NvgDraw.fillGradientRect(0x19000000, Color.BLACK.getRGB(), x1, y1, x2, y2, x1, y1, x1, y2);
        // knob: 3 x 3 box centred on (saturation, 1 - brightness)
        float knobX = MathHelper.clamp_float(this.width * this.saturation + this.x - 1.5F, this.x, this.x + this.width - 3.0F);
        float knobY = MathHelper.clamp_float((1.0F - this.brightness) * this.height + this.y - 1.5F, this.y, this.y + this.height - 3.0F);
        NvgContext.pushEvenScale();
        Vector4f px = NvgContext.toPixels(knobX, knobY, knobX + 3.0F, knobY + 3.0F);
        float kx1 = px.x + 0.5F;
        float ky1 = px.y + 0.5F;
        float kx2 = px.z - 0.5F;
        float ky2 = px.w - 0.5F;
        NvgDraw.strokeRect(true, new Color(9, 9, 9, 200).getRGB(), kx1, ky1, kx2, ky2, 0.5F);
        NvgDraw.strokeRect(false, new Color(228, 228, 228, 200).getRGB(), kx1, ky1, kx2, ky2, 0.5F);
        NvgContext.popEvenScale();
    }

    @Override
    public void updateFromColor() {
        Color color = this.picker.getColorSetting().getColor();
        float[] hsb = Color.RGBtoHSB(color.getRed(), color.getGreen(), color.getBlue(), null);
        this.saturation = hsb[1];
        this.brightness = hsb[2];
    }

    @Override
    public void setFromMouse(Vec2f mouse) {
        float brightness = 1.0F - (mouse.y - this.y) / this.height;
        this.brightness = brightness;
        float saturation = (mouse.x - this.x) / this.width;
        this.saturation = saturation;
        ColorSetting setting = this.picker.getColorSetting();
        Color old = setting.getColor();
        HueSlider hueSlider = this.picker.getHueSlider();
        int rgb = Color.HSBtoRGB(hueSlider.getHue(), saturation, brightness);
        int alpha = old.getAlpha();
        int argb = alpha == 255 ? rgb : (alpha << 24 | (rgb >> 16 & 0xFF) << 16 | (rgb >> 8 & 0xFF) << 8 | rgb & 0xFF);
        Color color = new Color(argb, true);
        this.picker.pendingColor = color;
        setting.setValue(color);
    }
}
