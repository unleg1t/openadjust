package com.adjustclient.module.impl.render.hud.playerlist;

import java.awt.Color;
import net.minecraft.util.MathHelper;

public class StatValue<T extends Number> {

    public String text;
    public int color;
    public T value;

    public StatValue(T value, String text) {
        this.text = text;
        float ratio = (float) value.intValue() / 400.0F;
        float t = MathHelper.clamp_float(ratio, 0.0F, 1.0F);
        float u = 1.0F - t;
        int r = (int) (t * 255.0F + u * 150.0F);
        int g = (int) (t * 85.0F + u * 150.0F);
        int a = (int) (u * 240.0F + t * 240.0F);
        this.color = new Color(r, g, g, a).getRGB();
    }

    public StatValue(T value, String text, StatColorScale scale) {
        this.text = text;
        this.color = chatColor(scale.getColor(value.doubleValue()).toString().charAt(1));
    }

    /** Fixed colour. */
    public StatValue(String text, Color color) {
        this.text = text;
        this.color = color.getRGB();
    }

    /** Fixed ARGB colour. */
    public StatValue(String text, int color) {
        this.text = text;
        this.color = color;
    }

    public static int chatColor(char code) {
        return net.minecraft.client.Minecraft.getMinecraft().fontRendererObj.getColorCode(code) | 0xF0000000;
    }
}
