package com.adjustclient.module.impl.render.motionblur;

import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.OpenGlHelper;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.WorldRenderer;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.client.shader.Framebuffer;
import org.lwjgl.opengl.GL11;

final class MoulberryBlur {
    private Framebuffer previous;
    private Framebuffer current;
    private boolean hasPrevious;

    void delete() {
        if (previous != null) previous.deleteFramebuffer();
        previous = null;
        if (current != null) current.deleteFramebuffer();
        current = null;
        hasPrevious = false;
    }
    void render(Framebuffer target, float strength) {
        GlStateManager.setActiveTexture(OpenGlHelper.defaultTexUnit);
        GL11.glMatrixMode(5889);
        GL11.glPushMatrix();
        GL11.glLoadIdentity();
        GL11.glOrtho(0, target.framebufferWidth, target.framebufferHeight, 0, 1000, 3000);
        GL11.glMatrixMode(5888);
        GL11.glPushMatrix();
        GL11.glLoadIdentity();
        GL11.glTranslatef(0, 0, -2000);
        try {
            if (previous == null) {
                previous = new Framebuffer(target.framebufferWidth, target.framebufferHeight, false);
                current = new Framebuffer(target.framebufferWidth, target.framebufferHeight, false);
            }
            GlStateManager.disableAlpha();
            GlStateManager.disableLighting();
            GlStateManager.disableDepth();
            GlStateManager.depthMask(false);
            GlStateManager.disableCull();
            GlStateManager.colorMask(true, true, true, true);
            current.framebufferClear();
            current.bindFramebuffer(true);
            GlStateManager.disableBlend();
            draw(target, 9728, 1);
            if (hasPrevious) {
                GlStateManager.enableBlend();
                GlStateManager.tryBlendFuncSeparate(770, 771, 1, 1);
                draw(previous, 9729, Math.min(strength * 0.1F + 0.1F, 0.89999998F));
            }
            target.bindFramebuffer(true);
            GlStateManager.disableBlend();
            draw(current, 9729, 1);
            Framebuffer swap = previous;
            previous = current;
            current = swap;
            hasPrevious = true;
        } finally {
            GL11.glMatrixMode(5888);
            GL11.glPopMatrix();
            GL11.glMatrixMode(5889);
            GL11.glPopMatrix();
            GL11.glMatrixMode(5888);
            GlStateManager.bindTexture(0);
            GlStateManager.color(1, 1, 1, 1);
            GlStateManager.disableBlend();
            GlStateManager.tryBlendFuncSeparate(770, 771, 1, 0);
            GlStateManager.enableDepth();
            GlStateManager.depthMask(true);
            GlStateManager.enableCull();
            target.bindFramebuffer(true);
        }
    }
    private void draw(Framebuffer source, int filter, float alpha) {
        GlStateManager.enableTexture2D();
        source.bindFramebufferTexture();
        GL11.glTexParameteri(3553, 10241, filter);
        GL11.glTexParameteri(3553, 10240, filter);
        GlStateManager.color(1, 1, 1, alpha);
        Tessellator tessellator = Tessellator.getInstance();
        WorldRenderer vertices = tessellator.getWorldRenderer();
        vertices.begin(7, DefaultVertexFormats.POSITION_TEX);
        vertices.pos(0, source.framebufferHeight, 0).tex(0, 0).endVertex();
        vertices.pos(source.framebufferWidth, source.framebufferHeight, 0).tex(1, 0).endVertex();
        vertices.pos(source.framebufferWidth, 0, 0).tex(1, 1).endVertex();
        vertices.pos(0, 0, 0).tex(0, 1).endVertex();
        tessellator.draw();
        GL11.glTexParameteri(3553, 10241, source.framebufferFilter);
        GL11.glTexParameteri(3553, 10240, source.framebufferFilter);
    }
}
