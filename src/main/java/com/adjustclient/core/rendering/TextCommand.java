package com.adjustclient.core.rendering;

import com.adjustclient.core.gui.hud.layout.*;
import com.adjustclient.core.rendering.font.*;
import com.adjustclient.core.rendering.font.image.NvgDraw;
import org.lwjgl.opengl.GL11;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.ScaledResolution;

public final class TextCommand extends Point2f implements PositionedCommand, DrawCommand {
    public NvgFontRenderer font;
    public String text;
    public int color;
    public boolean shadow;
    public TextCommand(NvgFontRenderer font, String text, float x, float y, int color, boolean shadow) {
        this.font = font; this.text = text; this.x = x; this.y = y; this.color = color; this.shadow = shadow;
    }
    public void setPosition(Point2f p) { x = p.x; y = p.y; }
    public void draw(NvgRenderer renderer, ScaledResolution resolution) {
        if (font != null) {
            if (shadow) font.drawString(text, x, y, color);
            else font.drawStringNoShadow(text, x, y, color);
        } else {
            FontRenderer vanilla = Minecraft.getMinecraft().fontRendererObj;
            if (shadow) vanilla.drawStringWithShadow(text, x, y, color);
            else if (vanilla.forkField87) vanilla.drawString(text, x, y, color, false);
        }
    }
    public Rect2f getBounds() {
        Rect2f result = new Rect2f();
        result.x = x; result.y = y;
        FontRenderer vanilla = Minecraft.getMinecraft().fontRendererObj;
        result.width = font != null ? font.getStringWidth(text) : ((TextRenderer) vanilla).getStringWidth(text);
        result.height = font != null ? font.size : vanilla.FONT_HEIGHT;
        return result;
    }
}
