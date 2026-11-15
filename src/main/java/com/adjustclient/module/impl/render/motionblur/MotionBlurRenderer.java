package com.adjustclient.module.impl.render.motionblur;

import com.adjustclient.logging.LogLevel;
import com.adjustclient.logging.Logger;
import com.adjustclient.util.MinecraftInstance;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.OpenGlHelper;
import net.minecraft.client.shader.Framebuffer;
import net.minecraft.client.shader.Shader;
import net.minecraft.client.shader.ShaderGroup;
import net.minecraft.entity.Entity;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.World;
import org.lwjgl.opengl.GL11;
import shadersmod.client.Shaders;

public final class MotionBlurRenderer implements MinecraftInstance {
    private final MotionBlurModule module;
    private final MonkeyBlurShader monkey = new MonkeyBlurShader();
    private final MoulberryBlur moulberry = new MoulberryBlur();
    private ShaderGroup phosphor;
    Framebuffer lastFramebuffer;
    World lastWorld;
    Entity lastCamera;
    private MotionBlurMode lastMode;
    private float lastStrength;
    long lastTime;
    private int lastWidth;
    private int lastHeight;
    private int lastPerspective;
    boolean active;
    private boolean phosphorHistory;
    boolean failed;
    private boolean matrixCaptured;

    MotionBlurRenderer(MotionBlurModule module) { this.module = module; }
    public void beginFrame() {
        active = false;
        matrixCaptured = false;
        Entity camera = mc.getRenderViewEntity();
        try {
            var renderingFrame = mc.getClass().getDeclaredField("renderingFrame");
            var cameraOverride = mc.getClass().getDeclaredField("cameraOverride");
            renderingFrame.setAccessible(true);
            cameraOverride.setAccessible(true);
            Entity override = (Entity) cameraOverride.get(mc);
            if (renderingFrame.getBoolean(mc) && override != null) camera = override;
        } catch (NoSuchFieldException absentInVanilla) {
        } catch (IllegalAccessException inaccessibleForkField) {
            throw new IllegalStateException(inaccessibleForkField);
        }
        if (!module.isEnabled() || mc.theWorld == null || camera == null || mc.skipRenderWorld
                || !OpenGlHelper.isFramebufferEnabled() || Shaders.shaderPackLoaded || mc.gameSettings.anaglyph) {
            destroy();
            lastFramebuffer = null;
            lastWorld = null;
            lastCamera = null;
            lastTime = 0;
            active = false;
            failed = false;
            return;
        }
        Framebuffer target = mc.getFramebuffer();
        if (target.framebufferWidth < 1 || target.framebufferHeight < 1) {
            destroy();
            lastFramebuffer = null;
            lastWorld = null;
            lastCamera = null;
            lastTime = 0;
            active = false;
            failed = false;
            return;
        }
        long now = System.nanoTime();
        if (target != lastFramebuffer || target.framebufferWidth != lastWidth || target.framebufferHeight != lastHeight
                || mc.theWorld != lastWorld || camera != lastCamera || mc.gameSettings.thirdPersonView != lastPerspective
                || module.mode.getValue() != lastMode || module.getStrength() != lastStrength
                || lastTime != 0 && now - lastTime >= 100000001L) {
            destroy();
            lastFramebuffer = null;
            lastWorld = null;
            lastCamera = null;
            lastTime = 0;
            active = false;
            failed = false;
        }
        lastFramebuffer = target;
        lastWidth = target.framebufferWidth;
        lastHeight = target.framebufferHeight;
        lastWorld = mc.theWorld;
        lastCamera = camera;
        lastPerspective = mc.gameSettings.thirdPersonView;
        lastMode = module.mode.getValue();
        lastStrength = module.getStrength();
        lastTime = now;
        active = !failed && (lastMode == MotionBlurMode.MOULBERRY || OpenGlHelper.shadersSupported);
        monkey.valid = false;
        monkey.depthCapture.prepared = false;
    }
    public void captureMatrices(float partialTicks) {
        if (active && lastMode == MotionBlurMode.MONKEY) {
            monkey.updateMatrices(lastCamera, partialTicks);
            matrixCaptured = true;
        }
    }
    private void fail(Throwable error) {
        destroy();
        active = false;
        matrixCaptured = false;
        failed = true;
        mc.getFramebuffer().bindFramebuffer(true);
        Logger.log(LogLevel.ERROR, true, "Could not load or render motion blur: {}", error);
    }
    static void renderShader(ShaderGroup group) {
        GlStateManager.setActiveTexture(OpenGlHelper.defaultTexUnit);
        GL11.glMatrixMode(5890);
        GL11.glPushMatrix();
        GL11.glLoadIdentity();
        try {
            group.loadShaderGroup(0.0F);
        } finally {
            GlStateManager.setActiveTexture(OpenGlHelper.defaultTexUnit);
            GL11.glMatrixMode(5890);
            GL11.glPopMatrix();
            GL11.glMatrixMode(5888);
            for (int unit = 3; unit >= 1; unit--) {
                GlStateManager.setActiveTexture(OpenGlHelper.defaultTexUnit + unit);
                GlStateManager.bindTexture(0);
                GlStateManager.disableTexture2D();
            }
            GlStateManager.setActiveTexture(OpenGlHelper.defaultTexUnit);
            GlStateManager.enableTexture2D();
            GlStateManager.color(1, 1, 1, 1);
            GlStateManager.disableBlend();
            GlStateManager.enableDepth();
            GlStateManager.depthMask(true);
            GlStateManager.enableAlpha();
            group.mainFramebuffer.bindFramebuffer(true);
        }
    }
    public boolean beginEntityMask(Entity entity) {
        if (!active || !matrixCaptured || lastMode != MotionBlurMode.MONKEY) return false;
        if (GL11.glGetInteger(36006) != lastFramebuffer.framebufferObject) return false;
        com.adjustclient.module.impl.render.chams.ChamsTargetSettings settings =
                com.adjustclient.game.render.FrozenPlayerRenderer.ENTITY_SETTINGS.get(entity);
        if (entity != mc.thePlayer && (settings == null || !settings.enabled.isEnabled())) return false;
        try {
            monkey.depthCapture.begin(lastFramebuffer);
            return true;
        } catch (RuntimeException error) {
            fail(error);
            return false;
        }
    }
    static ShaderGroup loadShader(Framebuffer target, String name) throws Exception {
        ShaderGroup group = new ShaderGroup(mc.getTextureManager(), mc.getResourceManager(), target,
                new ResourceLocation("shaders/post/adjust_" + name + ".json"));
        try {
            group.createBindFramebuffers(target.framebufferWidth, target.framebufferHeight);
            return group;
        } catch (RuntimeException error) {
            group.deleteShaderGroup();
            throw error;
        }
    }
    public void renderMonkey() {
        matrixCaptured = false;
        if (active && lastMode == MotionBlurMode.MONKEY) {
            try { monkey.render(lastFramebuffer, lastStrength); }
            catch (Exception error) { fail(error); }
        }
    }
    public void renderPost() {
        if (!active) return;
        try {
            if (lastMode == MotionBlurMode.MOULBERRY) {
                moulberry.render(lastFramebuffer, lastStrength);
            } else if (lastMode == MotionBlurMode.PHOSPHOR) {
                if (phosphor == null) phosphor = loadShader(lastFramebuffer, "phosphor_motion_blur");
                float blend = phosphorHistory ? lastStrength / 100.0F * 3.0F + 0.69999999F - 0.0099999998F : 0.0F;
                for (Shader shader : phosphor.listShaders) {
                    shader.getShaderManager().getShaderUniformOrDefault("BlendFactor").set(blend);
                    shader.getShaderManager().getShaderUniformOrDefault("Mode").set(0.0F);
                }
                renderShader(phosphor);
                phosphorHistory = true;
            }
        } catch (Exception error) { fail(error); }
    }
    public void destroy() {
        if (phosphor != null) phosphor.deleteShaderGroup();
        phosphor = null;
        monkey.delete();
        moulberry.delete();
        phosphorHistory = false;
    }
}
