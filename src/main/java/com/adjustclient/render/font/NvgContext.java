package com.adjustclient.render.font;

import com.adjustclient.logging.LogLevel;
import com.adjustclient.logging.Logger;
import com.adjustclient.render.font.image.NvgDraw;
import net.minecraft.client.renderer.EntityRenderer;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.OpenGlHelper;
import org.joml.Vector4f;
import org.lwjgl.nanovg.NVGColor;
import org.lwjgl.nanovg.NanoVGGL2;
import org.lwjgl.nanovg.NanoVGGL3;
import org.lwjgl.opengl.GL;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL15;
import org.lwjgl.opengl.GL20;
import org.lwjgl.opengl.GL30;
import org.lwjgl.opengl.GL31;
import org.lwjgl.opengl.GLCapabilities;
import org.lwjgl.system.MemoryStack;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.IntBuffer;

import static org.lwjgl.nanovg.NanoVG.*;

public final class NvgContext {
    public static boolean roundScaledPixels;

    /** The NanoVG context handle (NVGcontext*). */
    public static long vg;

    /** Scale of the NanoVG frame currently open, or {@code null} when no frame is open. */
    public static Float currentScale;

    /** Scale saved by {@link #pushEvenScale()} while the frame is temporarily rescaled; {@code null} otherwise. */
    public static Float savedScale;

    /** {@code savedScale / 2} while {@link #pushEvenScale()} is active; {@code null} otherwise. */
    public static Float scaleFactor;

    public static void init() throws IOException {
        if (GL.getCapabilities().OpenGL30) {
            vg = NanoVGGL3.nvgCreate(1);
            if (vg == 0L) {
                Logger.log(LogLevel.WARN, true, "NanoVG OpenGL 3.0 initialization failed, falling back to OpenGL 2.0", new Object[0]);
                vg = NanoVGGL2.nvgCreate(1);
            }
        } else {
            Logger.log(LogLevel.INFO, true, "Using NanoVG for OpenGL 2.0", new Object[0]);
            vg = NanoVGGL2.nvgCreate(1);
        }
        Fonts.init();
    }

    /** Builds a newly calloc'ed NVGColor from an ARGB int (caller frees it). */
    public static NVGColor color(int argb) {
        float red = (float) (argb >> 16 & 0xFF);
        float green = (float) (argb >> 8 & 0xFF);
        float blue = (float) (argb & 0xFF);
        float alpha = (float) (argb >> 24 & 0xFF);
        return NVGColor.calloc()
                .r(red / 255.0F)
                .g(green / 255.0F)
                .b(blue / 255.0F)
                .a(alpha / 255.0F);
    }

    /** Changes the scale of the open frame to {@code scale} (no-op without an open frame). */
    public static void setScale(float scale) {
        if (currentScale == null) {
            return;
        }
        float relative = scale / currentScale;
        nvgScale(vg, relative, relative);
        currentScale = scale;
    }

    /** Opens a NanoVG frame covering the whole display, scaled by {@code scale}. */
    public static void beginFrame(boolean pushAttrib, float scale) {
        if (currentScale != null) {
            return;
        }
        if (pushAttrib) {
            GlStateManager.pushAttrib();
        }
        float viewportWidth = Math.max(1.0F, (float) org.lwjgl.opengl.Display.getWidth());
        float viewportHeight = Math.max(1.0F, (float) org.lwjgl.opengl.Display.getHeight());
        float pixelRatio = Math.max(2.0F, org.lwjgl.opengl.Display.getPixelScaleFactor());
        nvgBeginFrame(vg, viewportWidth, viewportHeight, pixelRatio);
        nvgScale(vg, scale, scale);
        nvgShapeAntiAlias(vg, false);
        currentScale = scale;
    }

    /**
     * Converts a rectangle given in frame units into pixels. When the frame is temporarily rescaled
     * ({@link #pushEvenScale()}), sizes of at most one unit are snapped to a whole multiple of the factor.
     */
    public static Vector4f toPixels(float x1, float y1, float x2, float y2) {
        if (savedScale == null) {
            return new Vector4f(x1, y1, x2, y2);
        }
        float factor = scaleFactor;
        float px1 = x1 * factor;
        float py1 = y1 * factor;
        float height = y2 - y1;
        float width = x2 - x1;
        int whole = roundScaledPixels ? Math.round(factor) : (int) factor;
        float px2 = width > 1.0F ? x2 * factor : px1 + width * (float) whole;
        float py2 = height > 1.0F ? y2 * factor : py1 + height * (float) whole;
        return new Vector4f(px1, py1, px2, py2);
    }

    /** Closes the open NanoVG frame and restores the GL state Minecraft expects. */
    public static void endFrame() {
        if (currentScale == null) {
            return;
        }
        endNanoVgFrame(false);
        GlStateManager.popAttrib();
        restoreGlState();
        currentScale = null;
    }

    /** Flushes the open frame (end + begin again with the same scale), restoring scissors and global alpha. */
    public static void flushFrame() {
        if (currentScale == null) {
            return;
        }
        float scale = currentScale;
        // Preserve caller enable/scissor state without popping the outer beginFrame attrib stack entry.
        endNanoVgFrame(true);
        currentScale = null;
        restoreGlState();
        beginFrame(false, scale);
        NvgDraw.reapplyScissors();
        NvgDraw.setGlobalAlpha(NvgDraw.getGlobalAlpha());
    }

