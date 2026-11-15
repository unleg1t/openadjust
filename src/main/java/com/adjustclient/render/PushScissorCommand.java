package com.adjustclient.render;

import com.adjustclient.ui.hud.layout.*;
import com.adjustclient.render.font.*;
import com.adjustclient.render.font.image.NvgDraw;
import org.lwjgl.opengl.GL11;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.ScaledResolution;

public final class PushScissorCommand extends Rect2f implements StateCommand {
    public PushScissorCommand(float x, float y, float width, float height) {
        this.x = x; this.y = y; this.width = width; this.height = height;
    }
    public void draw(NvgRenderer renderer, ScaledResolution resolution) {
        GL11.glPushMatrix();
        GL11.glEnable(GL11.GL_SCISSOR_TEST);
        double scale = resolution.getScaleFactor_double();
        GL11.glScissor((int) (x * scale), (int) ((resolution.getScaledHeight() - (y + height)) * scale),
                (int) (width * scale), (int) (height * scale));
        NvgDraw.pushScissor(x, y, width, height);
    }
    public Rect2f getBounds() { return this; }
}
