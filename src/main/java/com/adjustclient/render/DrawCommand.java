package com.adjustclient.render;

import com.adjustclient.ui.hud.layout.Point2f;
import com.adjustclient.ui.hud.layout.Rect2f;
import com.adjustclient.render.font.NvgContext;
import com.adjustclient.render.font.image.NvgDraw;
import org.joml.Vector4f;
import net.minecraft.client.gui.ScaledResolution;

public interface DrawCommand {
    void setPosition(Point2f position);
    void draw(NvgRenderer renderer, ScaledResolution resolution);
    Rect2f getBounds();
}
