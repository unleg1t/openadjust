package com.adjustclient.ui.dialog;

import com.adjustclient.render.animation.Animation;
import com.adjustclient.render.animation.Easing;
import com.adjustclient.ui.hud.layout.Rect2f;

class DropdownOption extends Rect2f {
    public String text;
    public Animation hover = new Animation(Easing.LINEAR, 100L);
    public Object field28;

    DropdownOption(String text) {
        this.text = text;
        height = 10.0F;
    }
}
