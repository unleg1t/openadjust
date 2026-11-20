package com.adjustclient.managers.credits;

import com.adjustclient.core.rendering.animation.Animation;
import com.adjustclient.core.rendering.animation.Easing;

class CreditEntry {
    Animation animation;
    String text;
    int index;

    CreditEntry(int index, String text) {
        this.index = index;
        this.text = text;
    }

    float getProgress() {
        if (animation == null) {
            animation = new Animation(Easing.LINEAR, 7000L);
        }
        return animation.update(1.0F);
    }
}
