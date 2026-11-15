package com.adjustclient.ui.dialog;

import com.adjustclient.render.font.NvgFontRenderer;
import com.adjustclient.util.Vec2f;

public final class ActionLabel {
    public String text;
    public NvgFontRenderer font;
    public com.adjustclient.render.animation.Animation action;
    public float x;
    public float y;
    public Object field24;

    public ActionLabel(String text, NvgFontRenderer font) {
        this.text = text;
        this.font = font;
    }
    public boolean contains(Vec2f mouse) {
        return mouse.x >= x && mouse.x <= x + font.getStringWidth(text)
                && mouse.y >= y && mouse.y <= y + font.size;
    }
}
