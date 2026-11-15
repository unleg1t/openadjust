package com.adjustclient.render.font.image;

import com.adjustclient.render.font.NvgContext;
import org.lwjgl.nanovg.NVGColor;
import org.lwjgl.nanovg.NVGPaint;

import java.util.ArrayList;
import java.util.List;

import static org.lwjgl.nanovg.NanoVG.*;
import com.adjustclient.ui.screen.AdjustComponent;

public final class NvgDraw {

    public static final List<AdjustComponent> SCISSORS = new ArrayList<>();

    /** Last value passed to nvgGlobalAlpha (1.0 initially). */
    private static float globalAlpha = 1.0F;

    /** Preallocated paint object; used as the output paint of {@link NvgImage#draw} (nvgImagePattern). */
    static final NVGPaint PAINT = NVGPaint.create();

    public static void enableAntiAlias() {
        nvgShapeAntiAlias(NvgContext.vg, true);
    }

    public static void disableAntiAlias() {
        nvgShapeAntiAlias(NvgContext.vg, false);
    }

    public static void setGlobalAlpha(float alpha) {
        nvgGlobalAlpha(NvgContext.vg, alpha);
        globalAlpha = alpha;
    }

    public static float getGlobalAlpha() {
        return globalAlpha;
    }

    // ---- circles --------------------------------------------------------------------------------------------------

    /** Fills a circle with the current fill colour. */
    public static void fillCircle(float x, float y, float radius) {
        enableAntiAlias();
        nvgBeginPath(NvgContext.vg);
        nvgCircle(NvgContext.vg, x, y, radius);
        nvgFill(NvgContext.vg);
        nvgClosePath(NvgContext.vg);
        disableAntiAlias();
    }

    /** Fills a circle with an ARGB colour. */
    public static void fillCircle(int color, float x, float y, float radius) {
        NVGColor c = NvgContext.color(color);
        nvgFillColor(NvgContext.vg, c);
        fillCircle(x, y, radius);
        c.free();
    }

    // ---- rectangles -----------------------------------------------------------------------------------------------

    /** Fills the rectangle (x1, y1)-(x2, y2) with the current fill colour. */
    public static void fillRect(float x1, float y1, float x2, float y2) {
        nvgBeginPath(NvgContext.vg);
        nvgRect(NvgContext.vg, x1, y1, x2 - x1, y2 - y1);
        nvgFill(NvgContext.vg);
        nvgClosePath(NvgContext.vg);
    }

    /** Fills the rectangle (x1, y1)-(x2, y2) with an ARGB colour. */
    public static void fillRect(int color, float x1, float y1, float x2, float y2) {
        NVGColor c = NvgContext.color(color);
        nvgFillColor(NvgContext.vg, c);
        fillRect(x1, y1, x2, y2);
        c.free();
    }

    /** Fills a rounded rectangle (x1, y1)-(x2, y2) with the current fill colour. */
    public static void fillRoundedRect(float x1, float y1, float x2, float y2, float radius) {
        enableAntiAlias();
        nvgBeginPath(NvgContext.vg);
        nvgRoundedRect(NvgContext.vg, x1, y1, x2 - x1, y2 - y1, radius);
        nvgFill(NvgContext.vg);
        nvgClosePath(NvgContext.vg);
        disableAntiAlias();
    }

    /** Fills a rounded rectangle with an ARGB colour. */
    public static void fillRoundedRect(int color, float x1, float y1, float x2, float y2, float radius) {
        NVGColor c = NvgContext.color(color);
        nvgFillColor(NvgContext.vg, c);
        fillRoundedRect(x1, y1, x2, y2, radius);
        c.free();
    }

    /**
     * Strokes the border of the rectangle (x1, y1)-(x2, y2).
     * {@code outside}: the stroke is centred on a rectangle grown by half the line width (so the border lies
     * outside the rectangle); otherwise it is shrunk by half the line width (border inside).
     */
    public static void strokeRect(boolean outside, int color, float x1, float y1, float x2, float y2, float lineWidth) {
        NVGColor c = NvgContext.color(color);
        nvgStrokeColor(NvgContext.vg, c);
        float half = lineWidth * 0.5F;
        nvgBeginPath(NvgContext.vg);
        float width = x2 - x1;
        float height = y2 - y1;
        if (outside) {
            nvgRect(NvgContext.vg, x1 - half, y1 - half, width + lineWidth, height + lineWidth);
        } else {
            nvgRect(NvgContext.vg, x1 + half, y1 + half, width - lineWidth, height - lineWidth);
        }
        nvgStrokeWidth(NvgContext.vg, lineWidth);
        nvgStroke(NvgContext.vg);
        nvgClosePath(NvgContext.vg);
        c.free();
    }

