package com.adjustclient.managers.party.ui;
import com.adjustclient.core.rendering.animation.*;
import com.adjustclient.core.rendering.font.*;
public abstract class ContextMenuEntry {
    public Animation hover = new Animation(Easing.LINEAR, 70L);
    public float height = 9.5F;
    public float x;
    public float y;
    public boolean hovered;
}
