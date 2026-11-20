package com.adjustclient.core.rendering;

import com.adjustclient.core.gui.hud.layout.Point2f;
import com.adjustclient.core.gui.hud.layout.Rect2f;
import com.adjustclient.core.rendering.font.NvgContext;
import com.adjustclient.core.rendering.font.image.NvgDraw;
import org.joml.Vector4f;
import net.minecraft.client.gui.ScaledResolution;

public final class StrokeRectCommand extends Rect2f implements PositionedCommand, DrawCommand {
    public float lineWidth;
    public int color;
    public boolean outside;
    public StrokeRectCommand(float x, float y, float width, float height, float lineWidth, int color, boolean outside) {
        this.x = x; this.y = y; this.width = width; this.height = height;
        this.lineWidth = lineWidth;
        this.color = color;
        this.outside = outside;
    }
    public void setPosition(Point2f p) { x = p.x; y = p.y; }
    public void draw(NvgRenderer renderer, ScaledResolution resolution) {
        Vector4f p = NvgContext.toPixels(x, y, x + width, y + height);
        NvgDraw.strokeRect(outside, color, p.x, p.y, p.z, p.w, lineWidth);
    }
    public Rect2f getBounds() { return this; }
}
