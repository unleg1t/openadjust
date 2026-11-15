package com.adjustclient.render;

import com.adjustclient.render.shader.ShaderSource;
import java.awt.Color;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.OpenGlHelper;
import net.minecraft.util.ResourceLocation;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL20;

public final class LogoRenderer {
    public static final ResourceLocation VERTEX;
    public static final ResourceLocation TEXTURE;
    public static ShaderSource shader;
    public static final ResourceLocation FRAGMENT;
    public static int color1Uniform;
    public static int color2Uniform;

    static {
        TEXTURE = new ResourceLocation("adjust", "textures/other/adjust_colorable.png");
        VERTEX = new ResourceLocation("adjust", "shaders/logo.vert");
        FRAGMENT = new ResourceLocation("adjust", "shaders/logo.frag");
    }

    public static void draw(Color first, Color second, float x, float y, float width, float height) {
        if (shader == null) {
            shader = ShaderSource.loadFromResources(VERTEX, FRAGMENT);
            color1Uniform = OpenGlHelper.glGetUniformLocation(shader.programId, "u_Color1");
            color2Uniform = OpenGlHelper.glGetUniformLocation(shader.programId, "u_Color2");
        }
        GL11.glPushMatrix();
        GL11.glTranslatef(x, y, 0.0F);
        Minecraft.getMinecraft().getTextureManager().bindTexture(TEXTURE);
        if (shader.programId != 0) OpenGlHelper.glUseProgram(shader.programId);
        GL20.glUniform4f(color1Uniform, first.getRed() / 255.0F, first.getGreen() / 255.0F,
                first.getBlue() / 255.0F, first.getAlpha() / 255.0F);
        GL20.glUniform4f(color2Uniform, second.getRed() / 255.0F, second.getGreen() / 255.0F,
                second.getBlue() / 255.0F, second.getAlpha() / 255.0F);
        GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
        Gui.drawModalRectWithCustomSizedTexture(0, 0, 0.0F, 0.0F, (int) width, (int) height, width, height);
        OpenGlHelper.glUseProgram(0);
        GL11.glPopMatrix();
    }
}
