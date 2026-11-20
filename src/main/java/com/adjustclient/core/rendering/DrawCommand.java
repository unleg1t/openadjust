package com.adjustclient.core.rendering;

import com.adjustclient.core.gui.hud.layout.Point2f;
import com.adjustclient.core.gui.hud.layout.Rect2f;
import com.adjustclient.core.rendering.font.NvgContext;
import com.adjustclient.core.rendering.font.image.NvgDraw;
import org.joml.Vector4f;
import net.minecraft.client.gui.ScaledResolution;

public interface DrawCommand {
    void setPosition(Point2f position);
    void draw(NvgRenderer renderer, ScaledResolution resolution);
    Rect2f getBounds();
}
