package com.adjustclient.ui.connection;

import com.adjustclient.render.font.NvgFontRenderer;
import com.adjustclient.render.font.TextComponent;
import com.adjustclient.ui.screen.AdjustComponent;

public class ConnectionStatusRegion extends AdjustComponent {
    public ConnectionStatusRegion(NvgFontRenderer font, TextComponent[] components) {
        float totalWidth = 0.0F;
        for (TextComponent component : components) {
            totalWidth += font.getStringWidth(component.text);
        }
        this.x = 0.0F;
        this.y = 0.0F;
        this.width = totalWidth;
        this.height = font.size;
        this.owner = components;
    }
}
