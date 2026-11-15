package com.adjustclient.render.font;

import com.adjustclient.module.impl.render.hud.HUDModule;
import com.adjustclient.user.ClientUser;
import java.util.concurrent.atomic.AtomicInteger;

public class TextComponent {
    public String text;
    public int color;
    public TextComponent(String text, int color) { this.text = text; this.color = color; }
    public static float getWidth(TextComponent[] components, NvgFontRenderer font) {
        float width = 0.0F;
        for (TextComponent component : components) width += font.getStringWidth(component.text);
        return width;
    }
    public static void draw(TextComponent[] components, NvgFontRenderer font, float x, float y) {
        for (TextComponent component : components) {
            font.drawString(component.text, x, y, component.getColor());
            x += font.getStringWidth(component.text);
        }
    }
    public int getColor() { return color; }
}
