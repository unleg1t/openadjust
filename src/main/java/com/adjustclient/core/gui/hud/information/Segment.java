package com.adjustclient.core.gui.hud.information;

import com.adjustclient.core.rendering.font.TextComponent;

public final class Segment extends TextComponent {
    public boolean raised;
    public Object field16;

    public Segment(String text, int color) {
        super(text, color);
        this.raised = false;
        this.field16 = null;
    }

    public Segment(String text, int color, boolean raised) {
        super(text, color);
        this.raised = raised;
        this.field16 = null;
    }
}
