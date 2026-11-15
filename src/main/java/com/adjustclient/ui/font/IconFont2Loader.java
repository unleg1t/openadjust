package com.adjustclient.ui.font;

import com.adjustclient.render.font.FontFamily;
import com.adjustclient.render.font.Fonts;
import com.adjustclient.render.font.NvgFontRenderer;
import com.adjustclient.ui.screen.AdjustScreen;
import java.io.IOException;

public class IconFont2Loader extends AdjustScreen {
    public static NvgFontRenderer ICONS2;

    public static void b() throws IOException {
        ICONS2 = new NvgFontRenderer(new FontFamily("Icons2", Fonts.getFontStream("AdjustIcons2.ttf")), 7.0F, true);
    }
}
