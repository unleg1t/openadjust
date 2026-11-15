package com.adjustclient.changelog;

import com.adjustclient.render.animation.Animation;
import com.adjustclient.render.animation.Easing;
import java.util.function.Supplier;

public class ChangelogHighlight {
    public String text;
    public Supplier<Integer> color;
    public Runnable action;
    public Animation animation;
    public com.adjustclient.util.Timer clickTimer;
    public Object field24;

    // Plain spans have no action or hover animation.
    public ChangelogHighlight(String text) {
        this.text = text;
        this.color = () -> -4802889;
    }

    public ChangelogHighlight(String text, Supplier<Integer> color, Runnable action) {
        this.text = text;
        this.color = color;
        this.action = action;
        this.animation = new Animation(Easing.LINEAR, 120L);
    }
}
