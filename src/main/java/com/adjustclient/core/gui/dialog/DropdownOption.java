package com.adjustclient.core.gui.dialog;

import com.adjustclient.core.rendering.animation.Animation;
import com.adjustclient.core.rendering.animation.Easing;
import com.adjustclient.core.gui.hud.layout.Rect2f;

class DropdownOption extends Rect2f {
    public String text;
    public Animation hover = new Animation(Easing.LINEAR, 100L);
    public Object field28;

    DropdownOption(String text) {
        this.text = text;
        height = 10.0F;
    }
}
