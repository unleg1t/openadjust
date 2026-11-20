package com.adjustclient.core.rendering;

import com.adjustclient.modules.impl.render.hud.HUDModule;
import com.adjustclient.core.settings.ColorSetting;
import java.awt.Color;

public final class ColorUtil {
    public static Color themeColor() {
        int shift = ((Number) HUDModule.radiance.getValue()).intValue();
        Color color = HUDModule.colorSetting.getColor();
        if (shift < 2) return null;
        float[] hsl = toHsl(color);
        hsl[0] = (hsl[0] + shift) % 360.0F;
        return hslToColor(hsl, color.getAlpha());
    }

    public static Color secondary(Color color) {
        if (HUDModule.isSecondaryActive()) {
            Color secondary = HUDModule.secondary.getColor();
            return color.getAlpha() == 255 ? secondary : withAlpha(secondary, color.getAlpha());
        }
        return color;
    }

    public static int brighter(int argb, float factor) {
        int alpha = argb & 0xFF000000;
        int r = argb >> 16 & 255, g = argb >> 8 & 255, b = argb & 255;
        int floor = (int) (1.0F / (1.0F - factor));
        if (r == 0 && g == 0 && b == 0) return alpha | floor << 16 | floor << 8 | floor;
        if (r > 0 && r < floor) r = floor;
        if (g > 0 && g < floor) g = floor;
        if (b > 0 && b < floor) b = floor;
        return alpha | Math.min((int) (r / factor), 255) << 16
                | Math.min((int) (g / factor), 255) << 8 | Math.min((int) (b / factor), 255);
    }

    public static Color scale(Color color, float factor) {
        return new Color(Math.max((int) (color.getRed() * factor), 0),
                Math.max((int) (color.getGreen() * factor), 0),
                Math.max((int) (color.getBlue() * factor), 0), color.getAlpha());
    }

    public static Color hslToColor(float[] hsl, int alpha) {
        float h = hsl[0], s = hsl[1], l = hsl[2];
        float chroma = (1.0F - Math.abs(l * 2.0F - 1.0F)) * s;
        float x = chroma * (1.0F - Math.abs(h / 60.0F % 2.0F - 1.0F));
        float r = 0.0F, g = 0.0F, b = 0.0F;
        if (h >= 0.0F && h < 60.0F) { r = chroma; g = x; }
        else if (h >= 60.0F && h < 120.0F) { r = x; g = chroma; }
        else if (h >= 120.0F && h < 180.0F) { g = chroma; b = x; }
        else if (h >= 180.0F && h < 240.0F) { g = x; b = chroma; }
        else if (h >= 240.0F && h < 300.0F) { r = x; b = chroma; }
        else if (h >= 300.0F && h < 360.0F) { r = chroma; b = x; }
        float m = l - chroma / 2.0F;
        return new Color(Math.max(0, Math.min(255, (int) ((r + m) * 255.0F))),
                Math.max(0, Math.min(255, (int) ((g + m) * 255.0F))),
                Math.max(0, Math.min(255, (int) ((b + m) * 255.0F))), alpha);
    }

    public static Color rainbow(float offset, float saturation, float brightness) {
        return Color.getHSBColor(((float) (System.currentTimeMillis() % 5000L)
                + (5000.0F - offset * 30.0F)) / 5000.0F, saturation, brightness);
    }

    public static Color brighter(Color color, float factor) {
        int r = color.getRed(), g = color.getGreen(), b = color.getBlue();
        int floor = (int) (1.0F / (1.0F - factor));
        if (r == 0 && g == 0 && b == 0) return new Color(floor, floor, floor, color.getAlpha());
        if (r > 0 && r < floor) r = floor;
        if (g > 0 && g < floor) g = floor;
        if (b > 0 && b < floor) b = floor;
        return new Color(Math.max(0, Math.min(255, (int) (r / factor))),
                Math.max(0, Math.min(255, (int) (g / factor))),
                Math.max(0, Math.min(255, (int) (b / factor))), color.getAlpha());
    }

    public static int pulse() {
        long time = System.currentTimeMillis() % 1300L;
        if (time >= 650L) time = 1300L - time;
        return Math.max(0, (int) (float) ((Math.sin((double) time / 1300.0) * Math.PI + 1.0) * 40.0));
    }

    public static float[] toHsl(Color color) {
        float[] rgb = color.getRGBColorComponents(null);
        float r = rgb[0], g = rgb[1], b = rgb[2];
        float max = Math.max(r, Math.max(g, b)), min = Math.min(r, Math.min(g, b));
        float delta = max - min, hue = 0.0F;
        if (delta != 0.0F) {
            if (r == max) hue = ((g - b) / delta) % 6.0F;
            else if (g == max) hue = (b - r) / delta + 2.0F;
            else hue = (r - g) / delta + 4.0F;
            hue *= 60.0F;
            if (hue < 0.0F) hue += 360.0F;
        }
        float lightness = (max + min) / 2.0F;
        return new float[] {hue, delta == 0.0F ? 0.0F : delta / (1.0F - Math.abs(lightness * 2.0F - 1.0F)), lightness};
    }

    public static Color blend(Color first, Color second, double ratio) {
        ratio = Math.max(0.0, Math.min(1.0, ratio));
        double inverse = 1.0 - ratio;
        return new Color((int) (first.getRed() * ratio + second.getRed() * inverse),
                (int) (first.getGreen() * ratio + second.getGreen() * inverse),
                (int) (first.getBlue() * ratio + second.getBlue() * inverse),
                (int) (first.getAlpha() * ratio + second.getAlpha() * inverse));
    }

    public static Color withAlpha(Color color, int alpha) {
        alpha = Math.max(0, Math.min(255, alpha));
        return color.getAlpha() == alpha ? color : new Color(color.getRed(), color.getGreen(), color.getBlue(), alpha);
    }
}
