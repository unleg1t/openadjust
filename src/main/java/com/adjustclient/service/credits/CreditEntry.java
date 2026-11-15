package com.adjustclient.service.credits;

import com.adjustclient.render.animation.Animation;
import com.adjustclient.render.animation.Easing;

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
