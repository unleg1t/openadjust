package com.adjustclient.render;

import com.adjustclient.ui.hud.layout.Point2f;
import com.adjustclient.ui.hud.layout.Rect2f;
import com.adjustclient.render.font.NvgContext;
import com.adjustclient.render.font.image.NvgDraw;
import org.joml.Vector4f;
import net.minecraft.client.gui.ScaledResolution;

public final class GradientRectCommand extends Rect2f implements PositionedCommand, DrawCommand {
    public int startColor;
    public int endColor;
    public GradientRectCommand(float x, float y, float width, float height, int startColor, int endColor) {
        this.x = x; this.y = y; this.width = width; this.height = height;
        this.startColor = startColor;
        this.endColor = endColor;
    }
    public void setPosition(Point2f p) { x = p.x; y = p.y; }
    public void draw(NvgRenderer renderer, ScaledResolution resolution) {
        NvgDraw.fillGradientRect(startColor, endColor, x, y, x + width, y + height, x, y, x + width, y);
    }
    public Rect2f getBounds() { return this; }
}