    /**
     * If the current scale is not a multiple of 2, temporarily rescales the open frame to exactly 2 and remembers
     * the old scale and factor in {@link #savedScale} and {@link #scaleFactor}.
     */
    public static void pushEvenScale() {
        if (currentScale == null) {
            return;
        }
        float scale = currentScale;
        if (scale % 2.0F == 0.0F) {
            return;
        }
        savedScale = currentScale;
        nvgScale(vg, 2.0F / scale, 2.0F / scale);
        currentScale = 2.0F;
        scaleFactor = savedScale / 2.0F;
    }

    /** Undoes {@link #pushEvenScale()}. */
    public static void popEvenScale() {
        if (savedScale == null) {
            return;
        }
        setScale(savedScale);
        scaleFactor = null;
        savedScale = null;
    }

    /** Resets blend function, bound texture, blending and depth test after NanoVG rendering. */
    public static void restoreGlState() {
        // Force both OpenGL state and GlStateManager's cached flags/slots into sync after nvgEndFrame/popAttrib.
        GlStateManager.setActiveTexture(OpenGlHelper.defaultTexUnit);
        OpenGlHelper.setActiveTexture(OpenGlHelper.defaultTexUnit);
        GlStateManager.enableTexture2D();
        GL11.glEnable(GL11.GL_TEXTURE_2D);
        GlStateManager.bindTexture(0);
        GL11.glBindTexture(GL11.GL_TEXTURE_2D, 0);
        GlStateManager.tryBlendFuncSeparate(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA, GL11.GL_ZERO, GL11.GL_ONE);
        OpenGlHelper.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA, GL11.GL_ZERO, GL11.GL_ONE);
        GlStateManager.disableBlend();
        GlStateManager.enableBlend();
        GlStateManager.disableDepth();
        GlStateManager.enableDepth();
        GlStateManager.disableColorLogic();
        GL11.glDisable(GL11.GL_COLOR_LOGIC_OP);
    }

    /** Flushes NanoVG while preserving GL3 bindings and fixed-function states omitted by {@code glPushAttrib(8256)}. */
    private static void endNanoVgFrame(boolean preserveEnableAndScissor) {
        GLCapabilities caps = GL.getCapabilities();
        int prevProgram = caps.OpenGL20 ? GL11.glGetInteger(GL20.GL_CURRENT_PROGRAM) : 0;
        int prevVao = caps.OpenGL30 ? GL11.glGetInteger(GL30.GL_VERTEX_ARRAY_BINDING) : 0;
        int prevVbo = caps.OpenGL15 ? GL11.glGetInteger(GL15.GL_ARRAY_BUFFER_BINDING) : 0;
        int prevUbo = caps.OpenGL31 ? GL11.glGetInteger(GL31.GL_UNIFORM_BUFFER_BINDING) : 0;
        int prevFrontFace = GL11.glGetInteger(GL11.GL_FRONT_FACE);
        int prevCullFaceMode = GL11.glGetInteger(GL11.GL_CULL_FACE_MODE);

        boolean prevScissorTest = false;
        boolean prevStencilTest = false;
        boolean prevCullFace = false;
        boolean prevTex2D = false;
        int scissorX = 0;
        int scissorY = 0;
        int scissorW = 0;
        int scissorH = 0;
        boolean colorMaskR = true;
        boolean colorMaskG = true;
        boolean colorMaskB = true;
        boolean colorMaskA = true;

        try (MemoryStack stack = MemoryStack.stackPush()) {
            IntBuffer box = stack.mallocInt(4);
            GL11.glGetIntegerv(GL11.GL_SCISSOR_BOX, box);
            scissorX = box.get(0);
            scissorY = box.get(1);
            scissorW = box.get(2);
            scissorH = box.get(3);

            ByteBuffer mask = stack.malloc(4);
            GL11.glGetBooleanv(GL11.GL_COLOR_WRITEMASK, mask);
            colorMaskR = mask.get(0) != 0;
            colorMaskG = mask.get(1) != 0;
            colorMaskB = mask.get(2) != 0;
            colorMaskA = mask.get(3) != 0;
        }

        if (preserveEnableAndScissor) {
            prevScissorTest = GL11.glIsEnabled(GL11.GL_SCISSOR_TEST);
            prevStencilTest = GL11.glIsEnabled(GL11.GL_STENCIL_TEST);
            prevCullFace = GL11.glIsEnabled(GL11.GL_CULL_FACE);
            prevTex2D = GL11.glIsEnabled(GL11.GL_TEXTURE_2D);
        }

        nvgEndFrame(vg);

        if (caps.OpenGL30) {
            GL30.glBindVertexArray(prevVao);
        }
        if (caps.OpenGL15) {
            GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, prevVbo);
        }
        if (caps.OpenGL31) {
            GL15.glBindBuffer(GL31.GL_UNIFORM_BUFFER, prevUbo);
        }
        if (caps.OpenGL20) {
            GL20.glUseProgram(prevProgram);
        }
        GL11.glFrontFace(prevFrontFace);
        GL11.glCullFace(prevCullFaceMode);
        GL11.glColorMask(colorMaskR, colorMaskG, colorMaskB, colorMaskA);
        GL11.glScissor(scissorX, scissorY, scissorW, scissorH);

        if (preserveEnableAndScissor) {
            setGlEnable(GL11.GL_SCISSOR_TEST, prevScissorTest);
            setGlEnable(GL11.GL_STENCIL_TEST, prevStencilTest);
            setGlEnable(GL11.GL_CULL_FACE, prevCullFace);
            setGlEnable(GL11.GL_TEXTURE_2D, prevTex2D);
        }
    }

    private static void setGlEnable(int cap, boolean enabled) {
        if (enabled) {
            GL11.glEnable(cap);
        } else {
            GL11.glDisable(cap);
        }
    }

}
