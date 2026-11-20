package com.adjustclient.modules.impl.render.motionblur;

import java.nio.ByteBuffer;
import java.nio.FloatBuffer;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.OpenGlHelper;
import net.minecraft.client.renderer.texture.TextureUtil;
import net.minecraft.client.shader.Framebuffer;
import net.minecraft.client.shader.Shader;
import net.minecraft.client.shader.ShaderGroup;
import net.minecraft.client.shader.ShaderManager;
import net.minecraft.entity.Entity;
import org.joml.Matrix4f;
import org.lwjgl.BufferUtils;
import org.lwjgl.opengl.GL11;

final class MonkeyBlurShader {
    private final FloatBuffer matrixBuffer = BufferUtils.createFloatBuffer(16);
    private final Matrix4f projection = new Matrix4f();
    private final Matrix4f modelView = new Matrix4f();
    private final Matrix4f previousProjection = new Matrix4f();
    private final Matrix4f previousModelView = new Matrix4f();
    private final Matrix4f projectionInverse = new Matrix4f();
    private final Matrix4f modelViewInverse = new Matrix4f();
    private final Matrix4f transpose = new Matrix4f();
    private ShaderGroup shaders;
    final DepthCapture depthCapture = new DepthCapture();
    private int depthTexture = -1;
    private double cameraX, cameraY, cameraZ;
    private double previousX, previousY, previousZ;
    private boolean initialized;
    boolean valid;

    private void copyDepth(Framebuffer source, int texture) {
        int active = GL11.glGetInteger(34016);
        GlStateManager.setActiveTexture(OpenGlHelper.defaultTexUnit);
        int binding = GL11.glGetInteger(32873);
        GlStateManager.bindTexture(texture);
        GL11.glCopyTexSubImage2D(3553, 0, 0, 0, 0, 0, source.framebufferWidth, source.framebufferHeight);
        GlStateManager.bindTexture(binding);
        GlStateManager.setActiveTexture(active);
    }
    void render(Framebuffer target, float strength) throws Exception {
        if (!valid || !target.useDepth) return;
        GlStateManager.setActiveTexture(OpenGlHelper.defaultTexUnit);
        ensureDepthTexture(target);
        depthCapture.prepare(target);
        if (shaders == null) {
            shaders = MotionBlurRenderer.loadShader(target, "monkey_blur");
            Shader first = shaders.listShaders.getFirst();
            first.addAuxFramebuffer("depthtex", depthTexture, target.framebufferWidth, target.framebufferHeight);
            first.addAuxFramebuffer("EntityMaskSampler", depthCapture.mask,
                    target.framebufferWidth, target.framebufferHeight);
        }
        target.bindFramebuffer(false);
        copyDepth(target, depthTexture);
        ShaderManager shader = shaders.listShaders.getFirst().getShaderManager();
        shader.getShaderUniformOrDefault("strength").set(strength);
        shader.getShaderUniformOrDefault("cameraPosition").set((float) cameraX, (float) cameraY, (float) cameraZ);
        shader.getShaderUniformOrDefault("previousCameraPosition").set((float) previousX, (float) previousY, (float) previousZ);
        setMatrix(shader, "projectionInverse", projectionInverse);
        setMatrix(shader, "modelViewInverse", modelViewInverse);
        setMatrix(shader, "previousProjection", previousProjection);
        setMatrix(shader, "previousModelView", previousModelView);
        MotionBlurRenderer.renderShader(shaders);
    }
    void updateMatrices(Entity camera, float partialTicks) {
        previousProjection.set(projection);
        previousModelView.set(modelView);
        previousX = cameraX;
        previousY = cameraY;
        previousZ = cameraZ;
        cameraX = camera.prevPosX + (camera.posX - camera.prevPosX) * partialTicks;
        cameraY = camera.prevPosY + (camera.posY - camera.prevPosY) * partialTicks;
        cameraZ = camera.prevPosZ + (camera.posZ - camera.prevPosZ) * partialTicks;
        matrixBuffer.clear();
        GL11.glGetFloatv(2983, matrixBuffer);
        projection.set(matrixBuffer);
        matrixBuffer.clear();
        GL11.glGetFloatv(2982, matrixBuffer);
        modelView.set(matrixBuffer);
        if (!initialized || Math.abs(cameraX - previousX) > 16.0
                || Math.abs(cameraY - previousY) > 16.0 || Math.abs(cameraZ - previousZ) > 16.0) {
            previousProjection.set(projection);
            previousModelView.set(modelView);
            previousX = cameraX;
            previousY = cameraY;
            previousZ = cameraZ;
        }
        projection.invert(projectionInverse);
        modelView.invert(modelViewInverse);
        initialized = true;
        valid = true;
    }
    private void setMatrix(ShaderManager shader, String name, Matrix4f matrix) {
        var uniform = shader.getShaderUniformOrDefault(name);
        matrix.transpose(transpose);
        uniform.set(transpose);
    }
    private void ensureDepthTexture(Framebuffer target) {
        int active = GL11.glGetInteger(34016);
        GlStateManager.setActiveTexture(OpenGlHelper.defaultTexUnit);
        int binding = GL11.glGetInteger(32873);
        if (depthTexture == -1) depthTexture = allocateDepthTexture(target);
        GlStateManager.bindTexture(binding);
        GlStateManager.setActiveTexture(active);
    }
    void delete() {
        if (shaders != null) shaders.deleteShaderGroup();
        shaders = null;
        if (depthTexture != -1) TextureUtil.deleteTexture(depthTexture);
        depthTexture = -1;
        depthCapture.delete();
        initialized = false;
        valid = false;
    }
    private int allocateDepthTexture(Framebuffer framebuffer) {
        int texture = TextureUtil.glGenTextures();
        GlStateManager.bindTexture(texture);
        GL11.glTexParameteri(3553, 10241, 9728);
        GL11.glTexParameteri(3553, 10240, 9728);
        GL11.glTexParameteri(3553, 10242, 33071);
        GL11.glTexParameteri(3553, 10243, 33071);
        GL11.glTexImage2D(3553, 0, 33190, framebuffer.framebufferWidth, framebuffer.framebufferHeight,
                0, 6402, 5126, (ByteBuffer) null);
        return texture;
    }
}
