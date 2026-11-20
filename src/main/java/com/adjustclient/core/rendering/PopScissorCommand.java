package com.adjustclient.core.rendering;

import com.adjustclient.core.gui.hud.layout.*;
import com.adjustclient.core.rendering.font.*;
import com.adjustclient.core.rendering.font.image.NvgDraw;
import org.lwjgl.opengl.GL11;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.ScaledResolution;

public final class PopScissorCommand implements StateCommand {
    public void draw(NvgRenderer renderer, ScaledResolution resolution) {
        GL11.glDisable(GL11.GL_SCISSOR_TEST);
        GL11.glPopMatrix();
        NvgDraw.popScissor();
    }
}
