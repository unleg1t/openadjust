package com.adjustclient.core.rendering.shader;

import java.util.HashMap;
import java.util.Map;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.OpenGlHelper;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.client.shader.Framebuffer;
import net.minecraft.util.ResourceLocation;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL20;

public final class OutlineShader {
    public static final OutlineShader INSTANCE;
    private Map<String, Integer> uniforms;
    private Framebuffer framebuffer;
    private int program;
    private boolean entityShadows;
    private Object field20;

    static { INSTANCE = new OutlineShader(); }

    public OutlineShader() {
        ShaderSource source = ShaderSource.loadFromResources(new ResourceLocation("adjust", "shaders/vertex.vert"),
                new ResourceLocation("adjust", "shaders/outline.frag"));
        program = source.programId;
    }

    public void setupUniforms() {
        registerUniform("texture");
        registerUniform("texelSize");
        registerUniform("color");
        registerUniform("radius");
    }

    public void beginCapture(float partialTicks) {
        Minecraft mc = Minecraft.getMinecraft();
        GlStateManager.enableAlpha();
        GL11.glPushMatrix();
        GlStateManager.pushAttrib();
        if (framebuffer == null) {
            framebuffer = new Framebuffer(mc.displayWidth, mc.displayHeight, true);
        } else if (mc.displayWidth != framebuffer.framebufferWidth || mc.displayHeight != framebuffer.framebufferHeight) {
            framebuffer.deleteFramebuffer();
            framebuffer = new Framebuffer(mc.displayWidth, mc.displayHeight, true);
        } else {
            framebuffer.framebufferClear();
        }
        framebuffer.bindFramebuffer(true);
        entityShadows = mc.gameSettings.entityShadows;
        mc.gameSettings.entityShadows = false;
        mc.entityRenderer.setupCameraTransform(partialTicks, 0);
    }

    public void registerUniform(String name) {
        uniforms.put(name, GL20.glGetUniformLocation(program, name));
    }

    public void drawFramebuffer(Framebuffer target) {
        drawFramebuffer(target, new ScaledResolution(Minecraft.getMinecraft()));
    }

    public void drawFramebuffer(Framebuffer target, ScaledResolution resolution) {
        double width = resolution.getScaledWidth_double();
        double height = resolution.getScaledHeight_double();
        GlStateManager.bindTexture(target.framebufferTexture);
        GL11.glBegin(GL11.GL_QUADS);
        GL11.glTexCoord2d(0, 1);
        GL11.glVertex2d(0, 0);
        GL11.glTexCoord2d(0, 0);
        GL11.glVertex2d(0, height);
        GL11.glTexCoord2d(1, 0);
        GL11.glVertex2d(width, height);
        GL11.glTexCoord2d(1, 1);
        GL11.glVertex2d(width, 0);
        GL11.glEnd();
        GL20.glUseProgram(0);
    }

    public void startShader(int color, float radius) {
        GL11.glPushMatrix();
        GL20.glUseProgram(program);
        if (uniforms == null) {
            uniforms = new HashMap<>();
            setupUniforms();
        }
        updateUniforms(color, radius);
    }

    public void endCapture(int color, ScaledResolution resolution, float radius) {
        Minecraft mc = Minecraft.getMinecraft();
        mc.gameSettings.entityShadows = entityShadows;
        GlStateManager.enableBlend();
        GlStateManager.tryBlendFuncSeparate(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA, GL11.GL_ONE, GL11.GL_ZERO);
        mc.getFramebuffer().bindFramebuffer(true);
        mc.entityRenderer.enableLightmap();
        RenderHelper.disableStandardItemLighting();
        startShader(color, radius);
        mc.entityRenderer.setupOverlayRendering(resolution);
        drawFramebuffer(framebuffer, resolution);
        GL20.glUseProgram(0);
        GL11.glPopMatrix();
        GlStateManager.setActiveTexture(OpenGlHelper.lightmapTexUnit);
        GlStateManager.disableTexture2D();
        GlStateManager.setActiveTexture(OpenGlHelper.defaultTexUnit);
        GL11.glPopMatrix();
        GlStateManager.popAttrib();
    }

    public int getUniform(String name) { return uniforms.get(name); }

    public void updateUniforms(int color, float radius) {
        Minecraft mc = Minecraft.getMinecraft();
        GL20.glUniform1i(getUniform("texture"), 0);
        GL20.glUniform2f(getUniform("texelSize"), radius * (1.0f / mc.displayWidth), (1.0f / mc.displayHeight) * radius);
        GL20.glUniform4f(getUniform("color"), (color >> 16 & 255) / 255.0f,
                (color >> 8 & 255) / 255.0f, (color & 255) / 255.0f, (color >>> 24) / 255.0f);
        GL20.glUniform1f(getUniform("radius"), radius);
    }
}
