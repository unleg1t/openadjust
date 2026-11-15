package com.adjustclient.module.impl.render.hud.widget;

import com.adjustclient.render.animation.Animation;
import com.adjustclient.util.Timer;

import java.awt.Color;
import java.util.List;

final class ClickTracker {

    ClickSpeedWidget owner;
    List<Timer> clicks;
    Animation animation;

    ClickTracker(ClickSpeedWidget owner, Animation animation) {
        this.owner = owner;
        this.clicks = new java.util.ArrayList<>();
        this.animation = animation;
    }

    /** Removal predicate of the click list: the click happened at least one second ago. */
    private static boolean isOld(Timer click) {
        return click.hasElapsed(1000L);
    }

    /** Clicks per second: drops clicks older than one second and returns the count (at most 20). */
    int getCps() {
        this.clicks.removeIf(ClickTracker::isOld);
        return Math.min(20, this.clicks.size());
    }

    /**
     * Colour of the CPS text: a grey whose brightness (205 + 5 * t) and alpha (185 + 9 * t) rise with the flash animation and
     * the click count, t = animation + cps / 20 (just the animation when there are no clicks).
     */
    int getColor() {
        this.animation.update(0.0F);
        float flash = this.animation.getValue();
        this.clicks.removeIf(ClickTracker::isOld);
        int count = Math.min(20, this.clicks.size());
        int gray;
        int alpha;
        if (count == 0) {
            gray = (int) (flash * 5.0F + 205.0F);
            alpha = (int) (flash * 9.0F + 185.0F);
        } else {
            float t = (float) count / 20.0F + flash;
            gray = (int) (t * 5.0F + 205.0F);
            alpha = (int) (t * 9.0F + 185.0F);
        }
        return new Color(gray, gray, gray, alpha).getRGB();
    }
}