    // ---- triangles ------------------------------------------------------------------------------------------------

    /** Fills the triangle (x1, y1), (x2, y2), (x3, y3) with the current fill colour. */
    public static void fillTriangle(float x1, float y1, float x2, float y2, float x3, float y3) {
        enableAntiAlias();
        nvgBeginPath(NvgContext.vg);
        nvgMoveTo(NvgContext.vg, x1, y1);
        nvgLineTo(NvgContext.vg, x2, y2);
        nvgLineTo(NvgContext.vg, x3, y3);
        nvgFill(NvgContext.vg);
        nvgClosePath(NvgContext.vg);
        disableAntiAlias();
    }

    /** Fills a triangle with an ARGB colour. */
    public static void fillTriangle(int color, float x1, float y1, float x2, float y2, float x3, float y3) {
        NVGColor c = NvgContext.color(color);
        nvgFillColor(NvgContext.vg, c);
        fillTriangle(x1, y1, x2, y2, x3, y3);
        c.free();
    }

    // ---- gradient -------------------------------------------------------------------------------------------------

    /** Fills the rectangle (x1, y1)-(x2, y2) with a top-to-bottom linear gradient from {@code startColor} to {@code endColor}. */
    public static void fillGradientRect(int startColor, int endColor, float x1, float y1, float x2, float y2) {
        fillGradientRect(startColor, endColor, x1, y1, x2, y2, x1, y1, x1, y2);
    }

    public static void fillGradientRect(int startColor, int endColor, float x1, float y1, float x2, float y2, float sx, float sy, float ex, float ey) {
        NVGColor inner = NvgContext.color(startColor);
        NVGColor outer = NvgContext.color(endColor);
        nvgBeginPath(NvgContext.vg);
        NVGPaint paint = NVGPaint.calloc();
        nvgRect(NvgContext.vg, x1, y1, x2 - x1, y2 - y1);
        nvgLinearGradient(NvgContext.vg, sx, sy, ex, ey, inner, outer, paint);
        nvgFillPaint(NvgContext.vg, paint);
        nvgFill(NvgContext.vg);
        paint.free();
        nvgClosePath(NvgContext.vg);
        outer.free();
        inner.free();
    }

    // ---- scissor stack --------------------------------------------------------------------------------------------

    /**
     * Pops the innermost scissor: resets the scissor, removes the last stack entry and re-intersects the remaining ones.
     * Throws NoSuchElementException on an empty stack (ArrayList.removeLast).
     */
    public static void popScissor() {
        nvgResetScissor(NvgContext.vg);
        SCISSORS.removeLast();
        for (AdjustComponent rect : SCISSORS) {
            nvgIntersectScissor(NvgContext.vg, rect.getX(), rect.getY(),
                    rect.width /* width */, rect.height /* height */);
        }
    }

    /** Re-applies every scissor of the stack to the (freshly begun) NanoVG frame. Used by NvgContext.flushFrame. */
    public static void reapplyScissors() {
        for (AdjustComponent rect : SCISSORS) {
            nvgIntersectScissor(NvgContext.vg, rect.getX(), rect.getY(), rect.width, rect.height);
        }
    }

    /** Pushes a scissor rectangle (x, y, width, height) and intersects it with the current scissor. */
    public static void pushScissor(float x, float y, float width, float height) {
        SCISSORS.add(new AdjustComponent(x, y, width, height));
        nvgIntersectScissor(NvgContext.vg, x, y, width, height);
    }

    /** Innermost scissor rectangle, or {@code null} when the stack is empty. */
    public static AdjustComponent peekScissor() {
        return SCISSORS.isEmpty() ? null : SCISSORS.getLast();
    }
}
