package com.adjustclient.render.font.image;

import com.adjustclient.render.font.NvgContext;
import org.lwjgl.nanovg.NVGColor;
import org.lwjgl.nanovg.NVGPaint;

import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;

import static org.lwjgl.nanovg.NanoVG.*;
import com.adjustclient.util.BufferUtil;

public final class NvgImage {

    private ByteBuffer data;
    private int id;
    private Object field12;

    /** {@code stream} is read completely; {@code flags} are NVG_IMAGE_* flags (callers pass 0 or 32 = NEAREST). */
    public NvgImage(InputStream stream, int flags) throws IOException {
        this.data = BufferUtil.read(stream);               // same stream reader as FontFamily.register (assumed)
        this.id = nvgCreateImageMem(NvgContext.vg, flags, this.data);
    }

    public int getId() {
        return this.id;
    }

    public void draw(float x, float y, float width, float height) {
        nvgImagePattern(NvgContext.vg, x, y, width, height, 0.0F,
                this.getId(), 1.0F, NvgDraw.PAINT);
        nvgBeginPath(NvgContext.vg);
        nvgRect(NvgContext.vg, x, y, width, height);
        nvgFillPaint(NvgContext.vg, NvgDraw.PAINT);
        nvgFill(NvgContext.vg);
        nvgClosePath(NvgContext.vg);
    }

    public void draw(int color, float x, float y, float width, float height) {
        NVGColor tint = NvgContext.color(color);
        nvgImagePattern(NvgContext.vg, x, y, width, height, 0.0F, this.getId(), 1.0F, NvgDraw.PAINT);
        nvgBeginPath(NvgContext.vg);
        nvgRect(NvgContext.vg, x, y, width, height);
        NvgDraw.PAINT.innerColor(tint);
        nvgFillPaint(NvgContext.vg, NvgDraw.PAINT);
        nvgFill(NvgContext.vg);
        nvgClosePath(NvgContext.vg);
        tint.free();
    }
}
