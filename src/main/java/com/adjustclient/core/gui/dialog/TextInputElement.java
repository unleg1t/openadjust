package com.adjustclient.core.gui.dialog;

import com.adjustclient.core.rendering.font.TextInputBox;

public final class TextInputElement {
    public TextInputBox box;
    public int index;
    public int width;
    public Object field16;
    TextInputElement(TextInputBox box, int index, int width) {
        this.box = box;
        this.index = index;
        this.width = width;
    }
}
