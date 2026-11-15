package com.adjustclient.ui.connection;

import com.adjustclient.render.font.NvgFontRenderer;
import com.adjustclient.render.font.TextComponent;
import com.adjustclient.util.Vec2f;

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
