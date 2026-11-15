package com.adjustclient.ui.screen;

public class ClickableRegion<T> extends AdjustComponent {
    public Object field24;
    public ClickableRegion(T payload) { owner = payload; }
    public ClickableRegion(float x, float y, float width, float height, T payload) {
        super(x, y, width, height); owner = payload;
    }
}
