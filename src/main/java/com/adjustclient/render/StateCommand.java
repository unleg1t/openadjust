package com.adjustclient.render;

import com.adjustclient.ui.hud.layout.Point2f;
import com.adjustclient.ui.hud.layout.Rect2f;
import com.adjustclient.render.font.NvgContext;
import com.adjustclient.render.font.image.NvgDraw;
import org.joml.Vector4f;
import net.minecraft.client.gui.ScaledResolution;

public interface StateCommand extends DrawCommand {
    default void setPosition(Point2f position) {}
    default Rect2f getBounds() { return NvgRenderer.EMPTY_BOUNDS; }
}
