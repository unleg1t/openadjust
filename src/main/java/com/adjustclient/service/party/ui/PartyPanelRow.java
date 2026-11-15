package com.adjustclient.service.party.ui;

import com.adjustclient.render.animation.*;

public abstract class PartyPanelRow {
    public Animation highlightAnimation = new Animation(Easing.LINEAR, 0L);
    public float height = 8.0F;
    public boolean heightCounted;
    public boolean field13;
    public float updateHighlight(boolean hovered, boolean selected) {
        highlightAnimation.duration = hovered ? 65L : 90L;
        float target = selected ? (hovered ? 1.0F : 0.77999997F) : (hovered ? 0.75F : 0.0F);
        highlightAnimation.update(target);
        return target;
    }
    public abstract void onMouseClick(com.adjustclient.event.ScreenMouseClickEvent event);
    public abstract void render(com.adjustclient.event.Render2DEvent event, float x, float y, float width);
}
