package com.adjustclient.render.animation;

import com.adjustclient.util.Timer;

public class TimedFade {
    public Timer timer;
    public Animation fadeIn;
    public Animation fadeOut;
    public long fadeInDuration;
    public long holdDuration;
    public long fadeOutDuration;

    public TimedFade(long fadeInDuration, long holdDuration, long fadeOutDuration) {
        this.fadeInDuration = fadeInDuration;
        this.holdDuration = holdDuration;
        this.fadeOutDuration = fadeOutDuration;
    }

    public float b() {
        if (fadeIn == null) fadeIn = new Animation(Easing.LINEAR, fadeInDuration);
        if (!fadeIn.finished) {
            fadeIn.update(1.0F);
            return fadeIn.value;
        }
        if (timer == null) timer = new Timer();
        if (!timer.hasElapsed(holdDuration)) return 1.0F;
        if (fadeOut == null) {
            fadeOut = new Animation(Easing.LINEAR, fadeOutDuration);
            fadeOut.value = 1.0F;
            fadeOut.startedAt = System.currentTimeMillis();
            fadeOut.from = fadeOut.value;
            fadeOut.finished = false;
        }
        if (fadeOut.finished) return 0.0F;
        fadeOut.update(0.0F);
        return fadeOut.value;
    }
}
