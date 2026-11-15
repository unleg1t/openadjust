package com.adjustclient.ui.mainmenu;

import com.adjustclient.render.animation.Animation;
import com.adjustclient.render.animation.Easing;

class VoronoiCell {
    Animation animation;
    float brightness;
    boolean fading;

    void update() {
        if (fading || animation.finished) {
            if (!fading) {
                fading = true;
                animation.duration = 100L;
                animation.startedAt = System.currentTimeMillis();
                animation.from = animation.value;
                animation.finished = false;
            }
            animation.update(0.0F);
        } else {
            animation.update(brightness);
        }
    }

    void highlight(float brightness) {
        if (animation == null) {
            animation = new Animation(Easing.LINEAR, 20L);
        }
        fading = false;
        this.brightness = brightness;
        animation.startedAt = System.currentTimeMillis();
        animation.from = animation.value;
        animation.finished = false;
    }
}
