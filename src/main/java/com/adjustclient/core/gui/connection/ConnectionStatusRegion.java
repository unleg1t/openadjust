package com.adjustclient.core.gui.connection;

import com.adjustclient.core.rendering.font.NvgFontRenderer;
import com.adjustclient.core.rendering.font.TextComponent;
import com.adjustclient.core.gui.screen.AdjustComponent;

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
