package com.adjustclient.core.rendering.font.svg;

import com.adjustclient.core.rendering.font.NvgContext;
import org.lwjgl.nanovg.NSVGImage;
import org.lwjgl.nanovg.NVGColor;
import org.lwjgl.nanovg.NVGPaint;
import org.lwjgl.nanovg.NanoSVG;
import org.lwjgl.system.MemoryStack;
import org.lwjgl.system.MemoryUtil;

import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;

import static org.lwjgl.nanovg.NanoVG.*;

public final class SvgImage implements AutoCloseable {

    public record Bounds(float x, float y, float width, float height) {
    }

    public record RasterSize(int pixelWidth, int pixelHeight) {
    }

    private final NSVGImage image;
    private final LinkedHashMap<RasterSize, Integer> rasters;
    private final float width;
    private final long rasterizer;
    private final float height;
    private int field28;
    private Object field32;

    public SvgImage(InputStream stream) throws IOException {
        this.image = parse(stream.readAllBytes());
        this.width = this.image.width();
        this.height = this.image.height();
        this.rasterizer = NanoSVG.nsvgCreateRasterizer();
        this.rasters = new LinkedHashMap<>(16, 0.75F, true);
    }

    /** Parses SVG text (UTF-8/ASCII bytes) with NanoSVG, units "px", 96 dpi. Throws IllegalArgumentException when NanoSVG fails. */
    private static NSVGImage parse(byte[] svg) {
        ByteBuffer data = MemoryUtil.memAlloc(svg.length + 1);
        try (MemoryStack stack = MemoryStack.stackPush()) {
            data.put(svg).put((byte) 0).flip();
            NSVGImage parsed = NanoSVG.nsvgParse(data, stack.ASCII("px"), 96.0F);
            if (parsed == null) {
                throw new IllegalArgumentException("NanoSVG could not parse the SVG data");
            }
            return parsed;
        } finally {
            MemoryUtil.memFree(data);
        }
    }

    public void draw(FitMode mode, int color, float x, float y, float width, float height, float alpha) {
        if (NvgContext.currentScale == null) {
            throw new IllegalStateException("SVGs can only be drawn inside a NanoVG frame");
        }
        if (!Float.isFinite(x) || !Float.isFinite(y) || !Float.isFinite(width) || !Float.isFinite(height) || !Float.isFinite(alpha)) {
            throw new IllegalArgumentException("SVG draw arguments must be finite");
        }
        if (width <= 0.0F) {
            return;
        }
        if (height <= 0.0F || alpha <= 0.0F) {
            return;
        }
        float clampedAlpha = alpha <= 1.0F ? alpha : 1.0F;
        Bounds bounds = this.layout(mode, x, y, width, height);
        int texture = this.getTexture(bounds.width(), bounds.height());
        nvgSave(NvgContext.vg);
        try {
            if (mode == FitMode.COVER) {
                nvgIntersectScissor(NvgContext.vg, x, y, width, height);
            }
            try (MemoryStack stack = MemoryStack.stackPush()) {
                NVGPaint paint = NVGPaint.malloc(stack);
                nvgImagePattern(NvgContext.vg, bounds.x(), bounds.y(), bounds.width(), bounds.height(),
                        0.0F, texture,
                        1.0F, paint);
                tint(paint, color, stack, clampedAlpha);
                nvgBeginPath(NvgContext.vg);
                nvgRect(NvgContext.vg, bounds.x(), bounds.y(), bounds.width(), bounds.height());
                nvgFillPaint(NvgContext.vg, paint);
                nvgFill(NvgContext.vg);
                nvgClosePath(NvgContext.vg);
            }
        } finally {
            nvgRestore(NvgContext.vg);
        }
    }

    /** Sets both gradient colours of {@code paint} to {@code argb} with its alpha multiplied by {@code alpha}. */
    private static void tint(NVGPaint paint, int argb, MemoryStack stack, float alpha) {
        NVGColor c = NVGColor.malloc(stack);
        c.r((float) (argb >> 16 & 0xFF) / 255.0F);
        c.g((float) (argb >> 8 & 0xFF) / 255.0F);
        c.b((float) (argb & 0xFF) / 255.0F);
        c.a(alpha * ((float) (argb >>> 24) / 255.0F));
        paint.innerColor(c);
        paint.outerColor(c);
    }

    /** Computes where the SVG lands inside the destination rectangle (centred, aspect ratio kept except for STRETCH). */
    private Bounds layout(FitMode mode, float x, float y, float width, float height) {
        if (mode == FitMode.STRETCH) {
            return new Bounds(x, y, width, height);
        }
        float scaleX = width / this.width;
        float scaleY = height / this.height;
        float scale = mode == FitMode.CONTAIN ? Math.min(scaleX, scaleY) : Math.max(scaleX, scaleY);   // Math.min / Math.max (±0 handling)
        float w = scale * this.width;
        float h = scale * this.height;
        return new Bounds(x + (width - w) * 0.5F, y + (height - h) * 0.5F, w, h);
    }

    private int getTexture(float targetWidth, float targetHeight) {
        Float frameScale = NvgContext.currentScale;
        if (frameScale != null) {
            float s = Math.abs(frameScale);
            if (s > 1.0F) {
                targetWidth *= s;
                targetHeight *= s;
            }
        }
        float scale = Math.max(targetWidth / this.width, targetHeight / this.height);
        float largest = Math.max(this.width, this.height);
        int pixelWidth;
        int pixelHeight;
        if (scale * largest > 4096.0F) {
            float limited = 4096.0F / largest;
            pixelHeight = Math.max(1, Math.round(limited * this.height));
            pixelWidth = Math.max(1, Math.round(limited * this.width));
        } else {
            pixelHeight = Math.max(1, Math.round(scale * this.height));
            pixelWidth = Math.max(1, Math.round(scale * this.width));
        }
        RasterSize key = new RasterSize(pixelWidth, pixelHeight);
        Integer cached = this.rasters.get(key);
        if (cached != null) {
            return cached;
        }
        int byteCount;
        try {
            byteCount = Math.multiplyExact(Math.multiplyExact(pixelHeight, pixelWidth), 4);
        } catch (ArithmeticException e) {
            throw new IllegalArgumentException("SVG raster size is too large", e);
        }
        ByteBuffer pixels = MemoryUtil.memAlloc(byteCount);
        try {
            float fit = Math.min((float) pixelWidth / this.width, (float) pixelHeight / this.height);
            NanoSVG.nsvgRasterize(this.rasterizer, this.image,
                    ((float) pixelWidth - this.width * fit) * 0.5F, ((float) pixelHeight - this.height * fit) * 0.5F, fit,
                    pixels, pixelWidth, pixelHeight, 4 * pixelWidth);
            int id = nvgCreateImageRGBA(NvgContext.vg, pixelWidth, pixelHeight, 0, pixels);
            if (id == 0) {
                throw new IllegalStateException("NanoVG could not create an image for the SVG");
            }
            this.rasters.put(key, id);
            this.evict();
            return id;
        } finally {
            MemoryUtil.memFree(pixels);
        }
    }

    /** Deletes the least recently used raster when five or more are cached. */
    private void evict() {
        while (this.rasters.size() >= 5) {
            Iterator<Integer> it = this.rasters.values().iterator();
            int id = it.next();
            it.remove();
            nvgDeleteImage(NvgContext.vg, id);
        }
    }

    @Override
    public void close() {
    }
}
