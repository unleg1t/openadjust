package com.adjustclient.render.font;

import it.unimi.dsi.fastutil.floats.Float2ObjectOpenHashMap;
import net.minecraft.client.Minecraft;
import net.minecraft.util.ResourceLocation;

import java.io.IOException;
import java.io.InputStream;
import com.adjustclient.event.FontReloadEvent;
import com.adjustclient.ui.font.IconFont2Loader;
import com.adjustclient.ui.hud.toggled.ToggledList;

public final class Fonts {

    // ---- families -------------------------------------------------------------------------------------------
    public static FontFamily TAHOMA;
    public static FontFamily ICONS;
    public static FontFamily SERAPH;

    // ---- pre-created renderers (nominal size in the name) -----------------------------------------------------
    public static NvgFontRenderer TAHOMA_8;
    public static NvgFontRenderer TAHOMA_9;
    public static NvgFontRenderer TAHOMA_10;
    public static NvgFontRenderer TAHOMA_11;
    public static NvgFontRenderer TAHOMA_12;
    public static NvgFontRenderer TAHOMA_13;
    public static NvgFontRenderer TAHOMA_14;
    public static NvgFontRenderer TAHOMA_20;
    public static NvgFontRenderer TAHOMA_22;
    public static NvgFontRenderer ICONS_9;
    public static NvgFontRenderer ICONS_11;
    public static NvgFontRenderer ICONS_12;
    public static NvgFontRenderer ICONS_13;
    public static NvgFontRenderer ICONS_14;
    public static NvgFontRenderer SERAPH_10;

    private static final Float2ObjectOpenHashMap<NvgFontRenderer> TAHOMA_RENDERERS = new Float2ObjectOpenHashMap<>();
    private static final Float2ObjectOpenHashMap<NvgFontRenderer> ICON_RENDERERS = new Float2ObjectOpenHashMap<>();
    private static final Float2ObjectOpenHashMap<NvgFontRenderer> SERAPH_RENDERERS = new Float2ObjectOpenHashMap<>();

    /** Loads all font families and creates the standard renderers. Called from {@link NvgContext#init()}. */
    public static void init() throws IOException {
        TAHOMA = new FontFamily("Tahoma",
                getFontStream("Tahoma.ttf"),
                getFontStream("TahomaBD.otf"),
                getFontStream("TahomaItalic.ttf"),
                getFontStream("TahomaBDItalic.ttf"));
        TAHOMA_8 = tahoma(8.0F);
        TAHOMA_9 = tahoma(9.0F);
        TAHOMA_10 = tahoma(10.0F);
        TAHOMA_11 = tahoma(11.0F);
        TAHOMA_12 = tahoma(12.0F);
        TAHOMA_13 = tahoma(13.0F);
        TAHOMA_14 = tahoma(14.0F);
        TAHOMA_20 = tahoma(20.0F);
        TAHOMA_22 = tahoma(22.0F);
        ICONS = new FontFamily("Icons", getFontStream("AdjustIcons.ttf"));
        ICONS_9 = icons(9.0F);
        ICONS_11 = icons(11.0F);
        ICONS_12 = icons(12.0F);
        ICONS_13 = icons(13.0F);
        ICONS_14 = icons(14.0F);
        SERAPH = new FontFamily("Seraph", getFontStream("SeraphTags.ttf"));
        SERAPH_10 = seraph(10.0F);
        IconFont2Loader.b();
        if (ToggledList.INSTANCE != null) {
            new FontReloadEvent();
            ToggledList.INSTANCE.refresh();
        }
    }

    /** Tahoma renderer for a nominal size (cached). */
    public static NvgFontRenderer tahoma(float size) {
        if (TAHOMA_RENDERERS.containsKey(size)) {
            return TAHOMA_RENDERERS.get(size);
        }
        NvgFontRenderer renderer = new NvgFontRenderer(TAHOMA, size * 0.5F, false);
        TAHOMA_RENDERERS.put(size, renderer);
        return renderer;
    }

    /** Opens {@code assets/adjust/font/<file>} through Minecraft's default resource pack. */
    public static InputStream getFontStream(String file) throws IOException {
        return Minecraft.getMinecraft().mcDefaultResourcePack
                .getInputStream(new ResourceLocation("adjust", "font/" + file));
    }

    /** AdjustIcons renderer for a nominal size (cached). */
    public static NvgFontRenderer icons(float size) {
        NvgFontRenderer renderer = ICON_RENDERERS.get(size);
        if (renderer == null) {
            renderer = new NvgFontRenderer(ICONS, size * 0.5F, true);
            ICON_RENDERERS.put(size, renderer);
        }
        return renderer;
    }

    /** SeraphTags renderer for a nominal size (cached). */
    public static NvgFontRenderer seraph(float size) {
        NvgFontRenderer renderer = SERAPH_RENDERERS.get(size);
        if (renderer == null) {
            renderer = new NvgFontRenderer(SERAPH, size * 0.5F, true);
            SERAPH_RENDERERS.put(size, renderer);
        }
        return renderer;
    }
}
