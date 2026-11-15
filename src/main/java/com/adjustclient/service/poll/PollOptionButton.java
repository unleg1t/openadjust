package com.adjustclient.service.poll;

import com.adjustclient.ui.screen.AdjustComponent;
import com.adjustclient.render.animation.Animation;
import com.adjustclient.render.animation.Easing;

public final class PollOptionButton extends AdjustComponent {
    public PollService poll;
    public String text;
    public Animation hover = new Animation(Easing.LINEAR, 100L);
    public Animation selection = new Animation(Easing.LINEAR, 150L);
    public Animation result = new Animation(Easing.EASE_OUT_EXPO, 0L);
    public int votes;
    public int percentage;

    public PollOptionButton(String text, int votes) {
        this.text = text;
        this.votes = votes;
    }
}
