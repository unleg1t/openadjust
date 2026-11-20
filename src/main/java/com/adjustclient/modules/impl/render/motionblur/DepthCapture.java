package com.adjustclient.modules.impl.render.motionblur;

import java.nio.ByteBuffer;
import java.nio.FloatBuffer;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.OpenGlHelper;
import net.minecraft.client.shader.Framebuffer;
import org.lwjgl.BufferUtils;
import org.lwjgl.opengl.ARBDrawBuffers;
import org.lwjgl.opengl.EXTDrawBuffers2;
import org.lwjgl.opengl.GL;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL20;
import org.lwjgl.opengl.GL30;

public final class DepthCapture {
    public Framebuffer mask;
    private Framebuffer target;
    private final ByteBuffer savedMask = BufferUtils.createByteBuffer(4);
    public boolean prepared;
    private boolean capturing;

    public void prepare(Framebuffer framebuffer) {
        if (prepared) return;
        boolean depth = GL11.glIsEnabled(2929);
        boolean depthWrite = GL11.glGetBoolean(2930);
        int activeTexture = GL11.glGetInteger(34016);
        GlStateManager.setActiveTexture(OpenGlHelper.defaultTexUnit);
        int texture = GL11.glGetInteger(32873);
        FloatBuffer clearColor = BufferUtils.createFloatBuffer(4);
        ByteBuffer colorMask = ByteBuffer.allocateDirect(4);
        GL11.glGetFloatv(3106, clearColor);
        GL11.glGetBooleanv(3107, colorMask);
        try {
            if (mask == null) {
                mask = new Framebuffer(framebuffer.framebufferWidth, framebuffer.framebufferHeight, false);
                mask.setFramebufferColor(0, 0, 0, 0);
                target = framebuffer;
                framebuffer.bindFramebuffer(false);
                OpenGlHelper.glFramebufferTexture2D(OpenGlHelper.GL_FRAMEBUFFER,
                        OpenGlHelper.GL_COLOR_ATTACHMENT0 + 1, 3553, mask.framebufferTexture, 0);
                mask.checkFramebufferComplete();
            }
            GlStateManager.colorMask(true, true, true, true);
            mask.framebufferClear();
            prepared = true;
        } finally {
            GlStateManager.bindTexture(texture);
            GlStateManager.setActiveTexture(activeTexture);
            GlStateManager.clearColor(clearColor.get(0), clearColor.get(1), clearColor.get(2), clearColor.get(3));
            GlStateManager.colorMask(colorMask.get(0) != 0, colorMask.get(1) != 0,
                    colorMask.get(2) != 0, colorMask.get(3) != 0);
            GlStateManager.depthMask(depthWrite);
            if (depth) GlStateManager.enableDepth(); else GlStateManager.disableDepth();
            framebuffer.bindFramebuffer(false);
        }
    }
    private void colorMask(boolean red, boolean green, boolean blue, boolean alpha) {
        // No GLCapabilities instance set for the current thread. Possible solutions:\n\ta) Call GL.createCapabilities() after making a context current in the current thread.\n\tb) Call GL.setCapabilities() if a GLCapabilities instance already exists for the current context.
        final String capabilitiesError = "No GLCapabilities instance set for the current thread. Possible solutions:\n\ta) Call GL.createCapabilities() after making a context current in the current thread.\n\tb) Call GL.setCapabilities() if a GLCapabilities instance already exists for the current context.";
        if (GL.getCapabilities().OpenGL30) GL30.glColorMaski(1, red, green, blue, alpha);
        else if (GL.getCapabilities().GL_EXT_draw_buffers2)
            EXTDrawBuffers2.glColorMaskIndexedEXT(1, red, green, blue, alpha);
        else GlStateManager.colorMask(red, green, blue, alpha);
    }
    public void end() {
        if (!capturing) return;
        target.bindFramebuffer(false);
        int[] attachments = {OpenGlHelper.GL_COLOR_ATTACHMENT0};
        if (GL.getCapabilities().OpenGL20) GL20.glDrawBuffers(attachments);
        else ARBDrawBuffers.glDrawBuffersARB(attachments);
        colorMask(savedMask.get(0) != 0, savedMask.get(1) != 0,
                savedMask.get(2) != 0, savedMask.get(3) != 0);
        capturing = false;
    }
    public void begin(Framebuffer framebuffer) {
        prepare(framebuffer);
        savedMask.clear();
        GL11.glGetBooleanv(3107, savedMask);
        int[] attachments = {OpenGlHelper.GL_COLOR_ATTACHMENT0, OpenGlHelper.GL_COLOR_ATTACHMENT0 + 1};
        if (GL.getCapabilities().OpenGL20) GL20.glDrawBuffers(attachments);
        else ARBDrawBuffers.glDrawBuffersARB(attachments);
        colorMask(true, true, true, true);
        capturing = true;
    }
    public void delete() {
        end();
        if (target != null && target.framebufferObject >= 0) {
            target.bindFramebuffer(false);
            OpenGlHelper.glFramebufferTexture2D(OpenGlHelper.GL_FRAMEBUFFER,
                    OpenGlHelper.GL_COLOR_ATTACHMENT0 + 1, 3553, 0, 0);
        }
        if (mask != null) mask.deleteFramebuffer();
        mask = null;
        target = null;
        prepared = false;
    }
}
