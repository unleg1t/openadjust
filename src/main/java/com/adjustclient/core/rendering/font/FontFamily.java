package com.adjustclient.core.rendering.font;

import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;

import static org.lwjgl.nanovg.NanoVG.nvgCreateFontMem;
import com.adjustclient.utilities.BufferUtil;

public final class FontFamily {

    private static final List<ByteBuffer> FONT_BUFFERS = new ArrayList<>();

    public String regular;
    public String bold;
    public String italic;
    public String boldItalic;
    public String field20;

    public FontFamily(String name, InputStream regular) throws IOException {
        this.regular = this.register(name, regular);
    }

    public FontFamily(String name, InputStream regular, InputStream bold, InputStream italic, InputStream boldItalic) throws IOException {
        this.regular = this.register(name, regular);
        this.bold = this.register(name + "Bold", bold);
        this.italic = this.register(name + "Italic", italic);
        this.boldItalic = this.register(name + "BoldItalic", boldItalic);
    }

    /** Creates a NanoVG memory font called {@code name} from {@code stream}; returns {@code name}. */
    public String register(String name, InputStream stream) throws IOException {
        ByteBuffer data = BufferUtil.read(stream);                 // read whole stream into a direct ByteBuffer
        nvgCreateFontMem(NvgContext.vg, name, data, false);
        FONT_BUFFERS.add(data);
        return name;
    }
}
