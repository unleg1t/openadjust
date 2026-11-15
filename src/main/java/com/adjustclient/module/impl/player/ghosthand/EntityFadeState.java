package com.adjustclient.module.impl.player.ghosthand;

import com.adjustclient.render.animation.Animation;
import com.adjustclient.render.animation.Easing;

public class EntityFadeState {
    public final Animation target;
    public final Animation armor;
    public boolean retiring;

    public EntityFadeState() {
        target = new Animation(Easing.EASE_OUT_EXPO, 300L);
        target.value = 1.0F;
        armor = new Animation(Easing.EASE_OUT_EXPO, 300L);
        armor.value = 1.0F;
    }
}
