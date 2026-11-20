package com.adjustclient.core.gui.connection;

import com.adjustclient.core.rendering.font.NvgFontRenderer;
import com.adjustclient.core.rendering.font.TextComponent;
import com.adjustclient.utilities.Vec2f;

class ConnectionStatusSegment {
    TextComponent[] components;
    NvgFontRenderer font;
    float y;

    ConnectionStatusSegment() {}

    ConnectionStatusSegment(TextComponent[] components, NvgFontRenderer font) {
        this.components = components;
        this.font = font;
    }

    public Vec2f getSize() {
        float width = 0.0F;
        if (this.components != null && this.font != null) {
            for (TextComponent component : this.components) {
                width += this.font.getStringWidth(component.text);
            }
        }
        float height = this.font != null ? this.font.size + 3.0F : 3.0F;
        return new Vec2f(width, height);
    }
}
