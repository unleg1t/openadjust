package com.adjustclient.core.gui.font;

import com.adjustclient.core.rendering.font.FontFamily;
import com.adjustclient.core.rendering.font.Fonts;
import com.adjustclient.core.rendering.font.NvgFontRenderer;
import com.adjustclient.core.gui.screen.AdjustScreen;
import java.io.IOException;

public class IconFont2Loader extends AdjustScreen {
    public static NvgFontRenderer ICONS2;

    public static void b() throws IOException {
        ICONS2 = new NvgFontRenderer(new FontFamily("Icons2", Fonts.getFontStream("AdjustIcons2.ttf")), 7.0F, true);
    }
}
