package com.adjustclient.service.party.ui;
import com.adjustclient.render.animation.*;
import com.adjustclient.render.font.*;
public abstract class ContextMenuEntry {
    public Animation hover = new Animation(Easing.LINEAR, 70L);
    public float height = 9.5F;
    public float x;
    public float y;
    public boolean hovered;
}
